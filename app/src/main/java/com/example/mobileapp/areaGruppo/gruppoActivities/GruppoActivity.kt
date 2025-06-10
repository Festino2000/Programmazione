package com.example.mobileapp.areaGruppo.gruppoActivities

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.mobileapp.FabMenuController
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.adapters.GruppoAdapter
import com.example.mobileapp.R
import com.example.mobileapp.areaGruppo.gruppoDialogs.AggiungiGruppoDialog
import com.example.mobileapp.areaGruppo.gruppoDialogs.EntraGruppoDialog
import com.example.mobileapp.areaGruppo.gruppoDataClasses.Gruppo
import com.example.mobileapp.viewModels.GruppoViewModel
import com.example.mobileapp.areaGruppo.gruppoFragments.SchermataSpeseFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

// Activity principale che gestisce la schermata dei gruppi
class GruppoActivity : AppCompatActivity() {

    // RecyclerView per mostrare i gruppi
    private lateinit var recyclerView: RecyclerView
    // Adapter per la lista dei gruppi
    private lateinit var adapter: GruppoAdapter
    // Lista completa dei gruppi caricati dall'utente
    private var listaCompleta: List<Gruppo> = emptyList()
    // Controller per la gestione del menu floating (FAB)
    private lateinit var fabMenuController: FabMenuController
    // ViewModel per la logica dei gruppi
    private val viewModel: GruppoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gruppo)
        val toolbar = findViewById<Toolbar>(R.id.toolbar2)
        setSupportActionBar(toolbar)

        // Imposta la RecyclerView e il suo Adapter
        recyclerView = findViewById(R.id.recyclerViewGruppi)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GruppoAdapter(
            onGruppoClick = { gruppo -> apriSchermataSpeseGruppo(gruppo) }
        )
        recyclerView.adapter = adapter

        // Trova i riferimenti ai bottoni e al menu FAB
        val fabMenu = findViewById<ExtendedFloatingActionButton>(R.id.fabMenu)
        val buttonAggiungiGruppo = findViewById<MaterialButton>(R.id.fabAggiungiGruppo)
        val buttonEntraGruppo = findViewById<MaterialButton>(R.id.fabEntraGruppo)
        val touchSchermo = findViewById<View>(R.id.coordinatorLayout)

        // Inizializza il controller per il menu FAB
        fabMenuController = FabMenuController(
            fabMenu = fabMenu,
            fabButtons = listOf(buttonAggiungiGruppo, buttonEntraGruppo),
            rootView = touchSchermo
        )

        // Listener per aggiungere un nuovo gruppo
        buttonAggiungiGruppo.setOnClickListener {
            fabMenuController.closeMenu()
            val dialog = AggiungiGruppoDialog()
            dialog.listener = object : AggiungiGruppoDialog.OnGruppoCreatoListener {
                override fun onGruppoCreato(gruppo: Gruppo) {
                    viewModel.caricaGruppiUtente()
                    apriSchermataSpeseGruppo(gruppo)
                }
            }
            dialog.show(supportFragmentManager, "AggiungiGruppoDialog")
        }

        // Listener per entrare in un gruppo esistente tramite dialog
        buttonEntraGruppo.setOnClickListener {
            fabMenuController.closeMenu()
            val dialog = EntraGruppoDialog()
            dialog.show(supportFragmentManager, "EntraGruppoDialog")
        }

        // Observer per aggiornare la lista dei gruppi quando cambia
        viewModel.gruppiUtente.observe(this, Observer { lista ->
            listaCompleta = lista
            adapter.submitList(lista)
        })

        // Carica i gruppi dell’utente al primo avvio
        viewModel.caricaGruppiUtente()
    }

    // Crea il menu di ricerca nella toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_gruppo_activity, menu)
        val searchItem = menu.findItem(R.id.ricerca)
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = "Cerca gruppo"
        // Listener per la ricerca nei gruppi
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filtraGruppi(newText.orEmpty())
                return true
            }
        })

        return true
    }

    // Apre il fragment delle spese del gruppo selezionato
    private fun apriSchermataSpeseGruppo(gruppo: Gruppo) {
        val fragment = SchermataSpeseFragment()
        // Listener per mostrare/nascondere i FAB in base allo stack dei fragment
        supportFragmentManager.addOnBackStackChangedListener {
            val fab = findViewById<ExtendedFloatingActionButton>(R.id.fabMenu)
            val fabAggiungi = findViewById<MaterialButton>(R.id.fabAggiungiGruppo)
            val fabEntra = findViewById<MaterialButton>(R.id.fabEntraGruppo)

            val inHome = supportFragmentManager.backStackEntryCount == 0
            fabAggiungi.visibility = if (inHome) View.VISIBLE else View.GONE
            fabEntra.visibility = if (inHome) View.VISIBLE else View.GONE
            fab.visibility = if (supportFragmentManager.backStackEntryCount == 0) View.VISIBLE else View.GONE
        }

        // Passa i dati del gruppo al fragment
        val bundle = Bundle().apply {
            putString("idGruppo", gruppo.idUnico)
            putString("titoloGruppo", gruppo.titolo)
        }
        fragment.arguments = bundle

        invalidateOptionsMenu()

        // Sostituisce il fragment corrente con quello delle spese
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Filtra i gruppi in base al testo inserito nella ricerca
    private fun filtraGruppi(query: String) {
        val gruppiFiltrati = listaCompleta.filter {
            it.titolo.contains(query, ignoreCase = true)
        }
        adapter.submitList(gruppiFiltrati)
    }
}