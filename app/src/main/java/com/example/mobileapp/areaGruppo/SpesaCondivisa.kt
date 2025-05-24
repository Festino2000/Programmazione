package com.example.mobileapp.areaGruppo

data class SpesaCondivisa(
    val titolo: String,
    val descrizione: String,
    val giorno: Int,
    val mese: Int,
    val anno: Int,
    val importo: Float,
    val idUtentiCoinvolti: List<String>
)
