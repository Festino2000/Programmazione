package com.example.mobileapp.areaGruppo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.example.mobileapp.areaGruppo.SpesaCondivisaAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SpesaCondivisaFragment : Fragment(R.layout.fragment_spese_condivise) {

    private lateinit var recyclerViewDaPagare: RecyclerView
    private lateinit var recyclerViewDaRicevere: RecyclerView
    private lateinit var adapterDaPagare: SpesaCondivisaAdapter
    private lateinit var adapterDaRicevere: SpesaCondivisaAdapter
    private lateinit var fabAggiungiSpesa: FloatingActionButton


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = TextView(requireContext()).apply {
        text = "Spese"
        textSize = 24f
    }}
    /*override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_spese_condivise, container, false)
    }*/

    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewDaPagare = view.findViewById(R.id.recyclerViewDaPagare)
        recyclerViewDaRicevere = view.findViewById(R.id.recyclerViewDaRicevere)
        fabAggiungiSpesa = view.findViewById(R.id.fabAggiungiSpesa)

        recyclerViewDaPagare.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewDaRicevere.layoutManager = LinearLayoutManager(requireContext())

        adapterDaPagare = SpesaCondivisaAdapter()
        adapterDaRicevere = SpesaCondivisaAdapter()

        recyclerViewDaPagare.adapter = adapterDaPagare
        recyclerViewDaRicevere.adapter = adapterDaRicevere

        // Esempio dati di test (sostituisci con dati reali)
        val speseDaPagare = listOf(
            SpesaCondivisa("Cena", "Luca", 10, 5, 2025, 25.0f),
            SpesaCondivisa("Regalo", "Chiara", 11, 5, 2025, 15.0f)
        )

        val speseDaRicevere = listOf(
            SpesaCondivisa("Viaggio", "Marco", 12, 5, 2025, 30.0f),
            SpesaCondivisa("Taxi", "Giulia", 13, 5, 2025, 12.0f)
        )

        adapterDaPagare.submitList(speseDaPagare)
        adapterDaRicevere.submitList(speseDaRicevere)

        fabAggiungiSpesa.setOnClickListener {
            // TODO: Apri dialog o schermata per aggiungere una nuova spesa condivisa
        }
    }+/}
}*/