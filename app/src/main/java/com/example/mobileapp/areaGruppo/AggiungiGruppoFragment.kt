package com.example.mobileapp.areaGruppo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mobileapp.R
import com.google.firebase.firestore.FirebaseFirestore

class AggiungiGruppoFragment : Fragment() {

    private lateinit var editTextTitolo: EditText
    private lateinit var editTextDescrizione: EditText
    private lateinit var buttonConferma: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_aggiungi_gruppo, container, false)

        editTextTitolo = view.findViewById(R.id.editTextTitolo)
        editTextDescrizione = view.findViewById(R.id.editTextDescrizione)
        buttonConferma = view.findViewById(R.id.buttonConferma)

        buttonConferma.setOnClickListener { salvaGruppo() }

        return view
    }

    private fun salvaGruppo() {
        val titolo = editTextTitolo.text.toString().trim()
        val descrizione = editTextDescrizione.text.toString().trim()

        if (titolo.isEmpty()) {
            Toast.makeText(requireContext(), "Il titolo non può essere vuoto", Toast.LENGTH_SHORT).show()
            return
        }

        val gruppo = Gruppo(titolo, descrizione)

        db.collection("Gruppi")
            .add(gruppo)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Gruppo creato con successo", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Errore: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

