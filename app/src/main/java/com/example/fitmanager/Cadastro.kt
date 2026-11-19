package com.example.fitmanager

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Cadastro : AppCompatActivity() {

    private lateinit var dbHelper: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        dbHelper = Database(this)

        val etNome = findViewById<EditText>(R.id.etNome)
        val etEmail = findViewById<EditText>(R.id.etEmailCadastro)
        val etSenha = findViewById<EditText>(R.id.etSenhaCadastro)
        val etConfirmarSenha = findViewById<EditText>(R.id.etConfirmarSenha)
        val btnCadastrar = findViewById<Button>(R.id.btnCadastrar)
        val tvVoltarLogin = findViewById<TextView>(R.id.tvVoltarLogin)

        btnCadastrar.setOnClickListener {
            val nome = etNome.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val senha = etSenha.text.toString()
            val confirmarSenha = etConfirmarSenha.text.toString()

            //provavelmente tem uma forma melhor pra isso
            if (nome.isEmpty()) {
                etNome.error = "Informe seu nome"
                etNome.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                etEmail.error = "Informe seu e-mail"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "E-mail inválido"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (senha.isEmpty()) {
                etSenha.error = "Informe sua senha"
                etSenha.requestFocus()
                return@setOnClickListener
            }

            if (senha.length < 6) {
                etSenha.error = "A senha deve ter pelo menos 6 caracteres"
                etSenha.requestFocus()
                return@setOnClickListener
            }

            if (senha != confirmarSenha) {
                etConfirmarSenha.error = "As senhas não coincidem"
                etConfirmarSenha.requestFocus()
                return@setOnClickListener
            }

            if (dbHelper.emailExiste(email)) {
                etEmail.error = "Este e-mail já está cadastrado"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            val sucesso = dbHelper.cadastrarUsuario(nome, email, senha)

            if (sucesso) {
                Toast.makeText(
                    this,
                    "Conta criada com sucesso! Faça login para continuar.",
                    Toast.LENGTH_LONG,
                ).show()
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Erro ao cadastrar usuário. Tente novamente.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        tvVoltarLogin.setOnClickListener {
            finish()
        }
    }
}
