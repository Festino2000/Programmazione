package com.example.mobileapp.areaPersonale

data class Spesa(
    val titolo: String = "",
    val descrizione: String = "",
    val giorno: Int = 0,
    val mese: Int = 0,
    val anno: Int = 0,
    val importo: Float = 0.0f,
    val categoria: String = "",
    val id: String = "" // ID del documento Firestore (non visibile all'utente)
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
