package com.example.mobileapp.areaGruppo

import com.example.mobileapp.areaGruppo.Utente

data class SpesaCondivisa(
    val titolo: String,
    val descrizione: String,
    val giorno: Int,
    val mese: Int,
    val anno: Int,
    val importo: Float,
    //val utenteCoinvolto: String
)
