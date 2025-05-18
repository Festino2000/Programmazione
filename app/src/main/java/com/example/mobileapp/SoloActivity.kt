package com.example.mobileapp

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class SoloActivity : AppCompatActivity(), AggiungiSpesaFragment.OnSpesaAggiuntaListener {

    private val listaSpese = mutableListOf<Spesa>()
    private lateinit var db: FirebaseFirestore
    private lateinit var btnAggiungiSpesa: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo)

        db = FirebaseFirestore.getInstance()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Pulsante "Aggiungi Spesa"
        btnAggiungiSpesa = findViewById(R.id.btnAggiungiSpesa)
        btnAggiungiSpesa.setOnClickListener {
            Log.d("SoloActivity", "Pulsante Aggiungi Spesa cliccato")
            apriAggiungiSpesaFragment()
        }

        // Pulsante "Trasporti"
        /*val trasportiButton = findViewById<ImageView>(R.id.trasporti)
        trasportiButton.setOnClickListener {
            mostraListaSpesePerCategoria("TRASPORTI")
        }*/
    }

    // Funzione per aprire il Fragment di aggiunta spesa
    private fun apriAggiungiSpesaFragment() {
        // Nascondi il pulsante e l'icona Trasporti
        btnAggiungiSpesa.visibility = View.GONE
        //findViewById<FrameLayout>(R.id.fragment_container).visibility = View.VISIBLE

        val fragment = AggiungiSpesaFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, "AGGIUNGI_SPESA_FRAGMENT")
            .addToBackStack(null)
            .commit()
    }



    // Funzione per chiudere il Fragment e mostrare il pulsante
    fun chiudiFragment() {
        btnAggiungiSpesa.visibility = View.VISIBLE
        findViewById<FrameLayout>(R.id.fragment_container).visibility = View.GONE
        supportFragmentManager.popBackStack()
    }




    // Funzione per visualizzare la lista di spese filtrata per categoria
    /*private fun mostraListaSpesePerCategoria(category: String) {
        val fragment = ListaSpeseFragment.newInstance(category)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }*/

    override fun onSpesaAggiunta(spesa: Spesa) {
        listaSpese.add(spesa)
        Toast.makeText(this, "Spesa aggiunta: ${spesa.titolo}", Toast.LENGTH_SHORT).show()
        //chiudiFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                Toast.makeText(this, "Cerca cliccato", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_filter -> {
                Toast.makeText(this, "Filtra cliccato", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_settings -> {
                Toast.makeText(this, "Impostazioni cliccato", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
