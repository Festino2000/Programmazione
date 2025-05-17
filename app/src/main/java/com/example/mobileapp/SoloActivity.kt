package com.example.mobileapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class SoloActivity : AppCompatActivity(), AggiungiSpesaFragment.OnSpesaAggiuntaListener {

    // Lista per memorizzare le spese
    private val listaSpese = mutableListOf<Spesa>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_solo)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Pulsante per aggiungere una spesa
        val btnAggiungiSpesa = findViewById<Button>(R.id.btnAggiungiSpesa)
        btnAggiungiSpesa.setOnClickListener {
            // Caricamento del Fragment al click del pulsante
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AggiungiSpesaFragment()).addToBackStack(null).commit()
        }

        val trasportiButton = findViewById<ImageView>(R.id.trasporti)
        trasportiButton.setOnClickListener {
            mostraListaSpesePerCategoria("TRASPORTI")
        }
    }

    // Implementazione dell'interfaccia per ricevere la spesa dal Fragment
    override fun onSpesaAggiunta(spesa: Spesa) {
        listaSpese.add(spesa)
        Toast.makeText(this, "Spesa aggiunta: ${spesa.titolo}", Toast.LENGTH_SHORT).show()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Gestione dei click sul menu
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
    private fun mostraListaSpesePerCategoria(category: String) { // per visulaizzare il fragment
        val fragment = ListaSpeseFragment.newInstance(category)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}