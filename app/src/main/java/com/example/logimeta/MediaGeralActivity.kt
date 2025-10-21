package com.example.logimeta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.logimeta.database.DatabaseHelper
import com.example.logimeta.databinding.ActivityMediaGeralBinding
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class MediaGeralActivity : AppCompatActivity() {

    private val TAG = "MediaGeralActivity"

    private val binding by lazy {
        ActivityMediaGeralBinding.inflate(layoutInflater)
    }
    private val bancoDeDados by lazy {
        DatabaseHelper(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val moduloSelecionado = intent.getStringExtra(EscolherModuloActivity.MODULO_SELECIONADO_EXTRA)

        if (moduloSelecionado != null) {
            calcularEExibirMedias(moduloSelecionado)
        } else {
            Toast.makeText(this, "Erro: Módulo não especificado.", Toast.LENGTH_LONG).show()
            finish()
        }

        binding.mediaGeralVoltarButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun calcularEExibirMedias(moduloParaFiltrar: String) {

        val sql = """
            SELECT
                sc.modulo_selecionado,
                rc.tempo_coleta,
                rc.qtd_itens_coletados,
                rc.produto_embalado,
                rc.caixa_fechada
            FROM
                RegistroColeta rc
            JOIN
                SessaoColeta sc ON rc.id_sessao = sc.id_sessao
            WHERE
                sc.modulo_selecionado = ?
        """.trimIndent()

        val cursor = bancoDeDados.readableDatabase.rawQuery(sql, arrayOf(moduloParaFiltrar))

        var totalEnderecos = 0
        var totalTempoSegundos: Long = 0
        var totalItens = 0
        var totalComEmbalagem = 0
        var totalSemEmbalagem = 0
        var tempoComEmbalagem: Long = 0
        var tempoSemEmbalagem: Long = 0
        var totalCaixaFechada = 0
        var tempoCaixaFechada: Long = 0
        var nomeModulo = moduloParaFiltrar

        if (cursor.moveToFirst()) {
            totalEnderecos = cursor.count
            nomeModulo = cursor.getString(cursor.getColumnIndexOrThrow("modulo_selecionado"))

            do {
                val itensDaLinha = cursor.getInt(cursor.getColumnIndexOrThrow("qtd_itens_coletados"))
                totalItens += itensDaLinha

                val tempoColetaStr = cursor.getString(cursor.getColumnIndexOrThrow("tempo_coleta"))
                var segundosDaLinha: Long = 0
                if (!tempoColetaStr.isNullOrEmpty() && tempoColetaStr.contains(":")) {
                    try {
                        val partes = tempoColetaStr.split(":")
                        segundosDaLinha = (partes[0].toLong() * 60) + partes[1].toLong()
                        totalTempoSegundos += segundosDaLinha
                    } catch (e: Exception) {
                        Log.e(TAG, "Erro ao converter tempo: '$tempoColetaStr'", e)
                    }
                }

                val caixaFechada = cursor.getInt(cursor.getColumnIndexOrThrow("caixa_fechada")) == 1
                if (caixaFechada) {
                    totalCaixaFechada++
                    tempoCaixaFechada += segundosDaLinha
                    continue
                }

                val produtoEmbalado = cursor.getInt(cursor.getColumnIndexOrThrow("produto_embalado")) == 1
                if (produtoEmbalado) {
                    totalComEmbalagem++
                    tempoComEmbalagem += segundosDaLinha
                } else {
                    totalSemEmbalagem++
                    tempoSemEmbalagem += segundosDaLinha
                }

            } while (cursor.moveToNext())
        } else {
            Log.w(TAG, "Nenhum dado encontrado para o módulo '$moduloParaFiltrar'")
        }
        cursor.close()

        // --- Cálculos Finais das Médias ---
        val mediaGeralSegundos = if (totalEnderecos > 0) totalTempoSegundos.toDouble() / totalEnderecos else 0.0
        val mediaComEmbalagemSegundos = if (totalComEmbalagem > 0) tempoComEmbalagem / totalComEmbalagem else 0L
        val mediaSemEmbalagemSegundos = if (totalSemEmbalagem > 0) tempoSemEmbalagem / totalSemEmbalagem else 0L
        val mediaItensPorEndereco = if (totalEnderecos > 0) totalItens.toDouble() / totalEnderecos else 0.0
        val mediaCaixaFechadaSegundos = if (totalCaixaFechada > 0) tempoCaixaFechada / totalCaixaFechada else 0L

        // --- ATUALIZAÇÃO DA UI (MÉDIAS) ---
        binding.moduloGeralTextView.text = nomeModulo
        binding.tempoGeralTextView.text = formatarSegundos(mediaGeralSegundos.toLong())
        binding.tempoGeralCEmbalagemTextView.text = formatarSegundos(mediaComEmbalagemSegundos)
        binding.tempoGeralSemEmbalagemtextView.text = formatarSegundos(mediaSemEmbalagemSegundos)
        binding.tempoGeralCaixaFechadaTextView.text = formatarSegundos(mediaCaixaFechadaSegundos)
        binding.quantidadeGeralItensPorEnderecoTextView.text = String.format("%.1f", mediaItensPorEndereco)

        // --- LÓGICA DE CÁLCULO DAS PREVISÕES ---
        var previsaoEm1h = 0
        var previsaoEm7h20m = 0

        if (mediaGeralSegundos > 0) {
            // 1 hora = 3600 segundos
            val segundosEm1h = 3600
            previsaoEm1h = (segundosEm1h / mediaGeralSegundos).toInt()

            // 7 horas e 20 minutos = (7 * 3600) + (20 * 60) = 26400 segundos
            val segundosEm7h20m = 26400
            previsaoEm7h20m = (segundosEm7h20m / mediaGeralSegundos).toInt()
        }

        // --- ATUALIZAÇÃO DA UI (PREVISÕES) ---
        binding.previsao1hTextView.text = previsaoEm1h.toString()
        binding.previsao7h20TextView.text = previsaoEm7h20m.toString()

        Log.d(TAG, "Cálculos concluídos e UI atualizada para o módulo: $nomeModulo")
    }

    private fun formatarSegundos(segundosTotais: Long): String {
        if (segundosTotais <= 0) return "00:00:00"
        val horas = TimeUnit.SECONDS.toHours(segundosTotais)
        val minutos = TimeUnit.SECONDS.toMinutes(segundosTotais) % 60
        val segundos = segundosTotais % 60
        return String.format("%02d:%02d:%02d", horas, minutos, segundos)
    }
}
