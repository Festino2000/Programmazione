package com.example.mobileapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class AggiungiSpesaFragment : Fragment(R.layout.fragment_aggiungi_spesa) {

    private lateinit var callback: OnSpesaAggiuntaListener
    private lateinit var db: FirebaseFirestore

    interface OnSpesaAggiuntaListener {
        fun onSpesaAggiunta(spesa: Spesa)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSpesaAggiuntaListener) {
            callback = context
            Log.d("AggiungiSpesaFragment", "Callback collegata correttamente")
        } else {
            throw RuntimeException("$context deve implementare OnSpesaAggiuntaListener")
        }
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_aggiungi_spesa, container, false)

        val titoloSpesa = view.findViewById<EditText>(R.id.titoloSpesa)
        val descrizioneSpesa = view.findViewById<EditText>(R.id.descrizioneSpesa)
        val giornoSpesa = view.findViewById<EditText>(R.id.giornoSpesa)
        val meseSpesa = view.findViewById<EditText>(R.id.meseSpesa)
        val annoSpesa = view.findViewById<EditText>(R.id.annoSpesa)
        val importoSpesa = view.findViewById<EditText>(R.id.importoSpesa)
        val categoriaSpesa = view.findViewById<EditText>(R.id.categoriaSpesa)
        val btnConferma = view.findViewById<Button>(R.id.btnConfermaSpesa)

        btnConferma.setOnClickListener {
            Log.d("AggiungiSpesaFragment", "Pulsante Conferma Spesa cliccato")

            val titolo = titoloSpesa.text.toString()
            val descrizione = descrizioneSpesa.text.toString()
            val giorno = giornoSpesa.text.toString().toIntOrNull() ?: 0
            val mese = meseSpesa.text.toString().toIntOrNull() ?: 0
            val anno = annoSpesa.text.toString().toIntOrNull() ?: 0
            val importo = importoSpesa.text.toString().toFloatOrNull() ?: 0.0f
            val categoria = categoriaSpesa.text.toString()

            if (titolo.isBlank() || importo == 0.0f) {
                Toast.makeText(requireContext(), "Compila almeno il titolo e l'importo", Toast.LENGTH_SHORT).show()
                Log.e("AggiungiSpesaFragment", "Dati non validi: Titolo o Importo vuoti")
                return@setOnClickListener
            }

            // Crea una mappa con i dati della spesa
            val nuovaSpesa = hashMapOf(
                "titolo" to titolo,
                "descrizione" to descrizione,
                "giorno" to giorno,
                "mese" to mese,
                "anno" to anno,
                "importo" to importo,
                "categoria" to categoria,
                "data" to Timestamp.now() // Usa il timestamp corrente per la data
            )

            // Aggiungi la spesa a Firestore
            db.collection("Spese") // Nome collezione con la "S" maiuscola
                .add(nuovaSpesa)
                .addOnSuccessListener { documentReference ->
                    Log.d("AggiungiSpesaFragment", "Spesa salvata su Firestore con ID: ${documentReference.id}")
                    Toast.makeText(requireContext(), "Spesa Aggiunta Con Successo!", Toast.LENGTH_SHORT).show()

                    // Chiamata alla callback per aggiornare l'Activity
                    callback.onSpesaAggiunta(Spesa(titolo, descrizione, giorno, mese, anno, importo, categoria))

                    try {
                        // Verifica se l'activity è effettivamente SoloActivity e chiudi il fragment
                        val soloActivity = requireActivity() as? SoloActivity
                        if (soloActivity != null) {
                            soloActivity.chiudiFragment()
                            Log.d("AggiungiSpesaFragment", "Fragment chiuso tramite SoloActivity")
                        } else {
                            Log.e("AggiungiSpesaFragment", "Errore: L'activity non è di tipo SoloActivity")
                        }
                    } catch (e: Exception) {
                        Log.e("AggiungiSpesaFragment", "Errore nella chiusura del fragment: ${e.message}")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AggiungiSpesaFragment", "Errore nel salvataggio su Firestore", e)
                    Toast.makeText(requireContext(), "Errore nel salvataggio su Firestore", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }
}





