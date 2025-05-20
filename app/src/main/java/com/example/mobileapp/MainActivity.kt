package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.mobileapp.areaGruppo.GruppoActivity
import com.example.mobileapp.areaPersonale.SoloActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val soloButton = findViewById<ImageButton>(R.id.solo)
        val gruppoButton = findViewById<ImageButton>(R.id.gruppo)

        soloButton.setOnClickListener {
            val intent = Intent(this, SoloActivity::class.java)
            startActivity(intent)
        }

         gruppoButton.setOnClickListener {
            // Toast.makeText(this, "Pulsante Gruppo premuto!", Toast.LENGTH_SHORT).show()
             val intent = Intent(this, GruppoActivity::class.java)
             startActivity(intent)
        }
    }
}

