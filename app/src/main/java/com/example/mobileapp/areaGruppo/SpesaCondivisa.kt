package com.example.mobileapp.areaGruppo

data class SpesaCondivisa(
    val titolo: String = "",
    val descrizione: String = "",
    val giorno: Int = 0,
    val mese: Int = 0,
    val anno: Int = 0,
    val importo: Float = 0f,
    val idUtentiCoinvolti: List<String> = emptyList(),
    var creatoreID: String = "",
    var pagamentiEffettuati: MutableList<String> = mutableListOf(),
    var pagamentiConfermati: MutableList<String> = mutableListOf(),
    var idDocumento: String? = null // Serve per aggiornare Firestore
)
