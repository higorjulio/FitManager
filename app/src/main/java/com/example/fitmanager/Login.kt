package com.example.fitmanager

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Login : AppCompatActivity() {

    private lateinit var dbHelper: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = Database(this)

        val email = findViewById<EditText>(R.id.etEmail)
        val senha = findViewById<EditText>(R.id.etSenha)
        val entrar = findViewById<Button>(R.id.btnEntrar)
        val cadastro = findViewById<TextView>(R.id.tvCadastro)

        entrar.setOnClickListener {
            val usuario = email.text.toString().trim()
            val password = senha.text.toString()

            if (usuario.isEmpty()) {
                email.error = "Informe seu e-mail"
                email.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(usuario).matches()) {
                email.error = "E-mail inválido"
                email.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                senha.error = "Informe sua senha"
                senha.requestFocus()
                return@setOnClickListener
            }

            //----------
            val loginValido = dbHelper.validarLogin(usuario, password)

            if (loginValido) {
                Toast.makeText(
                    this,
                    "Bem-vindo ao FitManager!",
                    Toast.LENGTH_SHORT
                ).show()

                val intentHome = Intent(this, Home::class.java)

                intentHome.putExtra("usuario_email", usuario)

                startActivity(intentHome)
                finish()
            }

            else {
                Toast.makeText(
                    this,
                    "E-mail ou senha incorretos!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        cadastro.setOnClickListener {
            val intent = Intent(this, Cadastro::class.java)
            startActivity(intent)
        }
    }
}
