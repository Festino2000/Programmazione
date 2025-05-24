package com.example.mobileapp.areaGruppo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R

class SpesaCondivisaAdapter : RecyclerView.Adapter<SpesaCondivisaAdapter.SpesaViewHolder>() {

    private val spese = mutableListOf<SpesaCondivisa>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpesaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_spesa_condivisa, parent, false)
        return SpesaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpesaViewHolder, position: Int) {
        holder.bind(spese[position])
    }

    override fun getItemCount(): Int = spese.size

    fun submitList(nuovaLista: List<SpesaCondivisa>) {
        spese.clear()
        spese.addAll(nuovaLista)
        notifyDataSetChanged()
    }

    inner class SpesaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textPersona = itemView.findViewById<TextView>(R.id.textPersona)
        private val textImporto = itemView.findViewById<TextView>(R.id.textImportoSpesa)
        private val textDescrizione = itemView.findViewById<TextView>(R.id.textDescrizioneSpesa)
        private val textData = itemView.findViewById<TextView>(R.id.textDataSpesa)
        private val textStato = itemView.findViewById<TextView>(R.id.textStatoPagamento)

        fun bind(spesa: SpesaCondivisa) {
            textPersona.text = spesa.titolo
            textImporto.text = "${spesa.importo}€"
            textDescrizione.text = spesa.descrizione
            textData.text = String.format("%02d/%02d/%04d", spesa.giorno, spesa.mese, spesa.anno)
            textStato.text = "NON PAGATO"
            textStato.setBackgroundResource(R.drawable.text_non_pagato)
        }
    }
}
