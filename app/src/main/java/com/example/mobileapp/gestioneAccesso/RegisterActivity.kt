package com.example.mobileapp.gestioneAccesso

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonRegister: Button
    private lateinit var buttonGoogleSignIn: Button
    private lateinit var oneTapClient: SignInClient
    private lateinit var editTextNickname: EditText

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            if (idToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            println("Autenticazione con Google fallita: \${task.exception?.message}")
                        }
                    }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
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
                                val email = currentUser?.email

                                if (uid != null && email != null) {
                                    val utente = hashMapOf(
                                        "utenteID" to uid,
                                        "nickname" to email // oppure puoi chiedere un nickname personalizzato
                                    )

                                    val db = FirebaseFirestore.getInstance()
                                    db.collection("Utenti").document(uid)
                                        .set(utente)
                                        .addOnSuccessListener {
                                            println("Utente salvato nel DB")
                                            val intent = Intent(this, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        .addOnFailureListener {
                                            println("Errore salvataggio utente: ${it.message}")
                                        }
                                }
                            }
                            else {
                                println("Registrazione fallita: \${task.exception?.message}")
                            }
                        }
                } else {
                    println("Le password non coincidono!")
                }
            } else {
                println("Compila tutti i campi!")
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
                        println("Errore durante il lancio dell'intent: \${e.message}")
                    }
                }
                .addOnFailureListener {
                    println("Errore durante l'accesso con Google: \${it.message}")
                }
        }
    }
}