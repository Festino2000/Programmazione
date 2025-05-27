package com.example.mobileapp.areaGruppo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.FabMenuController
import com.example.mobileapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SpesaCondivisaFragment : Fragment(R.layout.fragment_spese_condivise) {

    private lateinit var recyclerViewDaPagare: RecyclerView
    private lateinit var recyclerViewDaRicevere: RecyclerView
    private lateinit var adapterDaPagare: SpesaCondivisaAdapter
    private lateinit var adapterDaRicevere: SpesaCondivisaAdapter
    private lateinit var fabAggiungiSpesa: FloatingActionButton
    private lateinit var idGruppoFirestore: String


    private val listaSpese = mutableListOf<SpesaCondivisa>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_spese_condivise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewDaPagare = view.findViewById(R.id.recyclerViewDaPagare)
        recyclerViewDaRicevere = view.findViewById(R.id.recyclerViewDaRicevere)
        fabAggiungiSpesa = view.findViewById(R.id.fabAggiungiSpesa)

        recyclerViewDaPagare.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewDaRicevere.layoutManager = LinearLayoutManager(requireContext())

        val idGruppo = arguments?.getString("idGruppo") ?: return

        FirebaseFirestore.getInstance().collection("Gruppi")
            .whereEqualTo("idUnico", idGruppo)
            .get()
            .addOnSuccessListener { result ->
                val document = result.documents.firstOrNull()
                if (document == null) {
                    Toast.makeText(requireContext(), "Gruppo non trovato", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                idGruppoFirestore = document.id
                adapterDaPagare = SpesaCondivisaAdapter(idGruppoFirestore)
                adapterDaRicevere = SpesaCondivisaAdapter(idGruppoFirestore)

                recyclerViewDaPagare.adapter = adapterDaPagare
                recyclerViewDaRicevere.adapter = adapterDaRicevere
                caricaSpese(idGruppoFirestore, view)

        fabAggiungiSpesa.setOnClickListener {
            val utentiID = document?.get("utentiID") as? List<String> ?: emptyList()

            FirebaseFirestore.getInstance().collection("Utenti")
                .whereIn("utenteID", utentiID)
                .get()
                .addOnSuccessListener { utentiDocs ->
                    val mioId = FirebaseAuth.getInstance().currentUser?.uid
                    val listaUtenti = utentiDocs.mapNotNull {
                        val id = it.getString("utenteID")
                        val nick = it.getString("nickname")
                        if (id != null && nick != null && id != mioId) Utente(id, nick) else null

                    /*val listaUtenti = utentiDocs.mapNotNull {
                        val id = it.getString("utenteID")
                        val nick = it.getString("nickname")
                        if (id != null && nick != null) Utente(id, nick) else null*/
                    }

                    val dialog = AggiungiSpesaDialog(listaUtenti) { nuovaSpesa ->
                        val mioId = FirebaseAuth.getInstance().currentUser?.uid ?: return@AggiungiSpesaDialog

                        val spesaConAutore = nuovaSpesa.copy(
                            creatoreID = mioId,
                            pagamentiEffettuati = mutableListOf(),
                            pagamentiConfermati = mutableListOf()
                        )
                        listaSpese.add(spesaConAutore)
                        aggiornaTotali(view, listaSpese)

                        adapterDaRicevere.submitList(listaSpese.filter { it.idUtentiCoinvolti.contains(mioId) })
                        adapterDaPagare.submitList(listaSpese.filter { !it.idUtentiCoinvolti.contains(mioId) })

                        // Facoltativo: salvataggio su Firestore
                        FirebaseFirestore.getInstance()
                            .collection("Gruppi")
                            .document(document!!.id)
                            .collection("Spese")
                            .add(spesaConAutore)
                    }
                    dialog.show(parentFragmentManager, "AggiungiSpesaDialog")
                }
            }
        }
    }
    private fun caricaSpese(gruppoId: String, view: View) {
        val mioId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("Gruppi")
            .document(gruppoId)
            .collection("Spese")
            .get()
            .addOnSuccessListener { result ->
                val speseCaricate = result.documents.mapNotNull { doc ->
                    val spesa = doc.toObject(SpesaCondivisa::class.java)
                    spesa?.apply { idDocumento = doc.id }
                }

                listaSpese.clear()
                listaSpese.addAll(speseCaricate)

                // Aggiorna RecyclerView e Totali
                adapterDaRicevere.submitList(listaSpese.filter { it.idUtentiCoinvolti.contains(mioId) })
                adapterDaPagare.submitList(listaSpese.filter { !it.idUtentiCoinvolti.contains(mioId) })

                aggiornaTotali(view, listaSpese)
            }
    }

    private fun aggiornaTotali(view: View, spese: List<SpesaCondivisa>) {
        val mioId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        var totalePagare = 0f
        var totaleRicevere = 0f

        for (spesa in spese) {
            if (spesa.idUtentiCoinvolti.contains(mioId)) {
                totalePagare += spesa.importo / spesa.idUtentiCoinvolti.size
            } else {
                totaleRicevere += spesa.importo
            }
        }

        val totalePagareView = view.findViewById<TextView>(R.id.totaleDaPagare)
        val totaleRicevereView = view.findViewById<TextView>(R.id.totaleDaRicevere)

        totalePagareView.text = if (totalePagare > 0) "-${"%.2f".format(totalePagare)}€" else "0€"
        totaleRicevereView.text = if (totaleRicevere > 0) "+${"%.2f".format(totaleRicevere)}€" else "0€"
    }
}
