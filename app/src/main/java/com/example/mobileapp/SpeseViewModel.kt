package com.example.mobileapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class SpeseViewModel : ViewModel() {

    private val _spese = MutableLiveData<MutableList<Spesa>>()
    val spese: LiveData<MutableList<Spesa>> = _spese

    init {
        _spese.value = mutableListOf()
    }

    fun caricaTutteLeSpese() {
        val db = FirebaseFirestore.getInstance()

        db.collectionGroup("Spese")
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
                        categoria = document.getString("categoria") ?: ""
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
