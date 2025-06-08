package com.example.mobileapp.areaGruppo.gruppoDataClasses

data class Gruppo (
    val titolo : String = "",
    val descrizione : String = "",
    var idUnico: String = "",
    var utentiID : List<String> = emptyList(),
    var creatoreID: String = ""
)