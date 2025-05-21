package com.example.mobileapp.areaPersonale

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import com.example.mobileapp.R

class AggiungiSpesaFragment : Fragment(R.layout.fragment_aggiungi_spesa) {

    private lateinit var autoCompleteCategorie: AutoCompleteTextView
    private val categorieList = mutableListOf<String>()
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_aggiungi_spesa, container, false)

        autoCompleteCategorie = view.findViewById(R.id.categoriaSpesa)
        caricaCategorie()

        autoCompleteCategorie.setOnItemClickListener { parent, _, position, _ ->
            val categoriaSelezionata = parent.getItemAtPosition(position).toString()
            if (categoriaSelezionata == "Aggiungi Categoria") {
                mostraDialogAggiungiCategoria()
            } else {
                autoCompleteCategorie.setText(categoriaSelezionata)
            }
        }

        val titoloSpesa = view.findViewById<EditText>(R.id.titoloSpesa)
        val descrizioneSpesa = view.findViewById<EditText>(R.id.descrizioneSpesa)
        val dataSpesa = view.findViewById<EditText>(R.id.DataSelezionata)
        val importoSpesa = view.findViewById<EditText>(R.id.importoSpesa)
        val categoriaSpesa = view.findViewById<AutoCompleteTextView>(R.id.categoriaSpesa)
        val btnConferma = view.findViewById<Button>(R.id.btnConfermaSpesa)

        categoriaSpesa.setOnClickListener {
            categoriaSpesa.showDropDown()
        }

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

            val nuovaSpesa = hashMapOf(
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

            db.collection("Spese")
                .add(nuovaSpesa)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Spesa Aggiunta Con Successo!", Toast.LENGTH_SHORT).show()
                    callback.onSpesaAggiunta(Spesa(titolo, descrizione, giorno, mese, anno, importo, categoria))
                    val intent = Intent(requireContext(), SoloActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Errore nel salvataggio su Firestore", Toast.LENGTH_SHORT).show()
                    Log.e("AggiungiSpesaFragment", "Errore nel salvataggio su Firestore", e)
                }
        }

        return view
    }

    private fun caricaCategorie() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        categorieList.clear()
        categorieList.addAll(listOf("Alimentari", "Trasporti", "Svago", "Abbigliamento", "Casa"))

        db.collection("utenti")
            .document(userId)
            .collection("categorie")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    doc.getString("nome")?.let {
                        if (!categorieList.contains(it)) {
                            categorieList.add(it)
                        }
                    }
                }
                if (!categorieList.contains("Aggiungi Categoria")) {
                    categorieList.add("Aggiungi Categoria")
                }
                aggiornaAutoComplete()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Errore nel caricamento categorie", e)
            }
    }

    private fun aggiornaAutoComplete() {
        val ctx = autoCompleteCategorie.context
        val adapter = ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, categorieList)
        autoCompleteCategorie.setAdapter(adapter)
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
            val nuovaCategoria = input.text.toString().trim()
            if (nuovaCategoria.isNotBlank()) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton
                val nuovaCategoriaMap = hashMapOf("nome" to nuovaCategoria)

                db.collection("utenti")
                    .document(userId)
                    .collection("categorie")
                    .add(nuovaCategoriaMap)
                    .addOnSuccessListener {
                        Toast.makeText(ctx, "Categoria aggiunta: $nuovaCategoria", Toast.LENGTH_SHORT).show()
                        caricaCategorie()
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


