package com.example.mobileapp.areaPersonale

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.mobileapp.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import android.Manifest

class AggiungiSpesaFragment : Fragment(R.layout.fragment_aggiungi_spesa) {

    private lateinit var autoCompleteCategorie: AutoCompleteTextView
    private val categorieList = mutableListOf<String>()
    private lateinit var launcherGalleria: ActivityResultLauncher<String>
    private lateinit var launcherCamera: ActivityResultLauncher<Uri>
    private lateinit var fileFotoUri: Uri
    private lateinit var callback: OnSpesaAggiuntaListener
    private lateinit var db: FirebaseFirestore

    interface OnSpesaAggiuntaListener {
        fun onSpesaAggiunta(spesa: Spesa)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSpesaAggiuntaListener) {
            callback = context
        } else {
            throw RuntimeException("$context deve implementare OnSpesaAggiuntaListener")
        }
        db = FirebaseFirestore.getInstance()
    }

    private val imageUris = mutableListOf<Uri>()
    private lateinit var launcher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_aggiungi_spesa, container, false)

        val btnAggiungiFoto = view.findViewById<Button>(R.id.btnAggiungiFoto)
        val layoutGalleria = view.findViewById<LinearLayout>(R.id.layoutGalleria)

        launcherGalleria = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            imageUris.addAll(uris)
            mostraAnteprime(layoutGalleria)
        }

        launcherCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageUris.add(fileFotoUri)
                mostraAnteprime(layoutGalleria)
            }
        }

        launcher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            imageUris.clear()
            imageUris.addAll(uris)
            mostraAnteprime(layoutGalleria)
        }

        btnAggiungiFoto.setOnClickListener {
            mostraSceltaFotoDialog()
        }

        autoCompleteCategorie = view.findViewById(R.id.categoriaSpesa)
        caricaCategorie()

        val titoloSpesa = view.findViewById<EditText>(R.id.titoloSpesa)
        val descrizioneSpesa = view.findViewById<EditText>(R.id.descrizioneSpesa)
        val dataSpesa = view.findViewById<EditText>(R.id.DataSelezionata)
        val importoSpesa = view.findViewById<EditText>(R.id.importoSpesa)
        val categoriaSpesa = view.findViewById<AutoCompleteTextView>(R.id.categoriaSpesa)
        val btnConferma = view.findViewById<Button>(R.id.btnConfermaSpesa)

        categoriaSpesa.setOnClickListener { categoriaSpesa.showDropDown() }

        var giorno = 0
        var mese = 0
        var anno = 0

        dataSpesa.setOnClickListener {
            val calendario = Calendar.getInstance()
            anno = calendario.get(Calendar.YEAR)
            mese = calendario.get(Calendar.MONTH)
            giorno = calendario.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                giorno = selectedDay
                mese = selectedMonth + 1
                anno = selectedYear
                val dataFormattata = "$giorno/$mese/$anno"
                dataSpesa.setText(dataFormattata)
            }, anno, mese, giorno).show()
        }

        // PRECOMPILAZIONE CAMPI se arrivi da modifica
        arguments?.let { args ->
            titoloSpesa.setText(args.getString("titolo", ""))
            descrizioneSpesa.setText(args.getString("descrizione", ""))
            importoSpesa.setText(args.getFloat("importo", 0f).toString())
            autoCompleteCategorie.setText(args.getString("categoria", ""), false)

            giorno = args.getInt("giorno", 0)
            mese = args.getInt("mese", 0)
            anno = args.getInt("anno", 0)

            if (giorno != 0 && mese != 0 && anno != 0) {
                val dataFormattata = "$giorno/$mese/$anno"
                dataSpesa.setText(dataFormattata)
            }
        }

        // CLICK BOTTONE CONFERMA
        btnConferma.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val uid = currentUser?.uid ?: return@setOnClickListener

            val titolo = titoloSpesa.text.toString()
            val descrizione = descrizioneSpesa.text.toString()
            val importo = importoSpesa.text.toString().toFloatOrNull() ?: 0.0f
            val categoria = categoriaSpesa.text.toString()

            if (titolo.isBlank() || importo == 0.0f) {
                Toast.makeText(requireContext(), "Compila almeno il titolo e l'importo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val spesaMap = hashMapOf<String, Any>(
                "uid" to uid,
                "titolo" to titolo,
                "descrizione" to descrizione,
                "giorno" to giorno,
                "mese" to mese,
                "anno" to anno,
                "importo" to importo,
                "categoria" to categoria,
                "data" to Timestamp.now()
            )

            val documentId = arguments?.getString("documentId")

            if (!documentId.isNullOrBlank()) {
                // Modifica esistente
                db.collection("Spese").document(documentId)
                    .set(spesaMap)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Spesa modificata con successo!", Toast.LENGTH_SHORT).show()
                        callback.onSpesaAggiunta(Spesa(titolo, descrizione, giorno, mese, anno, importo, categoria, documentId))
                        val intent = Intent(requireContext(), SoloActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Errore nella modifica della spesa", Toast.LENGTH_SHORT).show()
                    }
            } else {
                salvaSpesaConImmagini(spesaMap,
                    onComplete = { docId ->
                        Toast.makeText(requireContext(), "Spesa Aggiunta Con Successo!", Toast.LENGTH_SHORT).show()
                        callback.onSpesaAggiunta(Spesa(titolo, descrizione, giorno, mese, anno, importo, categoria, docId))
                        val intent = Intent(requireContext(), SoloActivity::class.java)
                        startActivity(intent)
                    },
                    onError = { e ->
                        Toast.makeText(requireContext(), "Errore nel salvataggio della spesa con immagini", Toast.LENGTH_SHORT).show()
                        Log.e("AggiungiSpesaFragment", "Errore immagini/Firestore", e)
                    }
                )
            }
        }

        return view
    }

    private fun caricaCategorie() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Pulisce la lista e aggiunge categorie predefinite
        categorieList.clear()
        categorieList.addAll(listOf("Alimentari", "Trasporti", "Svago", "Abbigliamento", "Casa"))

        // Carica categorie personalizzate dal database
        db.collection("Utenti")
            .document(userId)
            .collection("categorie")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val titolo = doc.getString("titolo")?.trim()
                    if (!titolo.isNullOrBlank() && !categorieList.contains(titolo)) {
                        categorieList.add(titolo)
                    }
                }

                // aggiorna dropdown con lista visiva (include "Aggiungi Categoria")
                aggiornaAutoComplete()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Errore nel caricamento categorie", e)
            }
    }

    private fun aggiornaAutoComplete() {
        val ctx = autoCompleteCategorie.context

        // Crea una lista solo per il menu a tendina
        val dropdownList = categorieList.toMutableList()

        if (!dropdownList.contains("Aggiungi Categoria")) {
            dropdownList.add("Aggiungi Categoria")
        }

        val adapter = ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, dropdownList)
        autoCompleteCategorie.setAdapter(adapter)

        autoCompleteCategorie.setOnItemClickListener { parent, _, position, _ ->
            val categoriaSelezionata = parent.getItemAtPosition(position).toString()
            if (categoriaSelezionata == "Aggiungi Categoria") {
                mostraDialogAggiungiCategoria()
                autoCompleteCategorie.setText("") // evita selezione involontaria
            }
        }
    }

    private fun mostraDialogAggiungiCategoria() {
        val ctx = autoCompleteCategorie.context
        val builder = AlertDialog.Builder(ctx)
        builder.setTitle("Nuova Categoria")

        val input = EditText(ctx).apply {
            hint = "Nome categoria"
        }
        builder.setView(input)

        builder.setPositiveButton("Aggiungi") { dialogInterface, _ ->
            val nuovaCategoria = input.text.toString()
                .trim()
                .replaceFirstChar { it.uppercaseChar() }

            if (nuovaCategoria.isNotBlank()) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton
                val nuovaCategoriaMap = hashMapOf("titolo" to nuovaCategoria)

                db.collection("Utenti")
                    .document(userId)
                    .collection("categorie")
                    .add(nuovaCategoriaMap)
                    .addOnSuccessListener {
                        Toast.makeText(ctx, "Categoria aggiunta: $nuovaCategoria", Toast.LENGTH_SHORT).show()
                        caricaCategorie()
                        autoCompleteCategorie.setText(nuovaCategoria, false)
                    }
                    .addOnFailureListener {
                        Toast.makeText(ctx, "Errore nel salvataggio della categoria", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(ctx, "Il nome della categoria non può essere vuoto", Toast.LENGTH_SHORT).show()
            }

            dialogInterface.dismiss()
        }

        builder.setNegativeButton("Annulla") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        builder.show()
    }

    private fun mostraAnteprime(container: LinearLayout) {
        container.removeAllViews()
        for (uri in imageUris) {
            val imageView = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                    setMargins(8, 0, 8, 0)
                }
                setImageURI(uri)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            container.addView(imageView)
        }
    }
    fun salvaSpesaConImmagini(
        spesaMap: HashMap<String, Any>,
        onComplete: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageUrls = mutableListOf<String>()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        if (imageUris.isEmpty()) {
            // Nessuna immagine da caricare
            db.collection("Spese").add(spesaMap)
                .addOnSuccessListener { ref -> onComplete(ref.id) }
                .addOnFailureListener(onError)
            return
        }

        for ((i, uri) in imageUris.withIndex()) {
            val path = "spese/$uid/${System.currentTimeMillis()}_$i.jpg"
            val imageRef = storageRef.child(path)

            imageRef.putFile(uri).continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception ?: Exception("Upload fallito")
                imageRef.downloadUrl
            }.addOnSuccessListener { downloadUrl ->
                imageUrls.add(downloadUrl.toString())
                if (imageUrls.size == imageUris.size) {
                    spesaMap["immagini"] = imageUrls
                    db.collection("Spese").add(spesaMap)
                        .addOnSuccessListener { ref -> onComplete(ref.id) }
                        .addOnFailureListener(onError)
                }
            }.addOnFailureListener(onError)
        }
    }
    private fun mostraSceltaFotoDialog() {
        val opzioni = arrayOf("Scatta una foto", "Scegli dalla galleria")

        AlertDialog.Builder(requireContext())
            .setTitle("Aggiungi Foto")
            .setItems(opzioni) { _, which ->
                when (which) {
                    0 -> richiediPermessoFotocamera()
                    1 -> richiediPermessoGalleria()
                }
            }
            .show()
    }

    private fun richiediPermessoGalleria() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), 1001)
        } else {
            launcherGalleria.launch("image/*")
        }
    }


    private fun richiediPermessoFotocamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 1002)
        } else {
            avviaCamera()
        }
    }

    private fun avviaCamera() {
        val fotoFile = File.createTempFile("spesa_", ".jpg", requireContext().cacheDir)
        fileFotoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", fotoFile)
        launcherCamera.launch(fileFotoUri)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launcherGalleria.launch("image/*")
        } else if (requestCode == 1002 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            avviaCamera()
        } else {
            Toast.makeText(requireContext(), "Permesso negato!", Toast.LENGTH_SHORT).show()
        }
    }
}



