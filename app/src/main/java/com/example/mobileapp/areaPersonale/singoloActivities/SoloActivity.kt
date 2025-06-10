package com.example.mobileapp.areaPersonale.singoloActivities

// Import delle librerie Android e di classi personalizzate
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.example.mobileapp.SpacingItemDecoration
import com.example.mobileapp.adapters.SpeseAdapter
import com.example.mobileapp.areaPersonale.singoloFragments.AggiungiSpesaFragment
import com.example.mobileapp.areaPersonale.singoloDataClasses.Spesa
import com.example.mobileapp.viewModels.SpeseViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Activity principale per la gestione delle spese personali
class SoloActivity : AppCompatActivity(),
    AggiungiSpesaFragment.OnSpesaAggiuntaListener {

    // Lista che contiene le spese
    private val listaSpese = mutableListOf<Spesa>()
    private lateinit var db: FirebaseFirestore
    private lateinit var btnAggiungiSpesa: Button
    private lateinit var viewModel: SpeseViewModel
    private lateinit var adapter: SpeseAdapter
    private lateinit var textImportoTotale: TextView

    // Variabili per filtri e ordinamenti
    private var categorieFiltrate: List<String>? = null
    private var rangePrezzoFiltrato: List<Pair<Float, Float>>? = null
    private var intervalloDateInizio: Calendar? = null
    private var intervalloDateFine: Calendar? = null
    private var ordineDataDescrescente: Boolean = false
    private var ordinaPerTitoloAttivo: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo)

        val btnRipristina = findViewById<Button>(R.id.btnRipristinaFiltri)
        textImportoTotale = findViewById(R.id.textImportoTotale)

        // Listener per il bottone di ripristino filtri
        btnRipristina.setOnClickListener {
            // Reset dei filtri
            categorieFiltrate = null
            rangePrezzoFiltrato = null
            intervalloDateInizio = null
            intervalloDateFine = null
            ordineDataDescrescente = false
            ordinaPerTitoloAttivo = false

            // Ricarica lista completa
            viewModel.spese.value?.let {
                adapter.submitList(it.toList())
            }

            // Nasconde il bottone
            btnRipristina.visibility = View.GONE
        }

        // Inizializzazione database e toolbar
        db = FirebaseFirestore.getInstance()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Setup RecyclerView con layout a griglia
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewSpese)
        adapter = SpeseAdapter(
            onModificaSpesa = { spesa -> apriModificaSpesaFragment(spesa) },
            onEliminaSpesa = { spesa -> mostraDialogConfermaEliminazione(spesa) }
        )
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.item_spacing)
        recyclerView.addItemDecoration(SpacingItemDecoration(spacingInPixels, 2))

        // Inizializzazione ViewModel e caricamento spese
        viewModel = ViewModelProvider(this).get(SpeseViewModel::class.java)
        viewModel.caricaTutteLeSpese()

        // Osserva aggiornamenti delle spese
        viewModel.spese.observe(this) { spese ->
            adapter.submitList(spese)
            aggiornaImportoTotale(spese)
        }

        // Pulsante per aggiungere una spesa
        btnAggiungiSpesa = findViewById(R.id.btnAggiungiSpesa)
        btnAggiungiSpesa.setOnClickListener {
            Log.d("SoloActivity", "Pulsante Aggiungi Spesa cliccato")
            apriAggiungiSpesaFragment()
        }
    }

    // Apre il fragment per aggiungere una nuova spesa
    private fun apriAggiungiSpesaFragment() {
        btnAggiungiSpesa.visibility = View.GONE
        findViewById<FrameLayout>(R.id.fragment_container).visibility = View.VISIBLE
        val fragment = AggiungiSpesaFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, "AGGIUNGI_SPESA_FRAGMENT")
            .addToBackStack(null)
            .commit()
    }

    // Callback quando una nuova spesa è stata aggiunta
    override fun onSpesaAggiunta(spesa: Spesa) {
        listaSpese.add(spesa)
        Toast.makeText(this, "Spesa aggiunta: ${spesa.titolo}", Toast.LENGTH_SHORT).show()
    }

    // Setup del menu di ricerca
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

    // Gestione delle selezioni nel menu
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

    // Filtra la lista delle spese in base alla query testuale
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

    // Mostra dialog per scegliere i filtri da applicare
    private fun mostraDialogFiltro() {
        val opzioni = arrayOf(
            "Filtra per categoria",
            "Filtra per prezzo",
            "Ordina per Titolo (A-Z)",
            "Filtra per intervallo di date"
        )
        val checkedItems = booleanArrayOf(
            categorieFiltrate != null,
            rangePrezzoFiltrato != null,
            ordinaPerTitoloAttivo,
            intervalloDateInizio != null && intervalloDateFine != null
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Filtri disponibili")
            .setMultiChoiceItems(opzioni, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Applica") { _, _ ->
                if (!checkedItems.any { it }) {
                    // Nessun filtro selezionato → reset
                    categorieFiltrate = null
                    rangePrezzoFiltrato = null
                    intervalloDateInizio = null
                    intervalloDateFine = null
                    ordinaPerTitoloAttivo = false
                    applicaTuttiIFiltri()
                    return@setPositiveButton
                }

                if (checkedItems[0]) mostraDialogoCategorie()
                else categorieFiltrate = null

                if (checkedItems[1]) mostraDialogoPrezzo()
                else rangePrezzoFiltrato = null

                ordinaPerTitoloAttivo = checkedItems[2]

                if (checkedItems[3]) mostraDialogoIntervalloDate()
                else {
                    intervalloDateInizio = null
                    intervalloDateFine = null
                }

                // Se non ci sono dialoghi asincroni (categorie o date), chiama direttamente i filtri
                if (!checkedItems[0] && !checkedItems[3]) {
                    applicaTuttiIFiltri()
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    // Mostra dialog di selezione categoria (recupera anche categorie personalizzate da Firestore)
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

    // Mostra dialog per scegliere ordinamento data all'interno di un intervallo scelto
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

    // Mostra i due date picker per selezionare l'intervallo di date
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

    // Mostra dialog per selezionare uno o più intervalli di prezzo
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
                if (checkedItems[1]) filtri.add(50f to 100f)
                if (checkedItems[2]) filtri.add(100f to Float.MAX_VALUE)

                filtraPerPrezzo(filtri)
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    // Applica filtro per categorie
    private fun filtraPerCategorie(categorie: List<String>) {
        categorieFiltrate = categorie
        applicaTuttiIFiltri()
    }
    // Applica filtro per intervallo di prezzo
    private fun filtraPerPrezzo(rangeList: List<Pair<Float, Float>>) {
        rangePrezzoFiltrato = rangeList
        applicaTuttiIFiltri()
    }

    // Applica filtro per intervallo di date e ordinamento
    private fun filtraPerIntervalloDate(inizio: Calendar, fine: Calendar, descending: Boolean) {
        intervalloDateInizio = inizio
        intervalloDateFine = fine
        ordineDataDescrescente = descending
        applicaTuttiIFiltri()
    }

    // Applica tutti i filtri selezionati dall'utente
    private fun applicaTuttiIFiltri() {
        val tutteLeSpese = viewModel.spese.value ?: return
        var speseFiltrate = tutteLeSpese.toList()

        // Filtro per categoria
        categorieFiltrate?.let { categorie ->
            speseFiltrate = speseFiltrate.filter { it.categoria in categorie }
        }

        // Filtro per prezzo
        rangePrezzoFiltrato?.let { ranges ->
            speseFiltrate = speseFiltrate.filter { spesa ->
                ranges.any { (min, max) -> spesa.importo in min..max }
            }
        }

        // Filtro per intervallo di date
        if (intervalloDateInizio != null && intervalloDateFine != null) {
            speseFiltrate = speseFiltrate.filter {
                val dataSpesa = Calendar.getInstance().apply {
                    set(it.anno, it.mese - 1, it.giorno)
                }
                dataSpesa in intervalloDateInizio!!..intervalloDateFine!!
            }.let {
                if (ordineDataDescrescente)
                    it.sortedByDescending { s -> Calendar.getInstance().apply { set(s.anno, s.mese - 1, s.giorno) }.timeInMillis }
                else
                    it.sortedBy { s -> Calendar.getInstance().apply { set(s.anno, s.mese - 1, s.giorno) }.timeInMillis }
            }
        }

        // Ordinamento per titolo
        if (ordinaPerTitoloAttivo) {
            speseFiltrate = speseFiltrate.sortedBy { it.titolo.lowercase() }
        }

        adapter.submitList(speseFiltrate)

        aggiornaImportoTotale(speseFiltrate)

        if (speseFiltrate.isEmpty()) {
            Toast.makeText(this, "Nessuna spesa trovata con i filtri applicati", Toast.LENGTH_SHORT).show()
        }
        val btnRipristina = findViewById<Button>(R.id.btnRipristinaFiltri)
        btnRipristina.visibility = View.VISIBLE
    }

    // Mostra popup statistiche dettagliate per mese e anno
    private fun mostraPopupStatisticheConFiltroMese() {
        val context = this
        val spese = viewModel.spese.value ?: return
        if (spese.isEmpty()) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_statistiche_mese, null)
        val spinnerMese = dialogView.findViewById<Spinner>(R.id.spinnerMese)
        val textStatistiche = dialogView.findViewById<TextView>(R.id.textStatistiche)

        // Lista mesi e setup spinner
        val mesi = listOf(
            "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
            "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"
        )
        spinnerMese.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, mesi)

        // Anni disponibili nelle spese
        val anniDisponibili = spese.map { it.anno }.toSet().sorted()

        if (anniDisponibili.isEmpty()) {
            Toast.makeText(context, "Nessun anno disponibile per le statistiche", Toast.LENGTH_SHORT).show()
            return
        }

        // Spinner per l'anno
        val spinnerAnno = Spinner(context).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, anniDisponibili)
        }

        // Inserisce spinner anno nel layout
        val layout = dialogView.findViewById<LinearLayout>(R.id.layoutStatisticheMese)
        layout.addView(spinnerAnno, 1)

        val builder = AlertDialog.Builder(context)
            .setTitle("Statistiche per mese")
            .setView(dialogView)
            .setPositiveButton("Chiudi", null)

        val alertDialog = builder.create()
        alertDialog.show()

        // Funzione per calcolare e mostrare statistiche
        val calcolaStatistiche = lambda@{
            val meseSelezionato = spinnerMese.selectedItemPosition + 1
            val annoSelezionato = spinnerAnno.selectedItem as Int

            val speseFiltrate = spese.filter { it.mese == meseSelezionato && it.anno == annoSelezionato }

            if (speseFiltrate.isEmpty()) {
                textStatistiche.text = "Nessuna spesa trovata per questo mese."
                return@lambda
            }

            val totale = speseFiltrate.sumOf { it.importo.toDouble() }
            val media = totale / speseFiltrate.size

            val max = speseFiltrate.maxByOrNull { it.importo }!!
            val min = speseFiltrate.minByOrNull { it.importo }!!

            // Top 3 categorie per totale speso
            val categorieMap = mutableMapOf<String, Double>()
            speseFiltrate.forEach {
                categorieMap[it.categoria] = categorieMap.getOrDefault(it.categoria, 0.0) + it.importo
            }
            val top3Categorie = categorieMap.entries.sortedByDescending { it.value }.take(3)

            // Compone la stringa con le statistiche
            val sb = StringBuilder()
            sb.append("Totale: €%.2f\n".format(totale))
            sb.append("Media: €%.2f\n\n".format(media))

            sb.append("Top categorie:\n")
            top3Categorie.forEach {
                sb.append("${it.key}: €%.2f\n".format(it.value))
            }

            sb.append("\nSpesa più alta: ${max.titolo} - €%.2f\n".format(max.importo))
            sb.append("Spesa più bassa: ${min.titolo} - €%.2f\n".format(min.importo))

            textStatistiche.text = sb.toString()
        }

        // Listener per aggiornare le statistiche al cambio di mese o anno
        spinnerMese.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                calcolaStatistiche()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        spinnerAnno.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                calcolaStatistiche()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // Mostra popup statistiche globali
    private fun mostraPopupStatistiche() {
        val spese = viewModel.spese.value ?: return
        if (spese.isEmpty()) return

        // Calcolo totale, media mensile, top categorie, massimi e minimi
        var totaleComplessivo = 0.0
        val mesiUnici = mutableSetOf<Pair<Int, Int>>() // (anno, mese)
        val totalePerCategoria = mutableMapOf<String, Double>()
        var spesaMax = spese.first()
        var spesaMin = spese.first()

        for (spesa in spese) {
            totaleComplessivo += spesa.importo
            // Totale per categoria
            val categoria = spesa.categoria
            totalePerCategoria[categoria] = totalePerCategoria.getOrDefault(categoria, 0.0) + spesa.importo
            // Mesi unici per la media mensile
            mesiUnici.add(Pair(spesa.anno, spesa.mese))
            // Valori massimo e minimo
            if (spesa.importo > spesaMax.importo) spesaMax = spesa
            if (spesa.importo < spesaMin.importo) spesaMin = spesa
        }

        val mediaMensile = if (mesiUnici.isNotEmpty()) totaleComplessivo / mesiUnici.size else 0.0

        // Top 3 categorie
        val top3Categorie = totalePerCategoria.entries
            .sortedByDescending { it.value }
            .take(3)

        // Compone il messaggio
        val sb = StringBuilder()
        sb.append("Statistiche Spese\n\n")
        sb.append("Totale complessivo: €%.2f\n".format(totaleComplessivo))
        sb.append("Media mensile: €%.2f\n\n".format(mediaMensile))

        sb.append("Top 3 categorie:\n")
        for ((categoria, totale) in top3Categorie) {
            sb.append("$categoria: €%.2f\n".format(totale))
        }

        sb.append("\nSpesa più alta: ${spesaMax.titolo} - €%.2f\n".format(spesaMax.importo))
        sb.append("Spesa più bassa: ${spesaMin.titolo} - €%.2f\n".format(spesaMin.importo))

        // Mostra il dialog con le statistiche globali e il bottone per vedere quelle dettagliate per mese
        AlertDialog.Builder(this)
            .setTitle("Statistiche")
            .setMessage(sb.toString())
            .setPositiveButton("OK", null)
            .setNeutralButton("Visualizza dettagli mese") { _, _ ->
                mostraPopupStatisticheConFiltroMese()
            }
            .show()
    }

    // Apre il fragment per modificare una spesa già esistente
    private fun apriModificaSpesaFragment(spesa: Spesa) {
        btnAggiungiSpesa.visibility = View.GONE
        findViewById<FrameLayout>(R.id.fragment_container).visibility = View.VISIBLE

        val fragment = AggiungiSpesaFragment()

        // Passa i dati della spesa da modificare tramite Bundle
        val bundle = Bundle().apply {
            putString("titolo", spesa.titolo)
            putString("descrizione", spesa.descrizione)
            putInt("giorno", spesa.giorno)
            putInt("mese", spesa.mese)
            putInt("anno", spesa.anno)
            putFloat("importo", spesa.importo)
            putString("categoria", spesa.categoria)
            putString("documentId", spesa.id) // ID necessario per update
            putBoolean("modifica", true) // Flag per distinguere modalità modifica
        }

        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Mostra dialog di conferma eliminazione spesa
    private fun mostraDialogConfermaEliminazione(spesa: Spesa) {
        AlertDialog.Builder(this)
            .setTitle("Conferma eliminazione")
            .setMessage("Sei sicuro di voler eliminare questa spesa?")
            .setPositiveButton("Sì") { _, _ ->
                db.collection("Spese")
                    .whereEqualTo("titolo", spesa.titolo)
                    .whereEqualTo("descrizione", spesa.descrizione)
                    .whereEqualTo("importo", spesa.importo)
                    .whereEqualTo("giorno", spesa.giorno)
                    .whereEqualTo("mese", spesa.mese)
                    .whereEqualTo("anno", spesa.anno)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            db.collection("Spese").document(document.id).delete()
                        }
                        Toast.makeText(this, "Spesa eliminata!", Toast.LENGTH_SHORT).show()
                        viewModel.caricaTutteLeSpese()
                    }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun aggiornaImportoTotale(spese: List<Spesa>) {
        val totale = spese.sumOf { it.importo.toDouble() }
        val testo = "Importo Totale = €%.2f".format(totale)
        textImportoTotale.text = testo
    }
}