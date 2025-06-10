package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mobileapp.areaGruppo.gruppoActivities.GruppoActivity
import com.example.mobileapp.areaPersonale.singoloActivities.SoloActivity
import com.example.mobileapp.gestioneAccesso.LoginActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        val soloButton = findViewById<ImageButton>(R.id.solo)
        val gruppoButton = findViewById<ImageButton>(R.id.gruppo)
        val logoutButton = findViewById<ImageButton>(R.id.logoutButton)
        val infoButton = findViewById<ImageButton>(R.id.bottoneInfoUtente)
        val nicknameTextView = findViewById<TextView>(R.id.textViewNickname)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("Utenti").document(uid).get()
                .addOnSuccessListener { document ->
                    val nickname = document.getString("nickname") ?: "Utente"
                    nicknameTextView.text = "Ciao $nickname 👋"
                }
                .addOnFailureListener {
                    nicknameTextView.text = "Ciao!"
                }
        }

        logoutButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Conferma Logout")
                .setMessage("Sei sicuro di voler uscire?")
                .setPositiveButton("Sì") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Annulla", null)
                .show()
        }

        soloButton.setOnClickListener {
            val intent = Intent(this, SoloActivity::class.java)
            startActivity(intent)
        }

        gruppoButton.setOnClickListener {
            val intent = Intent(this, GruppoActivity::class.java)
            startActivity(intent)
        }

        infoButton.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_info_utente, null)
            val emailTextView = dialogView.findViewById<TextView>(R.id.emailTextView)
            val nicknameEditText = dialogView.findViewById<EditText>(R.id.nicknameEditText)
            val modificaButton = dialogView.findViewById<Button>(R.id.modificaButton)

            emailTextView.text = currentUser?.email ?: "Email non disponibile"

            uid?.let {
                FirebaseFirestore.getInstance().collection("Utenti").document(it).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val nickname = document.getString("nickname")
                            nicknameEditText.setText(nickname)
                        }
                    }
            }

            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Le tue informazioni")
                .setNegativeButton("Chiudi", null)
                .create()

            alertDialog.show()

            modificaButton.setOnClickListener {
                val nuovoNickname = nicknameEditText.text.toString()
                if (nuovoNickname.isNotBlank()) {
                    FirebaseFirestore.getInstance().collection("Utenti").document(uid!!)
                        .update("nickname", nuovoNickname)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Nickname aggiornato!", Toast.LENGTH_SHORT).show()
                            nicknameTextView.text = "Ciao $nuovoNickname 👋"
                            alertDialog.dismiss()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Errore aggiornamento nickname", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Nickname non valido", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
