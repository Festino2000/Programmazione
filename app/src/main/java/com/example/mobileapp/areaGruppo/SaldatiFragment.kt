package com.example.mobileapp.areaGruppo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val idGruppo = arguments?.getString("idGruppo") ?: return

        FirebaseFirestore.getInstance().collection("Gruppi")
            .whereEqualTo("idUnico", idGruppo)
            .get()
            .addOnSuccessListener { gruppoResult ->
                val document = gruppoResult.documents.firstOrNull()
                if (document == null) return@addOnSuccessListener

                val utentiID = document.get("utentiID") as? List<String> ?: return@addOnSuccessListener

                FirebaseFirestore.getInstance().collection("Utenti")
                    .whereIn("utenteID", utentiID)
                    .get()
                    .addOnSuccessListener { utentiDocs ->
                        val mappaUtenti = utentiDocs.mapNotNull {
                            val id = it.getString("utenteID")
                            val nick = it.getString("nickname")
                            if (id != null && nick != null) id to nick else null
                        }.toMap()

                        adapter = SpesaCondivisaAdapter(document.id, mappaUtenti)
                        recyclerView.adapter = adapter

                        caricaSpeseSaldati(document.id, mappaUtenti)
                    }
            }
    }

    private fun caricaSpeseSaldati(idGruppo: String, mappaUtenti: Map<String, String>) {
        val mioId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("Gruppi")
            .document(idGruppo)
            .collection("Spese")
            .get()
            .addOnSuccessListener { result ->
                val speseSaldate = result.documents.mapNotNull { doc ->
                    doc.toObject(SpesaCondivisa::class.java)?.apply {
                        idDocumento = doc.id
                    }
                }.filter { spesa ->
                    if (spesa.creatoreID == mioId) {
                        spesa.pagamentiEffettuati.isNotEmpty()
                    } else {
                        spesa.pagamentiEffettuati.contains(mioId)
                    }
                }

                adapter.submitList(speseSaldate)
            }
    }
}
