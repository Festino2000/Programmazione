package com.example.mobileapp.GestioneSpese

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R

class SpeseAdapter : ListAdapter<Spesa, SpeseAdapter.SpesaViewHolder>(SpesaDiffCallback()) {

    class SpesaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titolo: TextView = itemView.findViewById(R.id.titoloSpesa)
        private val importo: TextView = itemView.findViewById(R.id.importoSpesa)
        private val data: TextView = itemView.findViewById(R.id.dataSpesa)

        fun bind(spesa: Spesa) {
            titolo.text = spesa.titolo
            importo.text = "€ ${spesa.importo}"
            data.text = String.format("%02d/%02d/%04d", spesa.giorno, spesa.mese, spesa.anno)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpesaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_spesa, parent, false)
        return SpesaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpesaViewHolder, position: Int) {
        val spesa = getItem(position)
        holder.bind(spesa)
    }

    class SpesaDiffCallback : DiffUtil.ItemCallback<Spesa>() {
        override fun areItemsTheSame(oldItem: Spesa, newItem: Spesa): Boolean {
            return oldItem.titolo == newItem.titolo && oldItem.anno == newItem.anno && oldItem.mese == newItem.mese && oldItem.giorno == newItem.giorno
        }

        override fun areContentsTheSame(oldItem: Spesa, newItem: Spesa): Boolean {
            return oldItem == newItem
        }
    }
}