package com.example.mobileapp.areaPersonale

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.example.mobileapp.SpacingItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SoloActivity : AppCompatActivity(), AggiungiSpesaFragment.OnSpesaAggiuntaListener {

    private val listaSpese = mutableListOf<Spesa>()
    private lateinit var db: FirebaseFirestore
    private lateinit var btnAggiungiSpesa: Button
    private lateinit var viewModel: SpeseViewModel
    private lateinit var adapter: SpeseAdapter
    private var filtroAttivo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo)

        // Inizializzazione del database e della toolbar
        db = FirebaseFirestore.getInstance()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Setup della RecyclerView con layout a griglia e decorazione per il margine tra gli item
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewSpese)
        adapter = SpeseAdapter()
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.item_spacing)
        recyclerView.addItemDecoration(SpacingItemDecoration(spacingInPixels, 2))

        // Inizializzazione del ViewModel
        viewModel = ViewModelProvider(this).get(SpeseViewModel::class.java)
        viewModel.caricaTutteLeSpese()

        // Osservazione delle spese aggiornate
        viewModel.spese.observe(this) { spese ->
            adapter.submitList(spese)
        }

        // Pulsante per aprire il fragment di aggiunta spesa
        btnAggiungiSpesa = findViewById(R.id.btnAggiungiSpesa)
        btnAggiungiSpesa.setOnClickListener {
            Log.d("SoloActivity", "Pulsante Aggiungi Spesa cliccato")
            apriAggiungiSpesaFragment()
        }
    }

    private fun apriAggiungiSpesaFragment() {
        btnAggiungiSpesa.visibility = View.GONE
        findViewById<FrameLayout>(R.id.fragment_container).visibility = View.VISIBLE
        val fragment = AggiungiSpesaFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, "AGGIUNGI_SPESA_FRAGMENT")
            .addToBackStack(null)
            .commit()
    }

    override fun onSpesaAggiunta(spesa: Spesa) {
        listaSpese.add(spesa)
        Toast.makeText(this, "Spesa aggiunta: ${spesa.titolo}", Toast.LENGTH_SHORT).show()
    }

    // Setup del menu ricerca
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = "Cerca per titolo o categoria..."

        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filtraSpese(newText?.trim()?.lowercase() ?: "")
                return true
            }
        })

        searchView.setOnCloseListener {
            viewModel.spese.value?.let { adapter.submitList(it.toList()) }
            false
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                mostraDialogFiltro()
                true
            }
            R.id.action_stats -> {
                mostraPopupStatistiche()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun filtraSpese(query: String) {
        val tutteLeSpese = viewModel.spese.value ?: return
        val speseFiltrate = tutteLeSpese.filter {
            it.titolo.contains(query, ignoreCase = true) ||
                    it.categoria.contains(query, ignoreCase = true) ||
                    it.importo.toString().contains(query)
        }
        adapter.submitList(speseFiltrate)
        if (speseFiltrate.isEmpty()) {
            Toast.makeText(this, "Nessuna spesa trovata", Toast.LENGTH_SHORT).show()
        }
    }

    // Mostra dialog con opzioni di filtro
    private fun mostraDialogFiltro() {
        val opzioni = arrayOf(
            "Filtra per categoria",
            "Filtra per prezzo",
            "Ordina per Titolo (A-Z)",
            "Filtra per intervallo di date"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Filtra le spese")
            .setItems(opzioni) { _, which ->
                when (which) {
                    0 -> mostraDialogoCategorie()
                    1 -> mostraDialogoPrezzo()
                    2 -> ordinaPerTitolo()
                    3 -> mostraDialogoIntervalloDate()
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }


    // Dialogo per selezione categoria
    private fun mostraDialogoCategorie() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val predefinite = listOf("Alimentari", "Trasporti", "Svago", "Abbigliamento", "Casa")

        db.collection("utenti").document(userId).collection("categorie")
            .get()
            .addOnSuccessListener { result ->
                val personalizzate = result.mapNotNull { it.getString("nome") }.distinct()
                val tutteLeCategorie = (predefinite + personalizzate).distinct()
                val checkedItems = BooleanArray(tutteLeCategorie.size) { false }

                MaterialAlertDialogBuilder(this)
                    .setTitle("Filtra per categoria")
                    .setMultiChoiceItems(
                        tutteLeCategorie.toTypedArray(),
                        checkedItems
                    ) { _, which, isChecked ->
                        checkedItems[which] = isChecked
                    }
                    .setPositiveButton("Applica") { _, _ ->
                        val selezionate = tutteLeCategorie.filterIndexed { i, _ -> checkedItems[i] }
                        filtraPerCategorie(selezionate)
                    }
                    .setNegativeButton("Annulla", null)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Errore nel caricamento delle categorie", Toast.LENGTH_SHORT)
                    .show()
            }
        }


    // Ordina per data all'interno di un intervallo selezionato
    private fun mostraDialogoOrdinamentoDate(inizio: Calendar, fine: Calendar) {
        val opzioni = arrayOf("Ordina dal più vecchio al più recente", "Ordina dal più recente al più vecchio")
        MaterialAlertDialogBuilder(this)
            .setTitle("Ordina spese per data")
            .setItems(opzioni) { _, which ->
                val descending = which == 1
                filtraPerIntervalloDate(inizio, fine, descending)
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    // Mostra i due date picker per l'intervallo
    private fun mostraDialogoIntervalloDate() {
        val calendarInizio = Calendar.getInstance()
        val calendarFine = Calendar.getInstance()

        DatePickerDialog(this, { _, annoInizio, meseInizio, giornoInizio ->
            calendarInizio.set(annoInizio, meseInizio, giornoInizio)

            DatePickerDialog(this, { _, annoFine, meseFine, giornoFine ->
                calendarFine.set(annoFine, meseFine, giornoFine)
                mostraDialogoOrdinamentoDate(calendarInizio, calendarFine)
            }, calendarFine.get(Calendar.YEAR), calendarFine.get(Calendar.MONTH), calendarFine.get(Calendar.DAY_OF_MONTH)).show()

        }, calendarInizio.get(Calendar.YEAR), calendarInizio.get(Calendar.MONTH), calendarInizio.get(Calendar.DAY_OF_MONTH)).show()
    }

    // Dialogo per selezione intervallo prezzo con checkbox
    private fun mostraDialogoPrezzo() {
        val opzioni = arrayOf(
            "Meno di 50€",
            "Da 50€ a 100€",
            "Più di 100€"
        )
        val checkedItems = booleanArrayOf(false, false, false)

        MaterialAlertDialogBuilder(this)
            .setTitle("Filtra per Prezzo")
            .setMultiChoiceItems(opzioni, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Applica") { _, _ ->
                val filtri = mutableListOf<Pair<Float, Float>>()

                if (checkedItems[0]) filtri.add(0f to 50f)
                if (checkedItems[1]) filtri.add(50f to 100f)  // correzione 1000f → 100f
                if (checkedItems[2]) filtri.add(100f to Float.MAX_VALUE)

                filtraPerPrezzo(filtri)
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    // Filtro per categorie selezionate
    private fun filtraPerCategorie(categorie: List<String>) {
        val spese = viewModel.spese.value ?: return
        val speseFiltrate = spese.filter { categorie.contains(it.categoria) }
        adapter.submitList(speseFiltrate)
        if (speseFiltrate.isEmpty()) {
            Toast.makeText(this, "Nessuna spesa trovata per le categorie selezionate", Toast.LENGTH_SHORT).show()
        }
    }
    private fun filtraPerPrezzo(rangeList: List<Pair<Float, Float>>) {
        val spese = viewModel.spese.value ?: return
        val speseFiltrate = spese.filter { spesa ->
            rangeList.any { (min, max) -> spesa.importo in min..max }
        }

        adapter.submitList(speseFiltrate)
        if (speseFiltrate.isEmpty()) {
            Toast.makeText(this, "Nessuna spesa trovata per i prezzi selezionati", Toast.LENGTH_SHORT).show()
        }
        filtroAttivo = true
    }

    // Ordina per titolo
    private fun ordinaPerTitolo() {
        val spese = viewModel.spese.value ?: return
        adapter.submitList(spese.sortedBy { it.titolo.lowercase() })
    }

    // Filtro per data con ordinamento crescente/decrescente
    private fun filtraPerIntervalloDate(inizio: Calendar, fine: Calendar, descending: Boolean) {
        val spese = viewModel.spese.value ?: return
        val speseFiltrate = spese.filter {
            val dataSpesa = Calendar.getInstance().apply {
                set(it.anno, it.mese - 1, it.giorno)
            }
            dataSpesa in inizio..fine
        }.let { lista ->
            if (descending)
                lista.sortedByDescending {
                    Calendar.getInstance().apply { set(it.anno, it.mese - 1, it.giorno) }.timeInMillis
                }
            else
                lista.sortedBy {
                    Calendar.getInstance().apply { set(it.anno, it.mese - 1, it.giorno) }.timeInMillis
                }
        }
        adapter.submitList(speseFiltrate)
        if (speseFiltrate.isEmpty()) {
            Toast.makeText(this, "Nessuna spesa trovata in questo intervallo", Toast.LENGTH_SHORT).show()
        }
    }
    private fun mostraPopupStatistiche() {
        val spese = viewModel.spese.value ?: return

        // Totale complessivo
        var totaleComplessivo = 0.0
        for (spesa in spese) {
            totaleComplessivo += spesa.importo
        }

        // Totale per categoria
        val totalePerCategoria = mutableMapOf<String, Double>()
        for (spesa in spese) {
            val categoria = spesa.categoria
            totalePerCategoria[categoria] = totalePerCategoria.getOrDefault(categoria, 0.0) + spesa.importo
        }

        // Totale ultimi 3 mesi
        val oggi = Calendar.getInstance()
        val treMesiFa = Calendar.getInstance().apply { add(Calendar.MONTH, -3) }
        var totaleUltimi3Mesi = 0.0

        for (spesa in spese) {
            val dataSpesa = Calendar.getInstance().apply {
                set(spesa.anno, spesa.mese - 1, spesa.giorno)
            }
            if (dataSpesa >= treMesiFa && dataSpesa <= oggi) {
                totaleUltimi3Mesi += spesa.importo
            }
        }

        // Costruzione del messaggio
        val sb = StringBuilder()
        sb.append("Statistiche Spese\n\n")
        sb.append("Totale complessivo: €%.2f\n".format(totaleComplessivo))
        sb.append("Totale ultimi 3 mesi: €%.2f\n\n".format(totaleUltimi3Mesi))
        sb.append("Totale per categoria:\n")
        for ((categoria, totale) in totalePerCategoria) {
            sb.append("$categoria: €%.2f\n".format(totale))
        }

        // Mostra AlertDialog
        AlertDialog.Builder(this)
            .setTitle("Statistiche")
            .setMessage(sb.toString())
            .setPositiveButton("OK", null)
            .show()
    }
}
