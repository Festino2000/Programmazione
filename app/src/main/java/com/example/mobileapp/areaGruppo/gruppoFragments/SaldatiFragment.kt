package com.example.mobileapp.areaGruppo.gruppoFragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.example.mobileapp.adapters.SpesaCondivisaAdapter
import com.example.mobileapp.areaGruppo.gruppoDataClasses.SpesaCondivisa
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SaldatiFragment : Fragment(R.layout.fragment_saldati) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SpesaCondivisaAdapter
    private val listaSpeseSaldati = mutableListOf<SpesaCondivisa>()
    private var mappaUtenti: Map<String, String> = emptyMap()

    private lateinit var layoutFiltriAttivi: View
    private lateinit var textViewFiltriAttivi: TextView
    private lateinit var buttonResetFiltri: Button

    // filtri attivi
    private var filtroUtenti: List<String>? = null
    private var filtroPrezzi: List<Pair<Float, Float>>? = null
    private var filtroDataInizio: Calendar? = null
    private var filtroDataFine: Calendar? = null
    private var queryCorrente: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewSaldati)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        layoutFiltriAttivi = view.findViewById(R.id.layoutFiltriAttivi)
        textViewFiltriAttivi = view.findViewById(R.id.textViewFiltriAttivi)
        buttonResetFiltri = view.findViewById(R.id.buttonResetFiltri)
        buttonResetFiltri.setOnClickListener { resettaFiltri() }

        val idGruppo = arguments?.getString("idGruppo") ?: return

        FirebaseFirestore.getInstance().collection("Gruppi")
            .whereEqualTo("idUnico", idGruppo)
            .get()
            .addOnSuccessListener { gruppoResult ->
                val document = gruppoResult.documents.firstOrNull() ?: return@addOnSuccessListener

                val utentiID = document.get("utentiID") as? List<String> ?: return@addOnSuccessListener

                FirebaseFirestore.getInstance().collection("Utenti")
                    .whereIn("utenteID", utentiID)
                    .get()
                    .addOnSuccessListener { utentiDocs ->
                        mappaUtenti = utentiDocs.mapNotNull {
                            val id = it.getString("utenteID")
                            val nick = it.getString("nickname")
                            if (id != null && nick != null) id to nick else null
                        }.toMap()

                        adapter = SpesaCondivisaAdapter(document.id, mappaUtenti)
                        recyclerView.adapter = adapter

                        caricaSpeseSaldati(document.id)
                    }
            }
    }
    fun eseguiRicerca(query: String) {
        queryCorrente = query
        applicaFiltri()
    }

    fun mostraDialogFiltri() {
        mostraDialogFiltro()
    }

    fun resettaTuttiIFiltri() {
        filtroUtenti = null
        filtroPrezzi = null
        filtroDataInizio = null
        filtroDataFine = null
        queryCorrente = ""
        applicaFiltri()
        Toast.makeText(requireContext(), "Filtri ripristinati", Toast.LENGTH_SHORT).show()
    }

    private fun caricaSpeseSaldati(idGruppo: String) {
        val mioId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("Gruppi")
            .document(idGruppo)
            .collection("Spese")
            .get()
            .addOnSuccessListener { result ->
                listaSpeseSaldati.clear()
                listaSpeseSaldati.addAll(
                    result.documents.mapNotNull { doc ->
                        doc.toObject(SpesaCondivisa::class.java)?.apply {
                            idDocumento = doc.id
                        }
                    }.filter { spesa ->
                        if (spesa.creatoreID == mioId) {
                            spesa.pagamentiConfermati.containsAll(spesa.idUtentiCoinvolti)
                        } else {
                            spesa.pagamentiEffettuati.contains(mioId)
                        }
                    }
                )
                applicaFiltri()
            }
    }

    private fun mostraDialogFiltro() {
        val opzioni = arrayOf(
            "Filtra per intervallo di date",
            "Filtra per prezzo",
            "Filtra per utenti coinvolti"
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtra le spese saldate")
            .setItems(opzioni) { _, which ->
                when (which) {
                    0 -> mostraDialogoIntervalloDate()
                    1 -> mostraDialogoPrezzo()
                    2 -> mostraDialogoUtentiCoinvolti()
                }
            }
            .show()
    }

    private fun mostraDialogoIntervalloDate() {
        val calendarInizio = Calendar.getInstance()
        val calendarFine = Calendar.getInstance()

        DatePickerDialog(requireContext(), { _, anno, mese, giorno ->
            calendarInizio.set(anno, mese, giorno)
            DatePickerDialog(requireContext(), { _, a, m, g ->
                calendarFine.set(a, m, g)
                filtroDataInizio = calendarInizio
                filtroDataFine = calendarFine
                applicaFiltri()
            }, calendarFine.get(Calendar.YEAR), calendarFine.get(Calendar.MONTH), calendarFine.get(Calendar.DAY_OF_MONTH)).show()
        }, calendarInizio.get(Calendar.YEAR), calendarInizio.get(Calendar.MONTH), calendarInizio.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun mostraDialogoPrezzo() {
        val opzioni = arrayOf("Meno di 50€", "Da 50€ a 100€", "Più di 100€")
        val checked = booleanArrayOf(false, false, false)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtra per Prezzo")
            .setMultiChoiceItems(opzioni, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton("Applica") { _, _ ->
                val filtri = mutableListOf<Pair<Float, Float>>()
                if (checked[0]) filtri.add(0f to 50f)
                if (checked[1]) filtri.add(50f to 100f)
                if (checked[2]) filtri.add(100f to Float.MAX_VALUE)
                filtroPrezzi = filtri
                applicaFiltri()
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun mostraDialogoUtentiCoinvolti() {
        val nomi = mappaUtenti.values.toTypedArray()
        val idUtenti = mappaUtenti.keys.toList()
        val checked = BooleanArray(nomi.size)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtra per utenti coinvolti")
            .setMultiChoiceItems(nomi, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton("Applica") { _, _ ->
                filtroUtenti = idUtenti.filterIndexed { index, _ -> checked[index] }
                applicaFiltri()
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun resettaFiltri() {
        filtroUtenti = null
        filtroPrezzi = null
        filtroDataInizio = null
        filtroDataFine = null
        queryCorrente = ""
        applicaFiltri()
        Toast.makeText(requireContext(), "Filtri ripristinati", Toast.LENGTH_SHORT).show()
    }

    private fun applicaFiltri() {
        var filtrate = listaSpeseSaldati.toList()

        filtroUtenti?.let { utenti ->
            filtrate = filtrate.filter { spesa ->
                spesa.idUtentiCoinvolti.any { it in utenti }
            }
        }

        filtroPrezzi?.let { ranges ->
            filtrate = filtrate.filter { spesa ->
                ranges.any { (min, max) -> spesa.importo in min..max }
            }
        }

        if (filtroDataInizio != null && filtroDataFine != null) {
            filtrate = filtrate.filter { spesa ->
                val data = Calendar.getInstance().apply { set(spesa.anno, spesa.mese - 1, spesa.giorno) }
                data in filtroDataInizio!!..filtroDataFine!!
            }
        }

        if (queryCorrente.isNotBlank()) {
            filtrate = filtrate.filter {
                it.titolo.contains(queryCorrente, true) || it.descrizione.contains(queryCorrente, true)
            }
        }

        adapter.submitList(filtrate)

        val filtri = mutableListOf<String>()
        if (filtroUtenti != null) filtri.add("Utenti")
        if (filtroPrezzi != null) filtri.add("Prezzo")
        if (filtroDataInizio != null && filtroDataFine != null) filtri.add("Data")
        if (queryCorrente.isNotBlank()) filtri.add("Testo")

        if (filtri.isEmpty()) {
            layoutFiltriAttivi.visibility = View.GONE
        } else {
            textViewFiltriAttivi.text = "Filtri attivi: ${filtri.joinToString()}"
            layoutFiltriAttivi.visibility = View.VISIBLE
        }
    }
}
