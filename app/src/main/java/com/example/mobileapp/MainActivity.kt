package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.mobileapp.areaGruppo.GruppoActivity
import com.example.mobileapp.areaPersonale.SoloActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AlertDialog
import com.example.mobileapp.gestioneAccesso.LoginActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        val soloButton = findViewById<ImageButton>(R.id.solo)
        val gruppoButton = findViewById<ImageButton>(R.id.gruppo)
        val logoutButton = findViewById<ImageButton>(R.id.logoutButton)

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
    }
}

