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

private val PREFERENZE_CATEGORIE = "PreferenzeCategorie"
private val KEY_CATEGORIE = "categorie"

class AggiungiSpesaFragment : Fragment(R.layout.fragment_aggiungi_spesa) {

    private lateinit var autoCompleteCategorie: AutoCompleteTextView
    private val categorieList = mutableListOf(
        "Alimentari", "Trasporti", "Intrattenimento", "Salute", "Aggiungi Categoria"
    )

    private lateinit var callback: OnSpesaAggiuntaListener
    private lateinit var db: FirebaseFirestore

    interface OnSpesaAggiuntaListener {
        fun onSpesaAggiunta(spesa: Spesa)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSpesaAggiuntaListener) {
            callback = context
            Log.d("AggiungiSpesaFragment", "Callback collegata correttamente")
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

        // Gestione della selezione nell'AutoCompleteTextView
        autoCompleteCategorie.setOnItemClickListener { parent, _, position, _ ->
            val categoriaSelezionata = parent.getItemAtPosition(position).toString()
            if (categoriaSelezionata == "Aggiungi Categoria") {
                mostraDialogAggiungiCategoria()
            } else {
                autoCompleteCategorie.setText(categoriaSelezionata)
            }
        }

        // Riferimenti agli elementi del layout
        val titoloSpesa = view.findViewById<EditText>(R.id.titoloSpesa)
        val descrizioneSpesa = view.findViewById<EditText>(R.id.descrizioneSpesa)
        val dataSpesa = view.findViewById<EditText>(R.id.DataSelezionata)
        val importoSpesa = view.findViewById<EditText>(R.id.importoSpesa)
        val categoriaSpesa = view.findViewById<AutoCompleteTextView>(R.id.categoriaSpesa)
        val btnConferma = view.findViewById<Button>(R.id.btnConfermaSpesa)

        // Popola l'AutoCompleteTextView con le categorie
        val categorie = resources.getStringArray(R.array.categorie_spesa)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categorie)
        categoriaSpesa.setAdapter(adapter)

        // Mostra il menu a tendina al clic
        categoriaSpesa.setOnClickListener {
            categoriaSpesa.showDropDown()
        }

        var giorno = 0
        var mese = 0
        var anno = 0

        // Selezione della data tramite DatePickerDialog
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

        // Azione al clic del pulsante "Conferma Spesa"
        btnConferma.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val uid = currentUser?.uid

            if (uid == null) {
                Toast.makeText(requireContext(), "Utente non loggato", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Raccolta dei dati inseriti dall'utente
            val titolo = titoloSpesa.text.toString()
            val descrizione = descrizioneSpesa.text.toString()
            val importo = importoSpesa.text.toString().toFloatOrNull() ?: 0.0f
            val categoria = categoriaSpesa.text.toString()

            // Verifica campi obbligatori
            if (titolo.isBlank() || importo == 0.0f) {
                Toast.makeText(requireContext(), "Compila almeno il titolo e l'importo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Creazione della mappa della nuova spesa
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

            // Salvataggio della spesa su Firestore
            db.collection("Spese")
                .add(nuovaSpesa)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Spesa Aggiunta Con Successo!", Toast.LENGTH_SHORT).show()
                    callback.onSpesaAggiunta(Spesa(titolo, descrizione, giorno, mese, anno, importo, categoria))

                    // Reindirizza alla SoloActivity dopo l'aggiunta
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
    private fun salvaCategorie() {
        val sharedPreferences = requireContext().getSharedPreferences(PREFERENZE_CATEGORIE, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        // val categorieString = categorieList.joinToString(",")
        editor.putStringSet(KEY_CATEGORIE, categorieList.toSet())
        editor.apply()
    }
    private fun caricaCategorie() {
        val sharedPreferences = requireContext().getSharedPreferences(PREFERENZE_CATEGORIE, Context.MODE_PRIVATE)
        val categorieSalvate = sharedPreferences.getStringSet(KEY_CATEGORIE, null)
        if (categorieSalvate != null) {
            categorieList.clear()
            categorieList.addAll(categorieSalvate)
            if (!categorieList.contains("Aggiungi Categoria")) {
                categorieList.add("Aggiungi Categoria")
            }
        } else {
            // Categorie di default
            categorieList.clear()
            categorieList.addAll(listOf("Alimentari", "Trasporti", "Intrattenimento", "Salute", "Casa", "Altro", "Aggiungi Categoria"))
        }
        aggiornaAutoComplete()
    }

    // Metodo per aggiornare l'AutoCompleteTextView con le categorie
    private fun aggiornaAutoComplete() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categorieList
        )
        autoCompleteCategorie.setAdapter(adapter)
    }
    // Mostra il dialog per aggiungere una nuova categoria
    private fun mostraDialogAggiungiCategoria() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Nuova Categoria")

        val input = EditText(requireContext()).apply {
            hint = "Nome categoria"
        }
        builder.setView(input)

        builder.setPositiveButton("Aggiungi") { dialogInterface, _ ->
            val nuovaCategoria = input.text.toString().trim()
            if (nuovaCategoria.isNotBlank()) {
                if (!categorieList.contains(nuovaCategoria)) {
                    // Aggiungi la nuova categoria prima di "Aggiungi Categoria"
                    categorieList.add(categorieList.size - 1, nuovaCategoria)
                    salvaCategorie()  // Salva le categorie aggiornate
                    aggiornaAutoComplete()
                    autoCompleteCategorie.setText("")  // Pulisce il campo per scegliere manualmente
                    autoCompleteCategorie.showDropDown()  // Mostra il menu aggiornato
                    Toast.makeText(requireContext(), "Categoria aggiunta: $nuovaCategoria", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Categoria già esistente", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Il nome della categoria non può essere vuoto", Toast.LENGTH_SHORT).show()
            }
            dialogInterface.dismiss()
        }

        builder.setNegativeButton("Annulla") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        builder.show()
    }
}

