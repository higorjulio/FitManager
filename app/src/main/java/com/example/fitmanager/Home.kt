package com.example.fitmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnFrequencia = findViewById<Button>(R.id.btnFrequencia)
        val btnPerfil = findViewById<Button>(R.id.btnPerfil)
        val btnTreino = findViewById<Button>(R.id.btnTreino)

        btnFrequencia.setOnClickListener {
            startActivity(
                Intent(this, Frequencia::class.java)
            )
        }

        btnPerfil.setOnClickListener {
            startActivity(
                Intent(this, Perfil::class.java)
            )
        }

        btnTreino.setOnClickListener {
            startActivity(
                Intent(this, Treino::class.java)
            )
        }
    }
}
