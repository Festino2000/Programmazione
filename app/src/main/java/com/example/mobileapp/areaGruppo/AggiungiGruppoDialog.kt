package com.example.mobileapp.areaGruppo

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.mobileapp.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class AggiungiGruppoDialog : DialogFragment() {

    private lateinit var editTextTitolo: EditText
    private lateinit var editTextDescrizione: EditText
    private lateinit var buttonConferma: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_aggiungi_gruppo, null)


        editTextTitolo = view.findViewById(R.id.editTextTitolo)
            editTextDescrizione = view.findViewById(R.id.editTextDescrizione)
            buttonConferma = view.findViewById(R.id.buttonConferma)
            val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

            buttonConferma.setOnClickListener { salvaGruppo() }

            return dialog
        }

    private fun salvaGruppo() {
        val titolo = editTextTitolo.text.toString().trim()
        val descrizione = editTextDescrizione.text.toString().trim()

        if (titolo.isEmpty()) {
            Toast.makeText(requireContext(), "Il titolo non può essere vuoto", Toast.LENGTH_SHORT)
                .show()
            return
        }

        generaIDUnivoco { idUnico ->
            if (idUnico == null) {
                Toast.makeText(
                    requireContext(),
                    "Errore nella generazione dell'ID",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val gruppo = Gruppo(titolo, descrizione, idUnico)
                db.collection("Gruppi")
                    .add(gruppo)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Gruppo creato con successo",
                            Toast.LENGTH_SHORT
                        ).show()
                        parentFragmentManager.popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            requireContext(),
                            "Errore: ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun generaIDUnivoco(callback: (String?) -> Unit) {
        val tentativiMax = 10
        var tentativi = 0

        fun tenta() {
            if (tentativi >= tentativiMax) {
                callback(null)
                return
            }

            val nuovoID = String.format("%06d", Random.nextInt(1, 100001))

            db.collection("Gruppi")
                .whereEqualTo("ID", nuovoID)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        callback(nuovoID)  // ID non esiste → OK
                    } else {
                        tentativi++
                        tenta() // ID già usato → riprova
                    }
                }
                .addOnFailureListener {
                    callback(null)
                }
        }

        tenta()
    }
}

