package com.example.mobileapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mobileapp.areaGruppo.gruppoDataClasses.Gruppo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GruppoViewModel : ViewModel() {

    private val _gruppiUtente = MutableLiveData<List<Gruppo>>()
    val gruppiUtente: LiveData<List<Gruppo>> get() = _gruppiUtente

    private val db = FirebaseFirestore.getInstance()
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    fun caricaGruppiUtente() {
        if (currentUserUid == null) {
            _gruppiUtente.value = emptyList()
            return
        }

        db.collection("Gruppi")
            .whereArrayContains("utentiID", currentUserUid)
            .get()
            .addOnSuccessListener { documents ->
                val lista = documents.mapNotNull { it.toObject(Gruppo::class.java) }
                _gruppiUtente.value = lista
            }
            .addOnFailureListener {
                _gruppiUtente.value = emptyList()
            }
    }
}
