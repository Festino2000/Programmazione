package com.example.mobileapp.areaGruppo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R

class UtenteCheckboxAdapter(private val utenti: List<Utente>) : RecyclerView.Adapter<UtenteCheckboxAdapter.ViewHolder>() {

    private val utentiSelezionati = mutableSetOf<Utente>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nicknameText: TextView = view.findViewById(R.id.nicknameText)
        val checkBox: CheckBox = view.findViewById(R.id.checkBoxUtente)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_utente_checkbox, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val utente = utenti[position]
        holder.nicknameText.text = utente.nickname
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = utentiSelezionati.contains(utente)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) utentiSelezionati.add(utente)
            else utentiSelezionati.remove(utente)
        }
    }

    override fun getItemCount(): Int = utenti.size

    fun getUtentiSelezionati(): List<Utente> = utentiSelezionati.toList()
}
