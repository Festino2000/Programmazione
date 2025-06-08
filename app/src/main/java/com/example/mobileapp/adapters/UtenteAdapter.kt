package com.example.mobileapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.example.mobileapp.areaGruppo.gruppoDataClasses.Utente

class UtenteAdapter(private val utenti: List<Utente>) :
    RecyclerView.Adapter<UtenteAdapter.UtenteViewHolder>() {

    inner class UtenteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textNome: TextView = itemView.findViewById(R.id.textNomeUtente)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UtenteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_utente_membro, parent, false)
        return UtenteViewHolder(view)
    }

    override fun onBindViewHolder(holder: UtenteViewHolder, position: Int) {
        val utente = utenti[position]
        holder.textNome.text = utente.nickname
    }

    override fun getItemCount(): Int = utenti.size
}
