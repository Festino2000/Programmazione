package com.example.mobileapp.areaGruppo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        adapterDaPagare = SpesaCondivisaAdapter()
        adapterDaRicevere = SpesaCondivisaAdapter()

        recyclerViewDaPagare.adapter = adapterDaPagare
        recyclerViewDaRicevere.adapter = adapterDaRicevere

        fabAggiungiSpesa.setOnClickListener {
            val idGruppo = arguments?.getString("idGruppo") ?: return@setOnClickListener

            FirebaseFirestore.getInstance().collection("Gruppi")
                .whereEqualTo("idUnico", idGruppo)
                .get()
                .addOnSuccessListener { result ->
                    val document = result.documents.firstOrNull()
                    val utentiID = document?.get("utentiID") as? List<String> ?: emptyList()

                    FirebaseFirestore.getInstance().collection("Utenti")
                        .whereIn("utenteID", utentiID)
                        .get()
                        .addOnSuccessListener { utentiDocs ->
                            val listaUtenti = utentiDocs.mapNotNull {
                                val id = it.getString("utenteID")
                                val nick = it.getString("nickname")
                                if (id != null && nick != null) Utente(id, nick) else null
                            }

                            val dialog = AggiungiSpesaDialog(listaUtenti) { nuovaSpesa ->
                                listaSpese.add(nuovaSpesa)
                                aggiornaTotali(view, listaSpese)

                                val mioId = FirebaseAuth.getInstance().currentUser?.uid ?: return@AggiungiSpesaDialog
                                adapterDaPagare.submitList(listaSpese.filter { it.idUtentiCoinvolti.contains(mioId) })
                                adapterDaRicevere.submitList(listaSpese.filter { !it.idUtentiCoinvolti.contains(mioId) })

                                // Facoltativo: salvataggio su Firestore
                                FirebaseFirestore.getInstance()
                                    .collection("Gruppi")
                                    .document(document!!.id)
                                    .collection("Spese")
                                    .add(nuovaSpesa)
                            }
                            dialog.show(parentFragmentManager, "AggiungiSpesaDialog")
                        }
                }
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
