package com.example.mobileapp.areaGruppo

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class AggiungiSpesaDialog(
    private val utentiGruppo: List<Utente>,
    private val onSpesaAggiunta: (SpesaCondivisa) -> Unit
) : DialogFragment() {

    private lateinit var editTitolo: EditText
    private lateinit var editDescrizione: EditText
    private lateinit var editImporto: EditText
    private lateinit var textData: TextView
    private lateinit var recyclerUtenti: RecyclerView
    private lateinit var adapter: UtenteCheckboxAdapter

    private var giorno = 0
    private var mese = 0
    private var anno = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_aggiungi_spesa, null)

        editTitolo = view.findViewById(R.id.editTitolo)
        editDescrizione = view.findViewById(R.id.editDescrizione)
        editImporto = view.findViewById(R.id.editImporto)
        textData = view.findViewById(R.id.textData)
        recyclerUtenti = view.findViewById(R.id.recyclerUtenti)

        adapter = UtenteCheckboxAdapter(utentiGruppo)
        recyclerUtenti.layoutManager = LinearLayoutManager(requireContext())
        recyclerUtenti.adapter = adapter

        val calendario = Calendar.getInstance()
        giorno = calendario.get(Calendar.DAY_OF_MONTH)
        mese = calendario.get(Calendar.MONTH) + 1
        anno = calendario.get(Calendar.YEAR)

        textData.text = "$giorno/$mese/$anno"
        textData.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                giorno = d
                mese = m + 1
                anno = y
                textData.text = "$giorno/$mese/$anno"
            }, anno, mese - 1, giorno).show()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Aggiungi Spesa")
            .setPositiveButton("Aggiungi") { _, _ ->
                val titolo = editTitolo.text.toString()
                val descrizione = editDescrizione.text.toString()
                val importo = editImporto.text.toString().toDoubleOrNull() ?: 0.0

                val mioId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                val utentiSelezionati = adapter.getUtentiSelezionati()
                    .map { it.utenteID }
                    .filter { it != mioId } // Rimuove il creatore dalla lista

                if (titolo.isNotBlank() && importo > 0 && utentiSelezionati.isNotEmpty()) {
                    val spesa = SpesaCondivisa(
                        titolo = titolo,
                        descrizione = descrizione,
                        giorno = giorno,
                        mese = mese,
                        anno = anno,
                        importo = importo,
                        idUtentiCoinvolti = utentiSelezionati,
                        creatoreID = mioId,
                        pagamentiEffettuati = mutableListOf(),
                        pagamentiConfermati = mutableListOf()
                    )
                    onSpesaAggiunta(spesa)
                } else {
                    Toast.makeText(requireContext(), "Compila tutti i campi obbligatori", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annulla", null)
            .create()
    }
}
