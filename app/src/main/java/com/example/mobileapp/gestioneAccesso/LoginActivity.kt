package com.example.mobileapp.gestioneAccesso

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobileapp.MainActivity
import com.example.mobileapp.R
import com.google.firebase.auth.FirebaseAuth

// Classe che gestisce la schermata di login dell'app
class LoginActivity : AppCompatActivity() {

    // Variabile per l'autenticazione tramite Firebase
    private lateinit var auth: FirebaseAuth
    // Campo di input per l'email
    private lateinit var editTextEmail: EditText
    // Campo di input per la password
    private lateinit var editTextPassword: EditText
    // Bottone per effettuare il login
    private lateinit var buttonLogin: Button
    // Link testuale per accedere alla schermata di registrazione
    private lateinit var textViewRegister: TextView

    // Metodo chiamato alla creazione dell'activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Imposta il layout della schermata

        // Collegamento delle view agli elementi del layout XML
        auth = FirebaseAuth.getInstance() // Ottiene l'istanza di FirebaseAuth
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewRegister = findViewById(R.id.textRegistrati)

        // Azione sul bottone di login
        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString() // Prende l'email inserita
            val password = editTextPassword.text.toString() // Prende la password inserita

            // Controlla che i campi non siano vuoti
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Prova a effettuare il login con Firebase
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Login effettuato con successo
                            Toast.makeText(this, "Login effettuato!", Toast.LENGTH_SHORT).show()
                            // Passa alla MainActivity dopo il login
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish() // Chiude l'activity di login
                        } else {
                            // Login fallito, mostra un messaggio di errore
                            Toast.makeText(this, "Login fallito: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // Se i campi sono vuoti, mostra un avviso
                Toast.makeText(this, "Compila tutti i campi!", Toast.LENGTH_SHORT).show()
            }
        }

        // Azione sul link di registrazione
        textViewRegister.setOnClickListener {
            // Passa alla schermata di registrazione
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}