package com.example.logimeta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.logimeta.database.DatabaseHelper
import com.example.logimeta.databinding.ActivityMediaGeralBinding
import com.example.logimeta.model.ResultadosCalculados
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToLong

class MediaGeralActivity : AppCompatActivity() {

    private val TAG = "MediaGeralActivity"

    private val binding by lazy {
        ActivityMediaGeralBinding.inflate(layoutInflater)
    }

    private val bancoDeDados by lazy {
        DatabaseHelper(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val moduloSelecionado = intent.getStringExtra(EscolherModuloActivity.MODULO_SELECIONADO_EXTRA)

        if (moduloSelecionado != null) {
            carregarEExibirMediasAsync(moduloSelecionado)
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

    private fun carregarEExibirMediasAsync(moduloParaFiltrar: String) {
        lifecycleScope.launch {
            val resultados: ResultadosCalculados? = withContext(Dispatchers.IO) {
                calcularMedias(moduloParaFiltrar)
            }
            if (resultados != null) {
                atualizarUI(resultados)
            } else {
                Toast.makeText(this@MediaGeralActivity, "Nenhum dado encontrado para este módulo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calcularMedias(moduloParaFiltrar: String): ResultadosCalculados? {
        val sql = """
            SELECT sc.modulo_selecionado, rc.tempo_coleta, rc.qtd_itens_coletados, rc.produto_embalado, rc.caixa_fechada
            FROM RegistroColeta rc
            JOIN SessaoColeta sc ON rc.id_sessao = sc.id_sessao
            WHERE sc.modulo_selecionado = ? COLLATE NOCASE
        """.trimIndent()

        bancoDeDados.readableDatabase.rawQuery(sql, arrayOf(moduloParaFiltrar)).use { cursor ->

            if (cursor == null || !cursor.moveToFirst()) {
                Log.w(TAG, "Nenhum dado encontrado para o módulo '$moduloParaFiltrar'")
                return null
            }

            var totalEnderecosPicking = 0
            var totalTempoPicking: Long = 0
            var totalItensPicking = 0
            var totalComEmbalagem = 0
            var tempoComEmbalagem: Long = 0
            var totalSemEmbalagem = 0
            var tempoSemEmbalagem: Long = 0

            var totalCaixaFechada = 0
            var tempoCaixaFechada: Long = 0

            val nomeModulo = cursor.getString(cursor.getColumnIndexOrThrow("modulo_selecionado"))

            do {
                var segundosDaLinha: Long = 0
                val tempoColetaStr = cursor.getString(cursor.getColumnIndexOrThrow("tempo_coleta"))

                if (!tempoColetaStr.isNullOrEmpty()) {
                    try {
                        val partes = tempoColetaStr.split(":")
                        segundosDaLinha = when (partes.size) {
                            3 -> partes[0].toLong() * 3600 + partes[1].toLong() * 60 + partes[2].toLong()
                            2 -> partes[0].toLong() * 60 + partes[1].toLong()
                            else -> 0
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Erro ao converter tempo: '$tempoColetaStr'", e)
                        continue
                    }
                } else continue

                val caixaFechada = cursor.getInt(cursor.getColumnIndexOrThrow("caixa_fechada")) == 1

                if (caixaFechada) {
                    totalCaixaFechada++
                    tempoCaixaFechada += segundosDaLinha
                } else {
                    totalEnderecosPicking++
                    totalTempoPicking += segundosDaLinha
                    totalItensPicking += cursor.getInt(cursor.getColumnIndexOrThrow("qtd_itens_coletados"))

                    val produtoEmbalado = cursor.getInt(cursor.getColumnIndexOrThrow("produto_embalado")) == 1
                    if (produtoEmbalado) {
                        totalComEmbalagem++
                        tempoComEmbalagem += segundosDaLinha
                    } else {
                        totalSemEmbalagem++
                        tempoSemEmbalagem += segundosDaLinha
                    }
                }
            } while (cursor.moveToNext())

            if (totalEnderecosPicking == 0 && totalCaixaFechada == 0) return null

            // --- CÁLCULOS CORRIGIDOS ---
            val totalEnderecosGerais = totalEnderecosPicking + totalCaixaFechada
            val tempoTotalGeral = totalTempoPicking + tempoCaixaFechada

            val mediaGeralSegundos = if (totalEnderecosGerais > 0)
                tempoTotalGeral.toDouble() / totalEnderecosGerais else 0.0

            val mediaComEmbalagemSegundos = if (totalComEmbalagem > 0)
                tempoComEmbalagem.toDouble() / totalComEmbalagem else 0.0

            val mediaSemEmbalagemSegundos = if (totalSemEmbalagem > 0)
                tempoSemEmbalagem.toDouble() / totalSemEmbalagem else 0.0

            val mediaCaixaFechadaSegundos = if (totalCaixaFechada > 0)
                tempoCaixaFechada.toDouble() / totalCaixaFechada else 0.0

            val mediaItensPorEndereco = if (totalEnderecosGerais > 0)
                totalItensPicking.toDouble() / totalEnderecosGerais else 0.0

            // 🔧 Previsões corrigidas (usam a média geral)
            val previsaoEm1h = if (mediaGeralSegundos > 0) (3600 / mediaGeralSegundos).roundToLong().toInt() else 0
            val previsaoEm7h20m = if (mediaGeralSegundos > 0) (26400 / mediaGeralSegundos).roundToLong().toInt() else 0

            return ResultadosCalculados(
                nomeModulo = nomeModulo,
                mediaGeralSegundos = mediaGeralSegundos.toLong(),
                mediaComEmbalagemSegundos = mediaComEmbalagemSegundos.toLong(),
                mediaSemEmbalagemSegundos = mediaSemEmbalagemSegundos.toLong(),
                mediaCaixaFechadaSegundos = mediaCaixaFechadaSegundos.toLong(),
                mediaItensPorEndereco = mediaItensPorEndereco,
                previsaoEm1h = previsaoEm1h,
                previsaoEm7h20m = previsaoEm7h20m
            )
        }
    }

    private fun atualizarUI(resultados: ResultadosCalculados) {
        binding.moduloGeralTextView.text = resultados.nomeModulo
        binding.tempoGeralTextView.text = formatarSegundos(resultados.mediaGeralSegundos)
        binding.tempoGeralCEmbalagemTextView.text = formatarSegundos(resultados.mediaComEmbalagemSegundos)
        binding.tempoGeralSemEmbalagemtextView.text = formatarSegundos(resultados.mediaSemEmbalagemSegundos)
        binding.tempoGeralCaixaFechadaTextView.text = formatarSegundos(resultados.mediaCaixaFechadaSegundos)
        binding.quantidadeGeralItensPorEnderecoTextView.text = String.format("%.1f", resultados.mediaItensPorEndereco)
        binding.previsao1hTextView.text = resultados.previsaoEm1h.toString()
        binding.previsao7h20TextView.text = resultados.previsaoEm7h20m.toString()
    }

    private fun formatarSegundos(segundosTotais: Long): String {
        if (segundosTotais <= 0) return "00:00:00"
        val horas = segundosTotais / 3600
        val minutos = (segundosTotais % 3600) / 60
        val segundos = segundosTotais % 60
        return String.format("%02d:%02d:%02d", horas, minutos, segundos)
    }
}
