package com.example.mobileapp.utils

import org.junit.Assert.*
import org.junit.Test

class SpesaValidatorTest {

    @Test
    fun `spesa valida con titolo e importo maggiore di zero`() {
        val result = SpesaValidator.isSpesaValida("Pizza", 10.0f)
        assertTrue(result)
    }

    @Test
    fun `spesa non valida con titolo vuoto`() {
        val result = SpesaValidator.isSpesaValida("", 10.0f)
        assertFalse(result)
    }

    @Test
    fun `spesa non valida con importo zero`() {
        val result = SpesaValidator.isSpesaValida("Panino", 0.0f)
        assertFalse(result)
    }

    @Test
    fun `spesa non valida con titolo vuoto e importo zero`() {
        val result = SpesaValidator.isSpesaValida("", 0.0f)
        assertFalse(result)
    }
}
