package com.example.mobileapp.areaGruppo.gruppoActivities

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        adapter = GruppoAdapter(
            onGruppoClick = { gruppo -> apriSchermataSpeseGruppo(gruppo) },
            onGruppoLongClick = { gruppo -> mostraDialogGestioneGruppo(gruppo) }
        )
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
    private fun mostraDialogGestioneGruppo(gruppo: Gruppo) {
        val uidCorrente = FirebaseAuth.getInstance().currentUser?.uid
        if (uidCorrente != gruppo.creatoreID) return // solo il creatore può modificare/eliminare

        val opzioni = arrayOf("Modifica Gruppo", "Elimina Gruppo")

        AlertDialog.Builder(this)
            .setTitle(gruppo.titolo)
            .setItems(opzioni) { _, which ->
                when (which) {
                    0 -> mostraDialogModificaGruppo(gruppo)
                    1 -> confermaEliminazioneGruppo(gruppo)
                }
            }
            .show()
    }
    private fun confermaEliminazioneGruppo(gruppo: Gruppo) {
        AlertDialog.Builder(this)
            .setTitle("Elimina gruppo")
            .setMessage("Vuoi eliminare il gruppo e tutte le sue spese?")
            .setPositiveButton("Sì") { _, _ ->
                val db = FirebaseFirestore.getInstance()
                db.collection("Gruppi")
                    .whereEqualTo("idUnico", gruppo.idUnico)
                    .get()
                    .addOnSuccessListener { result ->
                        val docId = result.documents.firstOrNull()?.id ?: return@addOnSuccessListener

                        // Prima elimina tutte le spese
                        db.collection("Gruppi").document(docId)
                            .collection("Spese")
                            .get()
                            .addOnSuccessListener { spese ->
                                for (doc in spese.documents) {
                                    doc.reference.delete()
                                }

                                // Poi elimina il gruppo
                                db.collection("Gruppi").document(docId).delete().addOnSuccessListener {
                                    viewModel.caricaGruppiUtente()
                                }
                            }
                    }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }
    private fun mostraDialogModificaGruppo(gruppo: Gruppo) {
        val input = EditText(this).apply {
            hint = "Titolo nuovo"
            setText(gruppo.titolo)
        }

        AlertDialog.Builder(this)
            .setTitle("Modifica gruppo")
            .setView(input)
            .setPositiveButton("Salva") { _, _ ->
                val nuovoTitolo = input.text.toString()
                val db = FirebaseFirestore.getInstance()

                db.collection("Gruppi")
                    .whereEqualTo("idUnico", gruppo.idUnico)
                    .get()
                    .addOnSuccessListener { result ->
                        val docId = result.documents.firstOrNull()?.id ?: return@addOnSuccessListener
                        db.collection("Gruppi").document(docId)
                            .update("titolo", nuovoTitolo)
                            .addOnSuccessListener { viewModel.caricaGruppiUtente() }
                    }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }
}