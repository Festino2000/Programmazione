package com.example.mobileapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class AggiungiSpesaFragment : Fragment(R.layout.fragment_aggiungi_spesa) {

    private lateinit var callback: OnSpesaAggiuntaListener

    // Interfaccia per inviare i dati all'Activity
    interface OnSpesaAggiuntaListener {
        fun onSpesaAggiunta(spesa: Spesa)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSpesaAggiuntaListener) {
            callback = context
        } else {
            throw RuntimeException("$context deve implementare OnSpesaAggiuntaListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_aggiungi_spesa, container, false)

        // Riferimenti agli elementi del layout
        val titoloSpesa = view.findViewById<EditText>(R.id.titoloSpesa)
        val descrizioneSpesa = view.findViewById<EditText>(R.id.descrizioneSpesa)
        val giornoSpesa = view.findViewById<EditText>(R.id.giornoSpesa)
        val meseSpesa = view.findViewById<EditText>(R.id.meseSpesa)
        val annoSpesa = view.findViewById<EditText>(R.id.annoSpesa)
        val importoSpesa = view.findViewById<EditText>(R.id.importoSpesa)
        val categoriaSpesa = view.findViewById<EditText>(R.id.categoriaSpesa)
        val btnConferma = view.findViewById<Button>(R.id.btnConfermaSpesa)

        btnConferma.setOnClickListener {
            val titolo = titoloSpesa.text.toString()
            val descrizione = descrizioneSpesa.text.toString()
            val giorno = giornoSpesa.text.toString().toIntOrNull() ?: 0
            val mese = meseSpesa.text.toString().toIntOrNull() ?: 0
            val anno = annoSpesa.text.toString().toIntOrNull() ?: 0
            val importo = importoSpesa.text.toString().toFloatOrNull() ?: 0.0f
            val categoria = categoriaSpesa.text.toString()

            if (titolo.isBlank() || importo == 0.0f) {
                Toast.makeText(requireContext(), "Compila almeno il titolo e l'importo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Creazione dell'oggetto Spesa
            val nuovaSpesa = Spesa(titolo, descrizione, giorno, mese, anno, importo, categoria)

            // Invio dei dati alla SoloActivity
            callback.onSpesaAggiunta(nuovaSpesa)

            // Torna alla schermata precedente
            parentFragmentManager.popBackStack()
        }

        return view
    }
}
