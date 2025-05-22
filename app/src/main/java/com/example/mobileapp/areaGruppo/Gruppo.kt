package com.example.mobileapp.areaGruppo

import kotlin.random.Random

data class Gruppo (
    val titolo : String = "",
    val descrizione : String = "",
    val ID : String = String.format("%06d", Random.nextInt(1, 100001))
)