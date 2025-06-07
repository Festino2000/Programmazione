package com.example.mobileapp.areaGruppo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R

class GruppoAdapter(
    private val onGruppoClick: (Gruppo) -> Unit,
    private val onGruppoLongClick: (Gruppo) -> Unit // aggiunto per gestione long press
) : ListAdapter<Gruppo, GruppoAdapter.GruppiViewHolder>(GruppoDiffCallback()) {

    inner class GruppiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titolo: TextView = itemView.findViewById(R.id.textViewTitoloGruppo)
        val descrizione: TextView = itemView.findViewById(R.id.textViewDescrizioneGruppo)
        val idUnico: TextView = itemView.findViewById(R.id.textViewIdGruppo)

        init {
            itemView.setOnClickListener {
                val gruppo = getItem(adapterPosition)
                onGruppoClick(gruppo)
            }
            itemView.setOnLongClickListener {
                val gruppo = getItem(adapterPosition)
                onGruppoLongClick(gruppo)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GruppiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gruppo, parent, false)
        return GruppiViewHolder(view)
    }

    override fun onBindViewHolder(holder: GruppiViewHolder, position: Int) {
        val gruppo = getItem(position)
        holder.titolo.text = gruppo.titolo
        holder.descrizione.text = gruppo.descrizione
        holder.idUnico.text = "ID: ${gruppo.idUnico}"
    }
}

class GruppoDiffCallback : DiffUtil.ItemCallback<Gruppo>() {
    override fun areItemsTheSame(oldItem: Gruppo, newItem: Gruppo): Boolean {
        return oldItem.idUnico == newItem.idUnico
    }

    override fun areContentsTheSame(oldItem: Gruppo, newItem: Gruppo): Boolean {
        return oldItem == newItem
    }
}
