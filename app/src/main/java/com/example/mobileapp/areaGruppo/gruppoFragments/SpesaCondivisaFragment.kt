package com.example.mobileapp.areaGruppo.gruppoFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.example.mobileapp.adapters.SpesaCondivisaAdapter
import com.example.mobileapp.areaGruppo.gruppoDialogs.AggiungiSpesaDialog
import com.example.mobileapp.areaGruppo.gruppoDataClasses.SpesaCondivisa
import com.example.mobileapp.areaGruppo.gruppoDataClasses.Utente
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
        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(requireActivity().findViewById(R.id.toolbar2))

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
                val utentiId = document?.get("utentiID") as? List<String> ?: emptyList()
                if (document == null) {
                    Toast.makeText(requireContext(), "Gruppo non trovato", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                idGruppoFirestore = document.id
                FirebaseFirestore.getInstance().collection("Utenti")
                    .whereIn("utenteID", utentiId)
                    .get()
                    .addOnSuccessListener { utentiDocs ->
                        val mioId = FirebaseAuth.getInstance().currentUser?.uid
                        val listaUtenti = utentiDocs.mapNotNull {
                            val id = it.getString("utenteID")
                            val nick = it.getString("nickname")
                            if (id != null && nick != null && id != mioId) Utente(id, nick) else null

                        }
                        val mappaUtenti = utentiDocs.mapNotNull {
                            val id = it.getString("utenteID")
                            val nick = it.getString("nickname")
                            if (id != null && nick != null) id to nick else null
                        }.toMap()

                        adapterDaPagare = SpesaCondivisaAdapter(idGruppoFirestore, mappaUtenti)
                        adapterDaRicevere = SpesaCondivisaAdapter(idGruppoFirestore, mappaUtenti)

                        recyclerViewDaPagare.adapter = adapterDaPagare
                        recyclerViewDaRicevere.adapter = adapterDaRicevere
                        caricaSpese(idGruppoFirestore, view)


        fabAggiungiSpesa.setOnClickListener {

                    val dialog = AggiungiSpesaDialog(listaUtenti) { nuovaSpesa ->
                        val mioId = FirebaseAuth.getInstance().currentUser?.uid ?: return@AggiungiSpesaDialog

                        val spesaConAutore = nuovaSpesa.copy(
                            creatoreID = mioId,
                            pagamentiEffettuati = mutableListOf(),
                            pagamentiConfermati = mutableListOf()
                        )
                        listaSpese.add(spesaConAutore)
                        aggiornaTotali(view, listaSpese)

                        adapterDaRicevere.submitList(listaSpese.filter {it.creatoreID == mioId})
                        adapterDaPagare.submitList(listaSpese.filter { it.idUtentiCoinvolti.contains(mioId)  })

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

        Log.d("DEBUG", "Avvio caricamento spese per gruppo $gruppoId")

        FirebaseFirestore.getInstance()
            .collection("Gruppi")
            .document(gruppoId)
            .collection("Spese")
            .get()
            .addOnSuccessListener { result ->
                Log.d("DEBUG", "Numero documenti trovati: ${result.documents.size}")

                val speseCaricate = result.documents.mapNotNull { doc ->
                    val spesa = doc.toObject(SpesaCondivisa::class.java)
                    Log.d("DEBUG", "Doc ${doc.id} → importo: ${spesa?.importo}")
                    spesa?.apply { idDocumento = doc.id }
                }

                listaSpese.clear()
                listaSpese.addAll(speseCaricate)

                Log.d("DEBUG", "Spese parse correttamente: ${listaSpese.size}")
            }
            .addOnFailureListener {
                Log.e("DEBUG", "Errore nel caricamento spese", it)
            }

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

                val speseNonSaldate = listaSpese.filterNot { spesa ->
                    spesa.pagamentiConfermati.containsAll(spesa.idUtentiCoinvolti)
                }

                // Mostra solo le spese rilevanti
                adapterDaPagare.submitList(
                    speseNonSaldate.filter {
                        it.idUtentiCoinvolti.contains(mioId) && it.creatoreID != mioId
                    }
                )

                adapterDaRicevere.submitList(
                    speseNonSaldate.filter { it.creatoreID == mioId }
                )

                aggiornaTotali(view, speseNonSaldate)
            }
    }

    private fun aggiornaTotali(view: View, spese: List<SpesaCondivisa>) {
        val mioId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        var totalePagare = 0.0
        var totaleRicevere = 0.0

        for (spesa in spese) {
            val numQuote = spesa.idUtentiCoinvolti.size + 1
            val tuttiPagato = spesa.pagamentiConfermati.containsAll(spesa.idUtentiCoinvolti)
            if (tuttiPagato) continue
            if (spesa.idUtentiCoinvolti.contains(mioId)) {
                totalePagare += spesa.importo / (numQuote)
            } else {
                totaleRicevere += ((spesa.importo / numQuote) * spesa.idUtentiCoinvolti.size)
            }
        }

        val totalePagareView = view.findViewById<TextView>(R.id.totaleDaPagare)
        val totaleRicevereView = view.findViewById<TextView>(R.id.totaleDaRicevere)

        totalePagareView.text = if (totalePagare > 0) "-${"%.2f".format(totalePagare)}€" else "0€"
        totaleRicevereView.text = if (totaleRicevere > 0) "+${"%.2f".format(totaleRicevere)}€" else "0€"
    }
}
