
package com.example.fitmanager

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Perfil : AppCompatActivity() {

    private lateinit var ivFotoPerfil: ImageView

    private val selecionarFoto =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->

            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    ivFotoPerfil.setImageURI(uri)

                    val preferencias = getSharedPreferences(
                        "FitManagerFotos",
                        MODE_PRIVATE
                    )

                    preferencias.edit()
                        .putString("foto_$emailAtual", uri.toString())
                        .apply()

                    Toast.makeText(
                        this,
                        "Foto de perfil atualizada!",
                        Toast.LENGTH_SHORT
                    ).show()

                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Não foi possível carregar a foto.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    private lateinit var dbHelper: Database
    private var emailAtual: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        dbHelper = Database(this)

        val etNome = findViewById<EditText>(R.id.etNomePerfil)
        ivFotoPerfil = findViewById(R.id.ivFotoPerfil)
        val btnEscolherFoto = findViewById<Button>(R.id.btnEscolherFoto)
        val etEmail = findViewById<EditText>(R.id.etEmailPerfil)
        val btnSalvar = findViewById<Button>(R.id.btnSalvarPerfil)
        val etSenhaAtual = findViewById<EditText>(R.id.etSenhaAtual)
        val etNovaSenha = findViewById<EditText>(R.id.etNovaSenha)
        val etConfirmarNovaSenha =
            findViewById<EditText>(R.id.etConfirmarNovaSenha)

        val btnAlterarSenha = findViewById<Button>(R.id.btnAlterarSenha)

        emailAtual = intent.getStringExtra("usuario_email") ?: ""

        if (emailAtual.isBlank()) {
            Toast.makeText(this, "Não foi possível identificar o usuário.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val usuario = dbHelper.buscarUsuarioPorEmail(emailAtual)

        if (usuario != null) {
            etNome.setText(usuario.first)
            etEmail.setText(usuario.second)
            emailAtual = usuario.second
            val preferencias = getSharedPreferences(
                "FitManagerFotos",
                MODE_PRIVATE
            )

            val fotoSalva = preferencias.getString(
                "foto_$emailAtual",
                null
            )

            if (fotoSalva != null) {
                ivFotoPerfil.setImageURI(Uri.parse(fotoSalva))
            }
        } else {
            Toast.makeText(this, "Usuário não encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnEscolherFoto.setOnClickListener {
            selecionarFoto.launch(arrayOf("image/*"))
        }
        btnSalvar.setOnClickListener {
            val novoNome = etNome.text.toString().trim()
            val novoEmail = etEmail.text.toString().trim()

            if (novoNome.isEmpty() || novoEmail.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(novoEmail).matches()) {
                Toast.makeText(this, "Digite um e-mail válido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val atualizado = dbHelper.atualizarUsuario(
                emailAtual,
                novoNome,
                novoEmail
            )

            if (atualizado) {
                emailAtual = novoEmail.lowercase().trim()
                val resultado = Intent()
                resultado.putExtra("usuario_email", emailAtual)
                setResult(RESULT_OK, resultado)

                Toast.makeText(
                    this,
                    "Perfil atualizado com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Não foi possível atualizar. Verifique se o e-mail já está cadastrado.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        btnAlterarSenha.setOnClickListener {

            val senhaAtual = etSenhaAtual.text.toString()
            val novaSenha = etNovaSenha.text.toString()
            val confirmarSenha = etConfirmarNovaSenha.text.toString()

            if (senhaAtual.isBlank() ||
                novaSenha.isBlank() ||
                confirmarSenha.isBlank()
            ) {
                Toast.makeText(
                    this,
                    "Preencha todos os campos de senha.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (novaSenha.length < 6) {
                Toast.makeText(
                    this,
                    "A nova senha deve ter pelo menos 6 caracteres.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (novaSenha != confirmarSenha) {
                Toast.makeText(
                    this,
                    "As novas senhas não coincidem.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (senhaAtual == novaSenha) {
                Toast.makeText(
                    this,
                    "A nova senha deve ser diferente da atual.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val alterada = dbHelper.alterarSenha(
                emailAtual,
                senhaAtual,
                novaSenha
            )

            if (alterada) {
                etSenhaAtual.text.clear()
                etNovaSenha.text.clear()
                etConfirmarNovaSenha.text.clear()

                Toast.makeText(
                    this,
                    "Senha alterada com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Senha atual incorreta.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
