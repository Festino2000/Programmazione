package com.example.mobileapp.areaGruppo

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.mobileapp.FabMenuController
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class GruppoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GruppoAdapter
    private var listaCompleta: List<Gruppo> = emptyList()
    private lateinit var fabMenuController: FabMenuController
    private val viewModel: GruppoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gruppo)
        val toolbar = findViewById<Toolbar>(R.id.toolbar2)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recyclerViewGruppi)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GruppoAdapter { gruppo ->
            apriSchermataSpeseGruppo(gruppo)
        }
        recyclerView.adapter = adapter

        val fabMenu = findViewById<ExtendedFloatingActionButton>(R.id.fabMenu)
        val buttonAggiungiGruppo = findViewById<MaterialButton>(R.id.fabAggiungiGruppo)
        val buttonEntraGruppo = findViewById<MaterialButton>(R.id.fabEntraGruppo)
        val touchSchermo = findViewById<View>(R.id.coordinatorLayout)

        //serve ad inzializzare il fabMenuController
        fabMenuController = FabMenuController(
            fabMenu = fabMenu,
            fabButtons = listOf(buttonAggiungiGruppo, buttonEntraGruppo),
            rootView = touchSchermo
        )

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

        buttonEntraGruppo.setOnClickListener {
            fabMenuController.closeMenu()
            val dialog = EntraGruppoDialog()
            dialog.show(supportFragmentManager, "EntraGruppoDialog")
        }


        // Observer del ViewModel per aggiornare la lista
        viewModel.gruppiUtente.observe(this, Observer { lista ->
            listaCompleta = lista
            adapter.submitList(lista)
        })

        // Carica inizialmente i gruppi dell’utente
        viewModel.caricaGruppiUtente()

        /*val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            Toast.makeText(this, "Click OK", Toast.LENGTH_SHORT).show()
            val fragment = SchermataSpeseFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit()
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_gruppo_activity, menu)
        val searchItem = menu.findItem(R.id.ricerca)
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = "Cerca gruppo"
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

    // 🔥 Mostra l’icona “Statistiche” solo nel fragment giusto
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val currentFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView)
            ?.childFragmentManager
            ?.fragments
            ?.firstOrNull()

        menu.findItem(R.id.statistiche)?.isVisible = currentFragment is SpesaCondivisaFragment
        return super.onPrepareOptionsMenu(menu)
    }

    // 🔥 Gestisce il click sull’icona “Statistiche”
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.statistiche -> {
                val currentFragment = supportFragmentManager
                    .findFragmentById(R.id.fragmentContainerView)
                    ?.childFragmentManager
                    ?.fragments
                    ?.firstOrNull()

                if (currentFragment is SpesaCondivisaFragment) {
                    currentFragment.mostraDialogStatistiche()
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun apriSchermataSpeseGruppo(gruppo: Gruppo) {
        val fragment = SchermataSpeseFragment()
        supportFragmentManager.addOnBackStackChangedListener {
            val fab = findViewById<ExtendedFloatingActionButton>(R.id.fabMenu)
            val fabAggiungi = findViewById<MaterialButton>(R.id.fabAggiungiGruppo)
            val fabEntra = findViewById<MaterialButton>(R.id.fabEntraGruppo)

            val inHome = supportFragmentManager.backStackEntryCount == 0
            fabAggiungi.visibility = if (inHome) View.VISIBLE else View.GONE
            fabEntra.visibility = if (inHome) View.VISIBLE else View.GONE
            fab.visibility = if (supportFragmentManager.backStackEntryCount == 0) View.VISIBLE else View.GONE
        }

        // Passaggio dati al fragment
        val bundle = Bundle().apply {
            putString("idGruppo", gruppo.idUnico)
            putString("titoloGruppo", gruppo.titolo)
        }
        fragment.arguments = bundle

        invalidateOptionsMenu()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun filtraGruppi(query: String) {
        val gruppiFiltrati = listaCompleta.filter {
            it.titolo.contains(query, ignoreCase = true)
        }
        adapter.submitList(gruppiFiltrati)
    }
}