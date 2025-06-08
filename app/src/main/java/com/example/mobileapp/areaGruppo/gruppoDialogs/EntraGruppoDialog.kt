package com.example.mobileapp.areaGruppo.gruppoDialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.mobileapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class EntraGruppoDialog : DialogFragment() {

    private lateinit var editTextIdGruppo: EditText
    private lateinit var buttonEntra: Button
    private val db = FirebaseFirestore.getInstance()

    // Interfaccia per restituire il gruppo selezionato
    interface OnGruppoEntratoListener {
        fun onGruppoEntrato(nomeGruppo: String)
    }

    var listener: OnGruppoEntratoListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_entra_gruppo, null)
        editTextIdGruppo = view.findViewById(R.id.editTextIdGruppo)
        buttonEntra = view.findViewById(R.id.buttonEntra)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        buttonEntra.setOnClickListener {
            val idUnico = editTextIdGruppo.text.toString().trim()
            if (idUnico.isNotEmpty()) {
                uniscitiAlGruppo(idUnico)
            } else {
                Toast.makeText(requireContext(), "Inserisci un ID valido", Toast.LENGTH_SHORT).show()
            }
        }

        return dialog
    }

    private fun uniscitiAlGruppo(idUnico: String) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(requireContext(), "Utente non autenticato", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("Gruppi")
            .whereEqualTo("idUnico", idUnico)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    val gruppoDoc = doc.reference
                    val nomeGruppo = doc.getString("nome") ?: "Gruppo"

                    gruppoDoc.update("utentiID", FieldValue.arrayUnion(currentUserUid))
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Entrato nel gruppo!", Toast.LENGTH_SHORT).show()
                            listener?.onGruppoEntrato(nomeGruppo) // Chiama listener
                            dismiss()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Errore: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Gruppo non trovato", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Errore: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
