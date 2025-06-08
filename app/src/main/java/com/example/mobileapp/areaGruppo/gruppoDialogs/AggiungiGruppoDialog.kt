package com.example.mobileapp.areaGruppo.gruppoDialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.mobileapp.R
import com.example.mobileapp.areaGruppo.gruppoDataClasses.Gruppo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class AggiungiGruppoDialog : DialogFragment() {

    private lateinit var editTextTitolo: EditText
    private lateinit var editTextDescrizione: EditText
    private lateinit var buttonConferma: Button
    var listener: OnGruppoCreatoListener? = null

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
            Toast.makeText(requireContext(), "Il titolo non può essere vuoto", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(requireContext(), "Errore: utente non autenticato", Toast.LENGTH_SHORT).show()
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
                val gruppo = Gruppo(
                    titolo = titolo,
                    descrizione = descrizione,
                    idUnico = idUnico,
                    utentiID = listOf(currentUserUid)
                )

                db.collection("Gruppi")
                    .add(gruppo)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Gruppo creato con successo",
                            Toast.LENGTH_SHORT
                        ).show()
                        listener?.onGruppoCreato(gruppo)
                        dismiss()
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
    interface OnGruppoCreatoListener {
        fun onGruppoCreato(gruppo: Gruppo)
    }
}

