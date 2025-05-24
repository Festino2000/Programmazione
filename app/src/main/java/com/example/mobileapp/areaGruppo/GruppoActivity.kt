package com.example.mobileapp.areaGruppo

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class GruppoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GruppoAdapter
    private val gruppiList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gruppo)

        recyclerView = findViewById(R.id.recyclerViewGruppi)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GruppoAdapter(gruppiList)
        recyclerView.adapter = adapter
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
            dialog.show(supportFragmentManager, "AggiungiGruppoDialog")
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


}