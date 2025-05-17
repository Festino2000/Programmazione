package com.example.mobileapp

data class Spesa(
    val titolo: String,
    val descrizione: String,
    val giorno: Int,
    val mese: Int,
    val anno: Int,
    val importo: Float,
    val categoria: String
) {
    // Formatta la data come stringa leggibile
    fun getData(): String {
        return String.format("%02d/%02d/%04d", giorno, mese, anno)
    }

    // Rappresentazione come stringa
    override fun toString(): String {
        return "[$categoria] $titolo - ${getData()} - €$importo"
    }
}
