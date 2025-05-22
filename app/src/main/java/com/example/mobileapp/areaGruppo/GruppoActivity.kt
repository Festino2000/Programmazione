package com.example.mobileapp.areaGruppo

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import androidx.fragment.app.FragmentContainerView

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

        val buttonAggiungi = findViewById<Button>(R.id.buttonAggiungiGruppo)
        buttonAggiungi.setOnClickListener {
            val fragmentContainer = findViewById<FragmentContainerView>(R.id.fragmentContainerView)
            fragmentContainer.visibility = View.VISIBLE

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, AggiungiGruppoFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun aggiungiNuovoGruppo(nome: String) {
        gruppiList.add(nome)
        adapter.notifyItemInserted(gruppiList.size - 1)
        recyclerView.scrollToPosition(gruppiList.size - 1)  // Scorri all'ultimo elemento
    }


}