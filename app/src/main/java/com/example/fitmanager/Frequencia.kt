package com.example.fitmanager

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Frequencia : AppCompatActivity() {

    private lateinit var preferencias: SharedPreferences

    private lateinit var tvTotalMes: TextView
    private lateinit var tvSequencia: TextView
    private lateinit var tvProgressoFrequencia: TextView
    private lateinit var progressFrequencia: ProgressBar
    private lateinit var btnEditarMeta: ImageButton
    private lateinit var tvStatusHoje: TextView
    private lateinit var btnRegistrarPresenca: Button
    private lateinit var tvHistoricoFrequencia: TextView
    private lateinit var diasSemanaViews: List<TextView>

    private val formatoChave = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val formatoMes = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val formatoExibicao = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_frequencia)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        preferencias = getSharedPreferences("FitManagerFrequencia", MODE_PRIVATE)

        tvTotalMes = findViewById(R.id.tvTotalMes)
        tvSequencia = findViewById(R.id.tvSequencia)
        tvProgressoFrequencia = findViewById(R.id.tvProgressoFrequencia)
        progressFrequencia = findViewById(R.id.progressFrequencia)
        btnEditarMeta = findViewById(R.id.btnEditarMeta)
        tvStatusHoje = findViewById(R.id.tvStatusHoje)
        btnRegistrarPresenca = findViewById(R.id.btnRegistrarPresenca)
        tvHistoricoFrequencia = findViewById(R.id.tvHistoricoFrequencia)

        diasSemanaViews = listOf(
            findViewById(R.id.tvDiaSeg),
            findViewById(R.id.tvDiaTer),
            findViewById(R.id.tvDiaQua),
            findViewById(R.id.tvDiaQui),
            findViewById(R.id.tvDiaSex),
            findViewById(R.id.tvDiaSab),
            findViewById(R.id.tvDiaDom)
        )

        btnEditarMeta.setOnClickListener {
            abrirDialogMetaSemanal()
        }

        btnRegistrarPresenca.setOnClickListener {
            registrarPresencaHoje()
        }

        atualizarTela()
    }

    private fun abrirDialogMetaSemanal() {
        val metaAtual = preferencias.getInt(KEY_META_SEMANAL, 5).coerceIn(1, 7)

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(InputFilter.LengthFilter(1))
            hint = "Ex: 5"
            setText(metaAtual.toString())
            setSelection(text.length)
        }

        val container = FrameLayout(this).apply {
            val margem = (20 * resources.displayMetrics.density).toInt()
            setPadding(margem, 8, margem, 0)
            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle("Definir meta semanal")
            .setMessage("Quantos dias por semana você quer treinar? (1 a 7)")
            .setView(container)
            .setPositiveButton("Salvar") { _, _ ->
                val novaMeta = input.text.toString().trim().toIntOrNull()
                if (novaMeta == null || novaMeta < 1 || novaMeta > 7) {
                    Toast.makeText(
                        this,
                        "Informe uma meta válida entre 1 e 7 dias.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                preferencias.edit()
                    .putInt(KEY_META_SEMANAL, novaMeta)
                    .apply()

                Toast.makeText(
                    this,
                    "Meta semanal atualizada para ${formatarDias(novaMeta)}!",
                    Toast.LENGTH_SHORT
                ).show()

                atualizarTela()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun registrarPresencaHoje() {
        val agora = Date()
        val chaveHoje = formatoChave.format(agora)
        val datas = obterDatasPresenca()

        if (datas.contains(chaveHoje)) {
            Toast.makeText(
                this,
                "Você já registrou presença hoje!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        datas.add(chaveHoje)

        val horaFormatada = SimpleDateFormat("HH:mm", Locale.getDefault()).format(agora)
        val registro = "Presença registrada em ${formatoExibicao.format(agora)}"
        val historicoAtual = preferencias.getString(KEY_HISTORICO, "") ?: ""
        val novoHistorico = if (historicoAtual.isEmpty()) {
            registro
        } else {
            "$registro\n$historicoAtual"
        }

        preferencias.edit()
            .putStringSet(KEY_DATAS_PRESENCA, datas)
            .putString(KEY_HISTORICO, novoHistorico)
            .putString(KEY_HORA_HOJE_DATA, chaveHoje)
            .putString(KEY_HORA_HOJE_VALOR, horaFormatada)
            .apply()

        Toast.makeText(
            this,
            "Presença registrada com sucesso!",
            Toast.LENGTH_SHORT
        ).show()

        atualizarTela()
    }

    private fun atualizarTela() {
        val datas = obterDatasPresenca()
        val metaSemanal = preferencias.getInt(KEY_META_SEMANAL, 5).coerceIn(1, 7)
        val hoje = Date()
        val chaveHoje = formatoChave.format(hoje)
        val mesAtual = formatoMes.format(hoje)

        val totalMes = datas.count { it.startsWith(mesAtual) }
        tvTotalMes.text = formatarDias(totalMes)

        val sequencia = calcularSequencia(datas)
        tvSequencia.text = formatarDias(sequencia)

        val diasNaSemana = atualizarDiasDaSemana(datas)

        progressFrequencia.max = metaSemanal
        progressFrequencia.progress = diasNaSemana.coerceAtMost(metaSemanal)
        tvProgressoFrequencia.text = if (diasNaSemana >= metaSemanal) {
            "Meta semanal atingida: $diasNaSemana de $metaSemanal dias!"
        } else {
            "Meta semanal: $diasNaSemana de $metaSemanal dias"
        }

        val jaRegistrouHoje = datas.contains(chaveHoje)
        if (jaRegistrouHoje) {
            val dataSalva = preferencias.getString(KEY_HORA_HOJE_DATA, "")
            val horaSalva = if (dataSalva == chaveHoje) {
                preferencias.getString(KEY_HORA_HOJE_VALOR, null)
            } else {
                null
            }
            tvStatusHoje.text = if (horaSalva != null) {
                "Presença registrada hoje às $horaSalva."
            } else {
                "Presença de hoje já registrada!"
            }
            btnRegistrarPresenca.isEnabled = false
            btnRegistrarPresenca.alpha = 0.6f
            btnRegistrarPresenca.text = "Presença registrada hoje"
        } else {
            tvStatusHoje.text = "Você ainda não registrou presença hoje."
            btnRegistrarPresenca.isEnabled = true
            btnRegistrarPresenca.alpha = 1.0f
            btnRegistrarPresenca.text = "Registrar presença hoje"
        }

        val historico = preferencias.getString(KEY_HISTORICO, "")
        tvHistoricoFrequencia.text = if (historico.isNullOrEmpty()) {
            "Nenhuma presença registrada ainda."
        } else {
            historico
        }
    }

    private fun atualizarDiasDaSemana(datas: Set<String>): Int {
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val diffParaSegunda = if (dayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - dayOfWeek
        cal.add(Calendar.DAY_OF_YEAR, diffParaSegunda)

        var diasTreinadosSemana = 0

        for (view in diasSemanaViews) {
            val chaveDia = formatoChave.format(cal.time)
            val presente = datas.contains(chaveDia)

            if (presente) {
                diasTreinadosSemana++
                view.setBackgroundColor(Color.parseColor("#174B91"))
                view.setTextColor(Color.WHITE)
            } else {
                view.setBackgroundColor(Color.parseColor("#E5EEF9"))
                view.setTextColor(Color.parseColor("#526579"))
            }

            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return diasTreinadosSemana
    }

    private fun calcularSequencia(datas: Set<String>): Int {
        if (datas.isEmpty()) return 0

        val cal = Calendar.getInstance()
        if (!datas.contains(formatoChave.format(cal.time))) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        var sequencia = 0
        while (datas.contains(formatoChave.format(cal.time))) {
            sequencia++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return sequencia
    }

    private fun obterDatasPresenca(): MutableSet<String> {
        val salvas = preferencias.getStringSet(KEY_DATAS_PRESENCA, emptySet())
        return HashSet(salvas ?: emptySet())
    }

    private fun formatarDias(quantidade: Int): String {
        return if (quantidade == 1) "1 dia" else "$quantidade dias"
    }

    companion object {
        private const val KEY_META_SEMANAL = "meta_semanal"
        private const val KEY_DATAS_PRESENCA = "datas_presenca"
        private const val KEY_HISTORICO = "historico_frequencia"
        private const val KEY_HORA_HOJE_DATA = "hora_hoje_data"
        private const val KEY_HORA_HOJE_VALOR = "hora_hoje_valor"
    }
}
