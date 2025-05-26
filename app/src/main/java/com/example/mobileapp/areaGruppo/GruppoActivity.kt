package com.example.mobileapp.areaGruppo

import GruppoAdapter
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class GruppoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GruppoAdapter
    private val gruppiList = mutableListOf<Gruppo>()

    private val viewModel: GruppoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gruppo)

        val toolbar = findViewById<Toolbar>(R.id.toolbar2)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recyclerViewGruppi)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GruppoAdapter(gruppiList){ gruppo ->
            apriSchermataSpeseGruppo(gruppo)
        }
        recyclerView.adapter = adapter

        val fabMenu = findViewById<ExtendedFloatingActionButton>(R.id.fabMenu)
        val buttonAggiungiGruppo = findViewById<MaterialButton>(R.id.fabAggiungiGruppo)
        val buttonEntraGruppo = findViewById<MaterialButton>(R.id.fabEntraGruppo)
        val touchSchermo = findViewById<View>(R.id.coordinatorLayout)

        fabMenu.setOnClickListener {
            buttonAggiungiGruppo.visibility = View.VISIBLE
            buttonEntraGruppo.visibility = View.VISIBLE
        }

        buttonAggiungiGruppo.setOnClickListener {
            val dialog = AggiungiGruppoDialog()
            dialog.listener = object : AggiungiGruppoDialog.OnGruppoCreatoListener {
                override fun onGruppoCreato(gruppo: Gruppo) {
                    viewModel.caricaGruppiUtente()
                    apriSchermataSpeseGruppo(gruppo)
                }
            }
            dialog.show(supportFragmentManager, "AggiungiGruppoDialog")
            buttonAggiungiGruppo.visibility = View.GONE
            buttonEntraGruppo.visibility = View.GONE
        }

        buttonEntraGruppo.setOnClickListener {
            val dialog = EntraGruppoDialog()
            dialog.show(supportFragmentManager, "EntraGruppoDialog")
            buttonAggiungiGruppo.visibility = View.GONE
            buttonEntraGruppo.visibility = View.GONE
        }

        touchSchermo.setOnTouchListener { v, _ ->
            if (buttonAggiungiGruppo.isVisible || buttonEntraGruppo.isVisible) {
                buttonAggiungiGruppo.visibility = View.GONE
                buttonEntraGruppo.visibility = View.GONE
                v.performClick()
            }
            false
        }

        // Observer del ViewModel per aggiornare la lista
        viewModel.gruppiUtente.observe(this, Observer { lista ->
            gruppiList.clear()
            gruppiList.addAll(lista)
            adapter.notifyDataSetChanged()
        })

        // Carica inizialmente i gruppi dell’utente
        viewModel.caricaGruppiUtente()
        
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    private fun apriSchermataSpeseGruppo(gruppo: Gruppo) {
        val fragment = SchermataSpeseFragment()

        // Passaggio dati al fragment (es. ID gruppo)
        val bundle = Bundle().apply {
            putString("idGruppo", gruppo.idUnico)
            putString("titoloGruppo", gruppo.titolo)
        }
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .addToBackStack(null)
            .commit()
    }
}

/*val button = findViewById<Button>(R.id.button)
button.setOnClickListener {
    Toast.makeText(this, "Click OK", Toast.LENGTH_SHORT).show()
    val fragment = SchermataSpeseFragment()
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragmentContainerView, fragment)
        .addToBackStack(null)
        .commit()
}*/