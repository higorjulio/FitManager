
package com.example.fitmanager

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.SharedPreferences
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Treino : AppCompatActivity() {


    private fun carregarHistorico() {

        val tvHistorico = findViewById<TextView>(R.id.tvHistorico)

        val historico = preferencias.getString("historico", "")

        if (historico.isNullOrEmpty()) {
            tvHistorico.text = "Nenhum treino finalizado ainda."
        } else {
            tvHistorico.text = historico
        }
    }

    private fun finalizarTreino() {

        val idsCards = listOf(
            R.id.exSupinoReto,
            R.id.exSupinoInclinado,
            R.id.exPuxada,
            R.id.exRemada,
            R.id.exAgachamento,
            R.id.exLegPress,
            R.id.exRosca,
            R.id.exTriceps
        )

        var concluidos = 0

        for (id in idsCards) {
            val card = findViewById<View>(id)
            val checkBox = card.findViewById<CheckBox>(R.id.cbConcluido)

            if (checkBox.isChecked) {
                concluidos++
            }
        }

        if (concluidos != idsCards.size) {
            Toast.makeText(
                this,
                "Conclua os 8 exercícios antes de finalizar!",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val data = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        ).format(Date())

        val registro = "Treino concluído em $data - 8 de 8 exercícios"

        val historicoAtual = preferencias.getString("historico", "") ?: ""

        val novoHistorico = if (historicoAtual.isEmpty()) {
            registro
        } else {
            "$registro\n$historicoAtual"
        }

        preferencias.edit()
            .putString("historico", novoHistorico)
            .apply()

        Toast.makeText(
            this,
            "Treino finalizado com sucesso!",
            Toast.LENGTH_SHORT
        ).show()

        // Reinicia as marcações para uma nova rotina
        for (id in idsCards) {
            preferencias.edit()
                .putBoolean("exercicio_$id", false)
                .apply()

            val card = findViewById<View>(id)
            val checkBox = card.findViewById<CheckBox>(R.id.cbConcluido)
            checkBox.isChecked = false
        }

        atualizarProgresso()
        carregarHistorico()
    }

    private lateinit var preferencias: SharedPreferences
    private lateinit var tvProgresso: TextView
    private lateinit var progressTreino: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treino)
        preferencias = getSharedPreferences(
            "FitManagerTreinos",
            MODE_PRIVATE
        )
        tvProgresso = findViewById(R.id.tvProgresso)
        progressTreino = findViewById(R.id.progressTreino)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        configurarExercicio(
            R.id.exSupinoReto,
            "Supino reto",
            3,
            12,
            60
        )

        configurarExercicio(
            R.id.exSupinoInclinado,
            "Supino inclinado",
            3,
            10,
            60
        )

        configurarExercicio(
            R.id.exPuxada,
            "Puxada frontal",
            3,
            12,
            60
        )

        configurarExercicio(
            R.id.exRemada,
            "Remada baixa",
            3,
            10,
            60
        )

        configurarExercicio(
            R.id.exAgachamento,
            "Agachamento",
            4,
            12,
            90
        )

        configurarExercicio(
            R.id.exLegPress,
            "Leg press",
            3,
            12,
            60
        )

        configurarExercicio(
            R.id.exRosca,
            "Rosca direta",
            3,
            12,
            60
        )

        configurarExercicio(
            R.id.exTriceps,
            "Tríceps pulley",
            3,
            12,
            60
        )
        atualizarProgresso()
        val btnFinalizarTreino = findViewById<Button>(
            R.id.btnFinalizarTreino
        )

        btnFinalizarTreino.setOnClickListener {
            finalizarTreino()
        }

        findViewById<Button>(R.id.btnVoltar).setOnClickListener {
            finish()
        }

        carregarHistorico()
    }

    private fun configurarExercicio(
        idCard: Int,
        nome: String,
        series: Int,
        repeticoes: Int,
        descanso: Int
    ) {
        val card = findViewById<View>(idCard)

        card.findViewById<TextView>(R.id.tvNomeExercicio).text = nome

        card.findViewById<TextView>(R.id.tvSeries).text =
            "Séries: $series"

        card.findViewById<TextView>(R.id.tvRepeticoes).text =
            "Repetições: $repeticoes"

        card.findViewById<TextView>(R.id.tvDescanso).text =
            "Descanso: $descanso segundos"

        val checkBox = card.findViewById<CheckBox>(R.id.cbConcluido)
        // Recupera a marcação salva anteriormente
        checkBox.isChecked = preferencias.getBoolean(
            "exercicio_$idCard",
            false
        )

// Ajusta o texto conforme a marcação recuperada
        checkBox.text = if (checkBox.isChecked) {
            "Exercício concluído!"
        } else {
            "Concluído"
        }

        checkBox.setOnCheckedChangeListener { _, marcado ->

            // Atualiza o texto
            checkBox.text = if (marcado) {
                "Exercício concluído!"
            } else {
                "Concluído"
            }

            // Salva a marcação no aparelho
            preferencias.edit()
                .putBoolean("exercicio_$idCard", marcado)
                .apply()

            // Atualiza a barra de progresso
            atualizarProgresso()
        }
    }
    private fun atualizarProgresso() {

        val idsCards = listOf(
            R.id.exSupinoReto,
            R.id.exSupinoInclinado,
            R.id.exPuxada,
            R.id.exRemada,
            R.id.exAgachamento,
            R.id.exLegPress,
            R.id.exRosca,
            R.id.exTriceps
        )

        var concluidos = 0

        for (id in idsCards) {
            val card = findViewById<View>(id)
            val checkBox = card.findViewById<CheckBox>(R.id.cbConcluido)

            if (checkBox.isChecked) {
                concluidos++
            }
        }

        tvProgresso.text = "Progresso: $concluidos de 8 exercícios"

        progressTreino.progress = concluidos
    }
}
