package com.example.mobileapp.areaGruppo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SaldatiFragment : Fragment(R.layout.fragment_saldati) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SpesaCondivisaAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewSaldati)

        val idGruppo = arguments?.getString("idGruppo") ?: return
        adapter = SpesaCondivisaAdapter(idGruppo)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        caricaSpeseSaldati(idGruppo)
    }
    private fun caricaSpeseSaldati(idGruppo: String) {
        val mioId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("Gruppi")
            .document(idGruppo)
            .collection("Spese")
            .get()
            .addOnSuccessListener { result ->
                val speseSaldate = result.documents.mapNotNull { doc ->
                    doc.toObject(SpesaCondivisa::class.java)?.let { spesa ->
                        spesa.idDocumento = doc.id
                        spesa // ← questa riga è fondamentale
                    }
                }.filter { spesa ->
                    // Sei creatore: mostra se ci sono pagamenti da confermare o tutti già confermati
                    if (spesa.creatoreID == mioId) {
                        spesa.pagamentiEffettuati.isNotEmpty()
                    } else {
                        // Sei partecipante: mostra se hai già pagato
                        spesa.pagamentiEffettuati.contains(mioId)
                    }
                }
                adapter.submitList(speseSaldate)
            }
    }

}