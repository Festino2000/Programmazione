package com.example.mobileapp.areaGruppo

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.mobileapp.R
import com.google.firebase.firestore.FirebaseFirestore

class StatisticheFragment : Fragment(R.layout.fragment_statistiche_nuovo) {

    private lateinit var idGruppoFirestore: String
    private val listaSpese = mutableListOf<SpesaCondivisa>()
    private val uidToNickname = mutableMapOf<String, String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val idGruppo = arguments?.getString("idGruppo") ?: return
        idGruppoFirestore = idGruppo

        caricaSpese(idGruppoFirestore, view)
    }

    private fun caricaSpese(gruppoId: String, view: View) {
        FirebaseFirestore.getInstance()
            .collection("Gruppi")
            .whereEqualTo("idUnico", gruppoId)
            .get()
            .addOnSuccessListener { result ->
                val documento = result.documents.firstOrNull()
                if (documento == null) {
                    Log.e("StatisticheFragment", "Nessun gruppo trovato con idUnico: $gruppoId")
                    return@addOnSuccessListener
                }

                val docId = documento.id

                FirebaseFirestore.getInstance()
                    .collection("Gruppi")
                    .document(docId)
                    .collection("Spese")
                    .get()
                    .addOnSuccessListener { speseResult ->
                        val speseCaricate = speseResult.documents.mapNotNull { doc ->
                            val spesa = doc.toObject(SpesaCondivisa::class.java)
                            spesa?.apply { idDocumento = doc.id }
                        }

                        listaSpese.clear()
                        listaSpese.addAll(speseCaricate)

                        Log.d("StatisticheFragment", "Spese trovate: ${listaSpese.size}")
                        caricaNicknameUtenti(view)
                    }
                    .addOnFailureListener {
                        Log.e("StatisticheFragment", "Errore nel caricamento spese", it)
                    }
            }
            .addOnFailureListener {
                Log.e("StatisticheFragment", "Errore nel recupero del gruppo", it)
            }
    }

    private fun caricaNicknameUtenti(view: View) {
        val db = FirebaseFirestore.getInstance()
        val utentiCoinvolti = listaSpese.flatMap { it.idUtentiCoinvolti + it.creatoreID }.toSet()

        db.collection("Utenti")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val uid = doc.id
                    val nickname = doc.getString("nickname") ?: "Sconosciuto"
                    if (uid in utentiCoinvolti) {
                        uidToNickname[uid] = nickname
                    }
                }
                aggiornaStatistiche(view)
            }
            .addOnFailureListener {
                Log.e("StatisticheFragment", "Errore nel caricamento dei nickname", it)
                aggiornaStatistiche(view)
            }
    }

    fun aggiornaStatistiche(view: View) {
        val totale = calcolaTotaleSpese()
        val totalePerUtente = calcolaTotalePerUtente()
        val saldoPerUtente = calcolaSaldoPerUtente()
        val spesaMedia = if (saldoPerUtente.isNotEmpty()) totale / saldoPerUtente.size else 0.0

        view.findViewById<TextView>(R.id.valoreTotaleSpese).text = "Totale spese: %.2f€".format(totale)
        view.findViewById<TextView>(R.id.valoreSpesaMedia).text = "Spesa media per utente: %.2f€".format(spesaMedia)

        val textTotalePerUtente = view.findViewById<TextView>(R.id.valoreTotalePerUtente)
        textTotalePerUtente.text = totalePerUtente.entries.joinToString("\n") {
            "${uidToNickname[it.key] ?: it.key}: %.2f€".format(it.value)
        }

        val textSaldoPerUtente = view.findViewById<TextView>(R.id.valoreSaldoPerUtente)
        textSaldoPerUtente.text = saldoPerUtente.entries.joinToString("\n") {
            "${uidToNickname[it.key] ?: it.key}: %.2f€".format(it.value)
        }
    }

    private fun calcolaTotaleSpese(): Double {
        return listaSpese.sumOf { it.importo }
    }

    private fun calcolaTotalePerUtente(): Map<String, Double> {
        return listaSpese.groupBy { it.creatoreID }
            .mapValues { entry -> entry.value.sumOf { it.importo } }
    }

    private fun calcolaSaldoPerUtente(): Map<String, Double> {
        val saldo = mutableMapOf<String, Double>()
        for (spesa in listaSpese) {
            val quota = spesa.importo / spesa.idUtentiCoinvolti.size
            for (uid in spesa.idUtentiCoinvolti) {
                saldo[uid] = saldo.getOrDefault(uid, 0.0) - quota
            }
            saldo[spesa.creatoreID] = saldo.getOrDefault(spesa.creatoreID, 0.0) + spesa.importo
        }
        return saldo
    }
}



