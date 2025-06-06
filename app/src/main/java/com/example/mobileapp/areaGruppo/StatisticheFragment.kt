package com.example.mobileapp.areaGruppo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.mobileapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StatisticheFragment : Fragment(R.layout.fragment_statistiche_nuovo) {

    private lateinit var idGruppoFirestore: String
    private val listaSpese = mutableListOf<SpesaCondivisa>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val idGruppo = arguments?.getString("idGruppo") ?: return
        idGruppoFirestore = idGruppo

        caricaSpese(idGruppoFirestore, view)
    }

    private fun caricaSpese(gruppoId: String, view: View) {
        // Cerchiamo il gruppo tramite il campo idUnico
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

                // Una volta trovato l'ID corretto, carichiamo le spese
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
                        aggiornaStatistiche(view)
                    }
                    .addOnFailureListener {
                        Log.e("StatisticheFragment", "Errore nel caricamento spese", it)
                    }
            }
            .addOnFailureListener {
                Log.e("StatisticheFragment", "Errore nel recupero del gruppo", it)
            }
    }


    fun aggiornaStatistiche(view: View) {
        val totale = calcolaTotaleSpese()
        val textTotale = view.findViewById<TextView>(R.id.valoreTotaleSpese)
        textTotale.text = "Totale spese: %.2f€".format(totale)
    }

    private fun calcolaTotaleSpese(): Double {
        var totale = 0.0
        for (spesa in listaSpese) {
            totale += spesa.importo
        }
        return totale
    }
}


