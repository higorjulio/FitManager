
package com.example.fitmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class Home : AppCompatActivity() {

    private var emailUsuario: String = ""

    private val perfilLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->

            if (resultado.resultCode == RESULT_OK) {
                val novoEmail = resultado.data?.getStringExtra("usuario_email")

                if (!novoEmail.isNullOrBlank()) {
                    emailUsuario = novoEmail
                }
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        emailUsuario = intent.getStringExtra("usuario_email") ?: ""

        val btnFrequencia = findViewById<Button>(R.id.btnFrequencia)
        val btnPerfil = findViewById<Button>(R.id.btnPerfil)
        val btnTreino = findViewById<Button>(R.id.btnTreino)
        val btnMapa = findViewById<Button>(R.id.btnMapa)

        btnFrequencia.setOnClickListener {
            startActivity(Intent(this, Frequencia::class.java))
        }

        btnPerfil.setOnClickListener {
            val intentPerfil = Intent(this, Perfil::class.java)
            intentPerfil.putExtra("usuario_email", emailUsuario)
            perfilLauncher.launch(intentPerfil)
        }

        btnTreino.setOnClickListener {
            startActivity(Intent(this, Treino::class.java))
        }

        btnMapa.setOnClickListener {
            startActivity(Intent(this, MapasAcademias::class.java))
        }
    }
}
