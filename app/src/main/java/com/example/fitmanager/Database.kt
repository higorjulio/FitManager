package com.example.fitmanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

class Database(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FitManager.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_USUARIOS = "usuarios"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NOME = "nome"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_SENHA = "senha"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = ("CREATE TABLE " + TABLE_USUARIOS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NOME + " TEXT NOT NULL, "
                + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, "
                + COLUMN_SENHA + " TEXT NOT NULL)")
        db.execSQL(createTableQuery)

        //adm para testes
        val values = ContentValues().apply {
            put(COLUMN_NOME, "Administrador")
            put(COLUMN_EMAIL, "admin@email.com")
            put(COLUMN_SENHA, hashPassword("123456"))
        }
        db.insert(TABLE_USUARIOS, null, values)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // As atualizações do banco serão feitas sem apagar os dados existentes.
    }


    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun cadastrarUsuario(nome: String, email: String, senha: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOME, nome)
            put(COLUMN_EMAIL, email.lowercase().trim())
            put(COLUMN_SENHA, hashPassword(senha))
        }

        val result = db.insert(TABLE_USUARIOS, null, values)
        db.close()
        return result != -1L
    }
    fun validarLogin(email: String, senha: String): Boolean {
        val db = readableDatabase
        val senhaHash = hashPassword(senha)
        val query = "SELECT * FROM $TABLE_USUARIOS WHERE LOWER($COLUMN_EMAIL) = ? AND $COLUMN_SENHA = ?"
        val cursor = db.rawQuery(query, arrayOf(email.lowercase().trim(), senhaHash))
        val existe = cursor.count > 0
        cursor.close()
        db.close()
        return existe
    }

    fun emailExiste(email: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USUARIOS WHERE LOWER($COLUMN_EMAIL) = ?"
        val cursor = db.rawQuery(query, arrayOf(email.lowercase().trim()))
        val existe = cursor.count > 0
        cursor.close()
        db.close()
        return existe
    }
    fun buscarUsuarioPorEmail(email: String): Pair<String, String>? {
        val db = readableDatabase

        val query = """
        SELECT $COLUMN_NOME, $COLUMN_EMAIL
        FROM $TABLE_USUARIOS
        WHERE LOWER($COLUMN_EMAIL) = ?
    """

        val cursor = db.rawQuery(
            query,
            arrayOf(email.lowercase().trim())
        )

        var usuario: Pair<String, String>? = null

        if (cursor.moveToFirst()) {
            val nome = cursor.getString(0)
            val emailEncontrado = cursor.getString(1)

            usuario = Pair(nome, emailEncontrado)
        }

        cursor.close()
        db.close()

        return usuario
    }
    fun atualizarUsuario(
        emailAtual: String,
        novoNome: String,
        novoEmail: String
    ): Boolean {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_NOME, novoNome.trim())
            put(COLUMN_EMAIL, novoEmail.lowercase().trim())
        }

        val resultado = try {
            db.update(
                TABLE_USUARIOS,
                values,
                "$COLUMN_EMAIL = ?",
                arrayOf(emailAtual.lowercase().trim())
            )
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            0
        }

        db.close()

        return resultado > 0
    }

    fun alterarSenha(
        email: String,
        senhaAtual: String,
        novaSenha: String
    ): Boolean {
        val db = writableDatabase

        val senhaAtualHash = hashPassword(senhaAtual)

        val novaSenhaHash = hashPassword(novaSenha)

        val values = ContentValues().apply {
            put(COLUMN_SENHA, novaSenhaHash)
        }

        val resultado = db.update(
            TABLE_USUARIOS,
            values,
            "$COLUMN_EMAIL = ? AND $COLUMN_SENHA = ?",
            arrayOf(
                email.lowercase().trim(),
                senhaAtualHash
            )
        )

        db.close()

        return resultado > 0
    }

}
