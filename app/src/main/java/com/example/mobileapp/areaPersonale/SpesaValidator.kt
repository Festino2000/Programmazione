package com.example.mobileapp.utils

object SpesaValidator {
    fun isSpesaValida(titolo: String, importo: Float): Boolean {
        return titolo.isNotBlank() && importo > 0f
    }
}
