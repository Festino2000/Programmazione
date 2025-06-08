package com.example.mobileapp.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mobileapp.areaPersonale.singoloDataClasses.Spesa
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SpeseViewModel : ViewModel() {

    private val _spese = MutableLiveData<MutableList<Spesa>>()
    val spese: LiveData<MutableList<Spesa>> = _spese

    init {
        _spese.value = mutableListOf()
    }

    fun caricaTutteLeSpese() {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Log.e("SpeseViewModel", "Utente non loggato")
            return
        }

        // Filtra per l'UID dell'utente corrente
        db.collection("Spese")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { result ->
                val listaSpese = mutableListOf<Spesa>()
                for (document in result) {
                    val spesa = Spesa(
                        titolo = document.getString("titolo") ?: "",
                        descrizione = document.getString("descrizione") ?: "",
                        giorno = (document.getLong("giorno") ?: 0).toInt(),
                        mese = (document.getLong("mese") ?: 0).toInt(),
                        anno = (document.getLong("anno") ?: 0).toInt(),
                        importo = (document.getDouble("importo") ?: 0.0).toFloat(),
                        categoria = document.getString("categoria") ?: "",
                        id = document.id
                    )
                    listaSpese.add(spesa)
                }
                _spese.value = listaSpese
            }
            .addOnFailureListener { e ->
                Log.e("SpeseViewModel", "Errore nel caricamento delle spese", e)
            }
    }

    fun aggiungiSpesa(spesa: Spesa) {
        val currentList = _spese.value ?: mutableListOf()
        currentList.add(spesa)
        _spese.value = currentList
    }
}
