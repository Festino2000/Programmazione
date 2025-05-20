package com.example.mobileapp.areaPersonale

import android.app.AlertDialog
import android.content.Context
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
import com.google.firebase.firestore.FirebaseFirestore

class SoloActivity : AppCompatActivity(), AggiungiSpesaFragment.OnSpesaAggiuntaListener {

    private val listaSpese = mutableListOf<Spesa>()
    private lateinit var db: FirebaseFirestore
    private lateinit var btnAggiungiSpesa: Button
    private lateinit var viewModel: SpeseViewModel
    private lateinit var adapter: SpeseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo)

        db = FirebaseFirestore.getInstance()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewSpese)
        adapter = SpeseAdapter()

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.item_spacing)
        recyclerView.addItemDecoration(SpacingItemDecoration(spacingInPixels, 2))

        viewModel = ViewModelProvider(this).get(SpeseViewModel::class.java)
        viewModel.caricaTutteLeSpese()

        viewModel.spese.observe(this) { spese ->
            adapter.submitList(spese)
        }

        btnAggiungiSpesa = findViewById(R.id.btnAggiungiSpesa)
        btnAggiungiSpesa.setOnClickListener {
            Log.d("SoloActivity", "Pulsante Aggiungi Spesa cliccato")
            apriAggiungiSpesaFragment()
        }

        // FAB Aggiungi Categoria
        val btnAggiungiCategoria = findViewById<View>(R.id.btnAggiungiCategoria)
        btnAggiungiCategoria.setOnClickListener {
            mostraDialogAggiungiCategoria()
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

    fun chiudiFragment() {
        btnAggiungiSpesa.visibility = View.VISIBLE
        findViewById<FrameLayout>(R.id.fragment_container).visibility = View.GONE
        supportFragmentManager.popBackStack()
    }

    override fun onSpesaAggiunta(spesa: Spesa) {
        listaSpese.add(spesa)
        Toast.makeText(this, "Spesa aggiunta: ${spesa.titolo}", Toast.LENGTH_SHORT).show()
    }

    private fun mostraDialogAggiungiCategoria() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nuova Categoria")

        val input = EditText(this).apply {
            hint = "Nome categoria"
        }
        builder.setView(input)

        builder.setPositiveButton("Aggiungi") { dialogInterface, _ ->
            val nuovaCategoria = input.text.toString().trim()
            if (nuovaCategoria.isNotBlank()) {
                val sharedPreferences = getSharedPreferences("PreferenzeCategorie", Context.MODE_PRIVATE)
                val categorieSalvate = sharedPreferences.getStringSet("categorie", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
                if (!categorieSalvate.contains(nuovaCategoria)) {
                    categorieSalvate.add(nuovaCategoria)
                    sharedPreferences.edit().putStringSet("categorie", categorieSalvate).apply()
                    Toast.makeText(this, "Categoria aggiunta: $nuovaCategoria", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Categoria già esistente", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Il nome della categoria non può essere vuoto", Toast.LENGTH_SHORT).show()
            }
            dialogInterface.dismiss()
        }

        builder.setNegativeButton("Annulla") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        builder.show()
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
