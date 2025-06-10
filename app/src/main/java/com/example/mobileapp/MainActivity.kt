package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mobileapp.areaGruppo.gruppoActivities.GruppoActivity
import com.example.mobileapp.areaPersonale.singoloActivities.SoloActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AlertDialog
import com.example.mobileapp.gestioneAccesso.LoginActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Imposta il layout dell'activity

        FirebaseApp.initializeApp(this) // Inizializza Firebase

        // Recupera i riferimenti ai bottoni dall'interfaccia
        val soloButton = findViewById<ImageButton>(R.id.solo)
        val gruppoButton = findViewById<ImageButton>(R.id.gruppo)
        val logoutButton = findViewById<ImageButton>(R.id.logoutButton)

        // Riferimento alla TextView dove viene mostrato il nickname
        val nicknameTextView = findViewById<TextView>(R.id.textViewNickname)
        // Ottiene l'utente corrente autenticato
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid

        // Se l'utente è autenticato, recupera il nickname dal database Firestore
        if (uid != null) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("Utenti").document(uid).get()
                .addOnSuccessListener { document ->
                    val nickname = document.getString("nickname") ?: "Utente"
                    nicknameTextView.text = "Ciao $nickname 👋"
                }
                .addOnFailureListener {
                    nicknameTextView.text = "Ciao!"
                }
        }

        // Gestione click sul bottone di logout
        logoutButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Conferma Logout")
                .setMessage("Sei sicuro di voler uscire?")
                .setPositiveButton("Sì") { _, _ ->
                    FirebaseAuth.getInstance().signOut() // Esegue il logout da Firebase
                    val intent = Intent(this, LoginActivity::class.java)
                    // Pulisce la cronologia delle activity
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent) // Torna alla schermata di login
                }
                .setNegativeButton("Annulla", null)
                .show()
        }

        // Gestione click sul bottone "Solo"
        soloButton.setOnClickListener {
            val intent = Intent(this, SoloActivity::class.java)
            startActivity(intent) // Avvia l'activity personale
        }

        // Gestione click sul bottone "Gruppo"
        gruppoButton.setOnClickListener {
            val intent = Intent(this, GruppoActivity::class.java)
            startActivity(intent) // Avvia l'activity di gruppo
        }
    }
}
