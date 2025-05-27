package com.example.mobileapp.areaGruppo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SpesaCondivisaAdapter(private val gruppoId: String) : RecyclerView.Adapter<SpesaCondivisaAdapter.SpesaViewHolder>() {

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
            val mioId = FirebaseAuth.getInstance().currentUser?.uid ?: return

            textPersona.text = spesa.titolo
            textImporto.text = "${spesa.importo}€"
            textDescrizione.text = spesa.descrizione
            textData.text = String.format("%02d/%02d/%04d", spesa.giorno, spesa.mese, spesa.anno)

            if (mioId == spesa.creatoreID) {
                // Sei il creatore della spesa (creditore)
                val pendenti = spesa.pagamentiEffettuati.filterNot { spesa.pagamentiConfermati.contains(it) }
                if (pendenti.isNotEmpty()) {
                    textStato.text = "Conferma ricezione"
                    textStato.setBackgroundResource(R.drawable.text_attesa_conferma)
                    textStato.setOnClickListener {
                        spesa.pagamentiConfermati.addAll(pendenti)
                        aggiornaSuFirestore(spesa)
                        notifyItemChanged(adapterPosition)
                    }
                } else {
                    textStato.text = "NON PAGATO"
                    textStato.setBackgroundResource(R.drawable.text_non_pagato)
                    textStato.setOnClickListener(null)
                }
            } else if (spesa.idUtentiCoinvolti.contains(mioId)) {
                // Sei un partecipante
                if (spesa.pagamentiEffettuati.contains(mioId)) {
                    textStato.text = "In attesa conferma"
                    textStato.setBackgroundResource(R.drawable.text_attesa_conferma)
                    textStato.setOnClickListener(null)
                } else {
                    textStato.text = "NON PAGATO"
                    textStato.setBackgroundResource(R.drawable.text_non_pagato)
                    textStato.setOnClickListener {
                        spesa.pagamentiEffettuati.add(mioId)
                        aggiornaSuFirestore(spesa)
                        notifyItemChanged(adapterPosition)
                    }
                }
            }
            // Caso finale: tutto confermato
            if (spesa.pagamentiConfermati.contains(mioId) || (mioId == spesa.creatoreID && spesa.pagamentiConfermati.containsAll(spesa.idUtentiCoinvolti.filter { it != mioId }))) {
                textStato.text = "PAGATO"
                textStato.setBackgroundResource(R.drawable.text_pagato)
                textStato.setOnClickListener(null)
            }

        }
        }
    private fun aggiornaSuFirestore(spesa: SpesaCondivisa) {
        val firestore = FirebaseFirestore.getInstance()
        val docId = spesa.idDocumento
        if (docId != null) {
            firestore.collection("Gruppi")
                .document(gruppoId)
                .collection("Spese")
                .document(docId)
                .set(spesa)
        }
    }
}
