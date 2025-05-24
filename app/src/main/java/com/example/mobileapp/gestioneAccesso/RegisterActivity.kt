package com.example.mobileapp.gestioneAccesso

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mobileapp.MainActivity
import com.example.mobileapp.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonRegister: Button
    private lateinit var buttonGoogleSignIn: Button
    private lateinit var oneTapClient: SignInClient
    private lateinit var editTextNickname: EditText

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            if (idToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val currentUser = auth.currentUser
                            val uid = currentUser?.uid
                            val email = currentUser?.email

                            if (uid != null && email != null) {
                                val db = FirebaseFirestore.getInstance()
                                db.collection("Utenti").document(uid)
                                    .get()
                                    .addOnSuccessListener { document ->
                                        if (document.exists()) {
                                            // Utente già registrato → vai alla MainActivity
                                            startActivity(Intent(this, MainActivity::class.java))
                                            finish()
                                        } else {
                                            // Utente nuovo → chiedi nickname
                                            mostraDialogNickname(uid, email)
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Errore accesso Firestore: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Autenticazione con Google fallita", Toast.LENGTH_SHORT).show()
                            Log.e("RegisterActivity", "Google sign-in failed", task.exception)
                        }
                    }
            }
        }
    }

    private fun mostraDialogNickname(uid: String, email: String) {
        val editText = EditText(this)
        editText.hint = "Inserisci un nickname"

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Benvenuto!")
            .setMessage("Inserisci il tuo nickname per completare la registrazione")
            .setView(editText)
            .setPositiveButton("Conferma") { _, _ ->
                val nickname = editText.text.toString().trim()
                if (nickname.isNotEmpty()) {
                    val utente = hashMapOf(
                        "utenteID" to uid,
                        "nickname" to nickname,
                        "email" to email
                    )
                    FirebaseFirestore.getInstance()
                        .collection("Utenti")
                        .document(uid)
                        .set(utente)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registrazione completata", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Errore salvataggio: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Nickname obbligatorio", Toast.LENGTH_SHORT).show()
                    mostraDialogNickname(uid, email) // ripropone il dialog
                }
            }
            .setCancelable(false)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        oneTapClient = Identity.getSignInClient(this)

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        buttonRegister = findViewById(R.id.buttonRegister)
        buttonGoogleSignIn = findViewById(R.id.buttonGoogleSignIn)
        editTextNickname = findViewById(R.id.editTextNickname)

        buttonRegister.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val confirmPassword = editTextConfirmPassword.text.toString()
            val nickname = editTextNickname.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && nickname.isNotEmpty()) {
                if (password == confirmPassword) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val currentUser = auth.currentUser
                                val uid = currentUser?.uid
                                val emailUser = currentUser?.email

                                if (uid != null && emailUser != null) {
                                    val utente = hashMapOf(
                                        "utenteID" to uid,
                                        "nickname" to nickname,
                                        "email" to emailUser
                                    )

                                    db.collection("Utenti").document(uid)
                                        .set(utente)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Registrazione completata", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, MainActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Errore salvataggio: ${it.message}", Toast.LENGTH_SHORT).show()
                                            Log.e("RegisterActivity", "Firestore save failed", it)
                                        }
                                }
                            } else {
                                Toast.makeText(this, "Registrazione fallita: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                Log.e("RegisterActivity", "createUserWithEmail failed", task.exception)
                            }
                        }
                } else {
                    Toast.makeText(this, "Le password non coincidono!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Compila tutti i campi!", Toast.LENGTH_SHORT).show()
            }
        }

        buttonGoogleSignIn.setOnClickListener {
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .build()

            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener { result ->
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                        googleSignInLauncher.launch(intentSenderRequest)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Errore lancio intent: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("RegisterActivity", "Intent launch failed", e)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Errore accesso Google: ${it.message}", Toast.LENGTH_SHORT).show()
                    Log.e("RegisterActivity", "Google access failed", it)
                }
        }
    }
}