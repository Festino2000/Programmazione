package com.example.mobileapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.example.mobileapp.areaPersonale.singoloDataClasses.Spesa
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SpeseAdapter(
    private val onModificaSpesa: (Spesa) -> Unit,
    private val onEliminaSpesa: (Spesa) -> Unit
) : ListAdapter<Spesa, SpeseAdapter.SpesaViewHolder>(SpesaDiffCallback()) {

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

        holder.itemView.setOnClickListener {
            MaterialAlertDialogBuilder(holder.itemView.context)
                .setTitle("Dettagli Spesa")
                .setMessage(
                    "Titolo: ${spesa.titolo}\n" +
                            "Descrizione: ${spesa.descrizione}\n" +
                            "Data: ${String.format("%02d/%02d/%04d", spesa.giorno, spesa.mese, spesa.anno)}\n" +
                            "Importo: €${spesa.importo}\n" +
                            "Categoria: ${spesa.categoria}"
                )
                .setPositiveButton("OK", null)
                .setNeutralButton("Modifica") { _, _ ->
                    onModificaSpesa(spesa)
                }
                .setNegativeButton("Elimina") { _, _ ->
                    onEliminaSpesa(spesa)
                }
                .show()
        }

    }

    class SpesaDiffCallback : DiffUtil.ItemCallback<Spesa>() {
        override fun areItemsTheSame(oldItem: Spesa, newItem: Spesa): Boolean {
            return oldItem.titolo == newItem.titolo &&
                    oldItem.anno == newItem.anno &&
                    oldItem.mese == newItem.mese &&
                    oldItem.giorno == newItem.giorno
        }

        override fun areContentsTheSame(oldItem: Spesa, newItem: Spesa): Boolean {
            return oldItem == newItem
        }
    }
}
