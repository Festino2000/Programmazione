package com.example.mobileapp.areaGruppo.gruppoDataClasses

data class SpesaCondivisa(
    val titolo: String = "",
    val descrizione: String = "",
    val giorno: Int = 0,
    val mese: Int = 0,
    val anno: Int = 0,
    val importo: Double = 0.0,
    val idUtentiCoinvolti: List<String> = emptyList(),
    var creatoreID: String = "",
    var pagamentiEffettuati: MutableList<String> = mutableListOf(),
    var pagamentiConfermati: MutableList<String> = mutableListOf(),
    var idDocumento: String? = null, // Serve per aggiornare Firestore
    var notificaMostrata: Boolean = false
)
