package com.example.mobileapp.areaPersonale.singoloFragments

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.mobileapp.R
import com.example.mobileapp.areaPersonale.singoloDataClasses.Spesa
import com.example.mobileapp.areaPersonale.singoloActivities.SoloActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.util.*
import com.example.mobileapp.areaPersonale.singoloDataClasses.SpesaLocale

// Fragment che gestisce l'aggiunta (o modifica) di una spesa
class AggiungiSpesaFragment : Fragment(R.layout.fragment_aggiungi_spesa) {

    // AutoCompleteTextView per le categorie di spesa
    private lateinit var autoCompleteCategorie: AutoCompleteTextView
    // Lista delle categorie
    private val categorieList = mutableListOf<String>()
    // Launcher per la selezione di immagini dalla galleria
    private lateinit var launcherGalleria: ActivityResultLauncher<String>
    // Launcher per scattare una foto con la fotocamera
    private lateinit var launcherCamera: ActivityResultLauncher<Uri>
    // Uri dove verrà salvata la foto scattata
    private lateinit var fileFotoUri: Uri
    // Callback per notificare la spesa aggiunta
    private lateinit var callback: OnSpesaAggiuntaListener
    // Istanza del database Firestore
    private lateinit var db: FirebaseFirestore

    // Interfaccia per comunicare la spesa aggiunta all'activity
    interface OnSpesaAggiuntaListener {
        fun onSpesaAggiunta(spesa: Spesa)
    }

    // Viene chiamato quando il fragment viene attaccato alla activity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSpesaAggiuntaListener) {
            callback = context
        } else {
            throw RuntimeException("$context deve implementare OnSpesaAggiuntaListener")
        }
        db = FirebaseFirestore.getInstance()
    }

    // Lista degli URI delle immagini associate alla spesa
    private val imageUris = mutableListOf<Uri>()

    // Metodo principale per la creazione della view del fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_aggiungi_spesa, container, false)

        // Pulsante per aggiungere foto
        val btnAggiungiFoto = view.findViewById<Button>(R.id.btnAggiungiFoto)
        // Layout dove verranno mostrate le anteprime delle immagini
        val layoutGalleria = view.findViewById<LinearLayout>(R.id.layoutGalleria)

        // Launcher per selezionare più immagini dalla galleria
        launcherGalleria = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            uris.forEach { uri ->
                try {
                    // Richiede il permesso permanente per leggere l'immagine
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    Log.w("SpesaDebug", "Permission persist failed: $uri", e)
                }
            }
            imageUris.addAll(uris)
            mostraAnteprime(layoutGalleria)
        }

        // Launcher per scattare una foto con la fotocamera
        launcherCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageUris.add(fileFotoUri)
                mostraAnteprime(layoutGalleria)
            }
        }

        // Al click sul bottone, mostra il dialog per scegliere come aggiungere foto
        btnAggiungiFoto.setOnClickListener {
            mostraSceltaFotoDialog()
        }

        // Inizializza l'AutoCompleteTextView delle categorie e carica le categorie
        autoCompleteCategorie = view.findViewById(R.id.categoriaSpesa)
        caricaCategorie()

        // Trova i vari campi di input nella view
        val titoloSpesa = view.findViewById<EditText>(R.id.titoloSpesa)
        val descrizioneSpesa = view.findViewById<EditText>(R.id.descrizioneSpesa)
        val dataSpesa = view.findViewById<EditText>(R.id.DataSelezionata)
        val importoSpesa = view.findViewById<EditText>(R.id.importoSpesa)
        val categoriaSpesa = view.findViewById<AutoCompleteTextView>(R.id.categoriaSpesa)
        val btnConferma = view.findViewById<Button>(R.id.btnConfermaSpesa)

        // Mostra il dropdown delle categorie quando si clicca
        categoriaSpesa.setOnClickListener { categoriaSpesa.showDropDown() }

        // Variabili per la data selezionata
        var giorno = 0
        var mese = 0
        var anno = 0

        // Gestione del click per selezionare la data tramite DatePickerDialog
        dataSpesa.setOnClickListener {
            val calendario = Calendar.getInstance()
            anno = calendario.get(Calendar.YEAR)
            mese = calendario.get(Calendar.MONTH)
            giorno = calendario.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                giorno = selectedDay
                mese = selectedMonth + 1 // I mesi partono da 0
                anno = selectedYear
                val dataFormattata = "$giorno/$mese/$anno"
                dataSpesa.setText(dataFormattata)
            }, anno, mese, giorno).show()
        }

        // Se il fragment viene aperto per modificare una spesa, carica i dati passati nei campi
        arguments?.let { args ->
            titoloSpesa.setText(args.getString("titolo", ""))
            descrizioneSpesa.setText(args.getString("descrizione", ""))
            importoSpesa.setText(args.getFloat("importo", 0f).toString())
            autoCompleteCategorie.setText(args.getString("categoria", ""), false)
            giorno = args.getInt("giorno", 0)
            mese = args.getInt("mese", 0)
            anno = args.getInt("anno", 0)
            if (giorno != 0 && mese != 0 && anno != 0) {
                dataSpesa.setText("$giorno/$mese/$anno")
            }
        }

        // Gestione del click sul bottone di conferma per aggiungere/modificare la spesa
        btnConferma.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val uid = currentUser?.uid ?: return@setOnClickListener

            val titolo = titoloSpesa.text.toString()
            val descrizione = descrizioneSpesa.text.toString()
            val importo = importoSpesa.text.toString().toFloatOrNull() ?: 0.0f
            val categoria = categoriaSpesa.text.toString()

            // Controlla che titolo e importo siano compilati
            if (titolo.isBlank() || importo == 0.0f) {
                Toast.makeText(requireContext(), "Compila almeno il titolo e l'importo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mappa con i dati da salvare su Firestore
            val spesaMap = hashMapOf(
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

            if (documentId != null) {
                // MODIFICA di una spesa esistente
                db.collection("Spese").document(documentId)
                    .set(spesaMap)
                    .addOnSuccessListener {
                        // Aggiorna la Room locale (database locale)
                        val spesaLocale = SpesaLocale(
                            id = documentId,
                            immagini = imageUris.mapNotNull { it.toString() }
                        )

                        Thread {
                            try {
                                AppDatabase.getDatabase(requireContext()).spesaDao().inserisci(spesaLocale)
                            } catch (e: Exception) {
                                Log.e("SpesaDebug", "Errore salvataggio Room", e)
                            }
                        }.start()

                        // Crea oggetto Spesa e notifica il callback
                        val nuovaSpesa = Spesa(
                            titolo = titolo,
                            descrizione = descrizione,
                            giorno = giorno,
                            mese = mese,
                            anno = anno,
                            importo = importo,
                            categoria = categoria,
                            id = documentId
                        )

                        callback.onSpesaAggiunta(nuovaSpesa)
                        startActivity(Intent(requireContext(), SoloActivity::class.java))
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Errore nella modifica", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // INSERIMENTO di una nuova spesa
                db.collection("Spese").add(spesaMap)
                    .addOnSuccessListener { docRef ->
                        val spesaLocale = SpesaLocale(
                            id = docRef.id,
                            immagini = imageUris.mapNotNull { it.toString() }
                        )

                        Thread {
                            try {
                                AppDatabase.getDatabase(requireContext()).spesaDao().inserisci(spesaLocale)
                            } catch (e: Exception) {
                                Log.e("SpesaDebug", "Errore salvataggio Room", e)
                            }
                        }.start()

                        val nuovaSpesa = Spesa(
                            titolo = titolo,
                            descrizione = descrizione,
                            giorno = giorno,
                            mese = mese,
                            anno = anno,
                            importo = importo,
                            categoria = categoria,
                            id = docRef.id
                        )

                        callback.onSpesaAggiunta(nuovaSpesa)
                        startActivity(Intent(requireContext(), SoloActivity::class.java))
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Errore nel salvataggio su Firestore", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        return view
    }

    // Mostra un dialog per scegliere come aggiungere una foto (scattare o galleria)
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

    // Richiede il permesso per accedere alla galleria e lancia il picker
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

    // Richiede il permesso per la fotocamera e avvia la fotocamera se già concesso
    private fun richiediPermessoFotocamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 1002)
        } else {
            avviaCamera()
        }
    }

    // Avvia la fotocamera per scattare una foto e salva l'immagine in un file temporaneo
    private fun avviaCamera() {
        val fotoFile = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "spesa_${System.currentTimeMillis()}.jpg"
        )
        fileFotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            fotoFile
        )
        launcherCamera.launch(fileFotoUri)
    }

    // Mostra le anteprime delle immagini scelte/scattate nel layout apposito
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

    // Carica le categorie dalla lista predefinita e da Firestore
    private fun caricaCategorie() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        categorieList.clear()
        categorieList.addAll(listOf("Alimentari", "Trasporti", "Svago", "Abbigliamento", "Casa"))

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
                aggiornaAutoComplete()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Errore nel caricamento categorie", e)
            }
    }

    // Aggiorna l'adapter dell'AutoCompleteTextView delle categorie
    private fun aggiornaAutoComplete() {
        val ctx = autoCompleteCategorie.context
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
                autoCompleteCategorie.setText("")
            }
        }
    }

    // Mostra un dialog per aggiungere una nuova categoria personalizzata
    private fun mostraDialogAggiungiCategoria() {
        val ctx = autoCompleteCategorie.context
        val builder = AlertDialog.Builder(ctx)
        builder.setTitle("Nuova Categoria")

        val input = EditText(ctx).apply {
            hint = "Nome categoria"
        }
        builder.setView(input)

        builder.setPositiveButton("Aggiungi") { dialogInterface, _ ->
            val nuovaCategoria = input.text.toString().trim().replaceFirstChar { it.uppercaseChar() }

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
}

