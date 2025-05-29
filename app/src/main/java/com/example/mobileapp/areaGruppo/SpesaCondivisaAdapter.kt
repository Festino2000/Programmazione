package com.example.mobileapp.areaGruppo

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SpesaCondivisaAdapter(private val gruppoId: String, private val mappaUtenti: Map<String, String>) : RecyclerView.Adapter<SpesaCondivisaAdapter.SpesaViewHolder>() {

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
        private val buttonConferma = itemView.findViewById<Button>(R.id.buttonConfermaRicezione)

        fun bind(spesa: SpesaCondivisa) {
            val mioId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val quotaText = itemView.findViewById<TextView>(R.id.textQuotaSpesa)
            val numeroQuote = spesa.idUtentiCoinvolti.size + 1
            val quota = spesa.importo / numeroQuote
            quotaText.text = "Quota: %.2f€".format(quota)

            val layoutPartecipanti = itemView.findViewById<LinearLayout>(R.id.layoutPartecipanti)
            layoutPartecipanti.removeAllViews()
            val context = itemView.context

            textPersona.text = spesa.titolo
            textImporto.text = "${spesa.importo}€"
            textDescrizione.text = spesa.descrizione
            textData.text = String.format("%02d/%02d/%04d", spesa.giorno, spesa.mese, spesa.anno)


            buttonConferma.visibility = View.GONE
            textStato.visibility = View.VISIBLE
            textStato.setOnClickListener(null)

            // Creatore della spesa
            if (mioId == spesa.creatoreID) {
                val pendenti = spesa.pagamentiEffettuati.filterNot { spesa.pagamentiConfermati.contains(it) }
                layoutPartecipanti.visibility = View.VISIBLE

                if(pendenti.isNotEmpty() && !spesa.notificaMostrata){
                    AlertDialog.Builder(itemView.context)
                        .setTitle("Nuovo pagamento ricevuto")
                        .setMessage("Uno o più partecipanti hanno segnalato di aver pagato. Puoi ora confermare la ricezione.")
                        .setPositiveButton("OK", null)
                        .show()

                    spesa.notificaMostrata = true
                }
                for (idUtente in spesa.idUtentiCoinvolti) {
                    val riga = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, 8, 0, 8)
                    }

                    val statoPagamento = when {
                        spesa.pagamentiConfermati.contains(idUtente) -> "PAGATO"
                        spesa.pagamentiEffettuati.contains(idUtente) -> "IN ATTESA"
                        else -> "NON PAGATO"
                    }

                    val nickname = mappaUtenti[idUtente] ?: idUtente
                    val textUtente = TextView(context).apply {
                        text = "$nickname - $statoPagamento"
                        textSize = 14f
                        setPadding(8, 0, 16, 0)
                    }

                    val bottone = Button(context).apply {
                        text = when {
                            spesa.pagamentiConfermati.contains(idUtente) -> "Confermato"
                            spesa.pagamentiEffettuati.contains(idUtente) -> "Conferma"
                            else -> ""
                        }

                        visibility = if (spesa.pagamentiEffettuati.contains(idUtente)
                            && !spesa.pagamentiConfermati.contains(idUtente)) View.VISIBLE else View.GONE

                        textSize = 12f
                        setOnClickListener {
                            spesa.pagamentiConfermati.add(idUtente)
                            aggiornaSuFirestore(spesa)
                            notifyItemChanged(adapterPosition)
                        }
                    }

                    riga.addView(textUtente)
                    riga.addView(bottone)
                    layoutPartecipanti.addView(riga)
                }

                // Stato generale per il creatore
                if (spesa.pagamentiConfermati.containsAll(spesa.idUtentiCoinvolti)) {
                    textStato.text = "PAGATO"
                    textStato.setBackgroundResource(R.drawable.text_pagato)
                } else {
                    textStato.text = "NON PAGATO"
                    textStato.setBackgroundResource(R.drawable.text_non_pagato)
                }

            } else if (spesa.idUtentiCoinvolti.contains(mioId)) {
                // Partecipante della spesa
                layoutPartecipanti.visibility = View.GONE
                when {
                    spesa.pagamentiConfermati.contains(mioId) -> {
                        textStato.text = "PAGATO"
                        textStato.setBackgroundResource(R.drawable.text_pagato)
                    }
                    spesa.pagamentiEffettuati.contains(mioId) -> {
                        textStato.text = "In attesa conferma"
                        textStato.setBackgroundResource(R.drawable.text_attesa_conferma)
                    }
                    else -> {
                        textStato.text = "NON PAGATO"
                        textStato.setBackgroundResource(R.drawable.text_non_pagato)
                        textStato.setOnClickListener {
                            spesa.pagamentiEffettuati.add(mioId)
                            aggiornaSuFirestore(spesa)
                            AlertDialog.Builder(itemView.context)
                                .setTitle("Pagamento Inviato")
                                .setMessage("Hai notificato il creatore che hai effettuato il pagamento.")
                                .setPositiveButton("OK", null)
                                .show()
                            notifyItemChanged(adapterPosition)
                        }
                    }
                }

            } else {
                // Nessun coinvolgimento
                layoutPartecipanti.visibility = View.GONE
                textStato.visibility = View.GONE
                buttonConferma.visibility = View.GONE
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
