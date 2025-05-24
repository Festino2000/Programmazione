package com.example.mobileapp.areaGruppo

import GruppoPagerAdapter
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.mobileapp.areaGruppo.AggiungiGruppoDialog.OnGruppoCreatoListener
import com.example.mobileapp.areaPersonale.AggiungiSpesaFragment

class GruppoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GruppoAdapter
    private val gruppiList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gruppo)
        val toolbar = findViewById<Toolbar>(R.id.toolbar2)
        setSupportActionBar(toolbar)
        recyclerView = findViewById(R.id.recyclerViewGruppi)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GruppoAdapter(gruppiList)
        recyclerView.adapter = adapter

        /*val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val fragment = SchermataSpeseFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit()
        }*/
        val fab_button = findViewById<Button>(R.id.fabMenu)
        val buttonAggiungiGruppo = findViewById<Button>(R.id.fabAggiungiGruppo)
        val buttonEntraGruppo = findViewById<Button>(R.id.fabEntraGruppo)
        val touchSchermo = findViewById<View>(R.id.coordinatorLayout)

        fab_button.setOnClickListener{
            buttonEntraGruppo.visibility = View.VISIBLE
            buttonAggiungiGruppo.visibility = View.VISIBLE
        }

        buttonAggiungiGruppo.setOnClickListener {
            val dialog = AggiungiGruppoDialog()
            dialog.listener = object : OnGruppoCreatoListener {
                override fun onGruppoCreato(titolo: String) {
                    aggiungiNuovoGruppo(titolo)  // Aggiunge il gruppo alla lista
                }
            }
            dialog.show(supportFragmentManager, "AggiungiGruppoDialog")
            buttonEntraGruppo.visibility = View.GONE
            buttonAggiungiGruppo.visibility = View.GONE
        }

        buttonEntraGruppo.setOnClickListener {
            val dialog = EntraGruppoDialog()
            dialog.show(supportFragmentManager, "EntraGruppoDialog")
            buttonEntraGruppo.visibility = View.GONE
            buttonAggiungiGruppo.visibility = View.GONE
        }

        touchSchermo.setOnTouchListener { v, event ->
            if (buttonEntraGruppo.isVisible || buttonAggiungiGruppo.isVisible) {
                buttonEntraGruppo.visibility = View.GONE
                buttonAggiungiGruppo.visibility = View.GONE
                v.performClick()
            }
            false
        }
    }
    private fun aggiungiNuovoGruppo(nome: String) {
        gruppiList.add(nome)
        adapter.notifyItemInserted(gruppiList.size - 1)
        recyclerView.scrollToPosition(gruppiList.size - 1)  // Scorri all'ultimo elemento

    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                // Azione per la ricerca
                Toast.makeText(this, "Ricerca cliccata", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_settings -> {
                // Azione per le impostazioni
                Toast.makeText(this, "Impostazioni cliccate", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }*/
}