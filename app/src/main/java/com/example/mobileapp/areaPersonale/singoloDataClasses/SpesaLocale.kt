package com.example.mobileapp.areaPersonale.singoloDataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.mobileapp.areaPersonale.UriListConverter

@Entity
@TypeConverters(UriListConverter::class)
data class SpesaLocale(
    @PrimaryKey val id: String, // corrisponde all'id Firestore
    val immagini: List<String> // URI locali salvati come stringhe
)
