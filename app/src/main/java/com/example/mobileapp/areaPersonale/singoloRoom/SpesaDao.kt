package com.example.mobileapp.areaPersonale.singoloRoom

import androidx.room.*
import com.example.mobileapp.areaPersonale.singoloDataClasses.SpesaLocale
@Dao
interface SpesaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserisci(spesa: SpesaLocale)

    @Query("SELECT * FROM SpesaLocale")
    fun getTutte(): List<SpesaLocale>

    @Query("SELECT * FROM SpesaLocale WHERE id = :id LIMIT 1")
    fun getById(id: String): SpesaLocale?

    @Delete
    fun elimina(spesa: SpesaLocale)
}
