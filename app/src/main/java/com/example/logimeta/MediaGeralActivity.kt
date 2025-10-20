package com.example.logimeta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
// Removido o import do Compose que não é usado aqui
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.logimeta.database.DatabaseHelper
import com.example.logimeta.databinding.ActivityMediaGeralBinding
import java.util.concurrent.TimeUnit

class MediaGeralActivity : AppCompatActivity() {

    // TAG para filtrar os logs e facilitar a depuração
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

        // Chama a função principal que busca, calcula e exibe os dados
        calcularEExibirMedias()

        binding.mediaGeralVoltarButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // adicionar finish para fechar a tela atual
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Busca os dados de um módulo específico, calcula as médias e atualiza a interface.
     */
    private fun calcularEExibirMedias() {
        val moduloParaFiltrar = "Plantas baixo giro" // Módulo que você quer analisar

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

        // O '?' é substituído pelo valor em arrayOf() de forma segura
        val cursor = bancoDeDados.readableDatabase.rawQuery(sql, arrayOf(moduloParaFiltrar))

        // Variáveis para armazenar os totais
        var totalEnderecos = 0
        var totalTempoSegundos: Long = 0
        var totalItens = 0
        var totalComEmbalagem = 0
        var totalSemEmbalagem = 0
        var tempoComEmbalagem: Long = 0
        var tempoSemEmbalagem: Long = 0
        var nomeModulo = "Plantas baixo giro" // Valor padrão

        if (cursor.moveToFirst()) {
            totalEnderecos = cursor.count // O total de endereços é o número de linhas retornadas
            nomeModulo = cursor.getString(cursor.getColumnIndexOrThrow("modulo_selecionado"))

            do {
                // --- Processamento de cada linha ---
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
                    // Lógica exclusiva: Se for caixa fechada, não conta como 'com/sem embalagem'
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
        val mediaGeralSegundos = if (totalEnderecos > 0) totalTempoSegundos / totalEnderecos else 0
        val mediaComEmbalagemSegundos = if (totalComEmbalagem > 0) tempoComEmbalagem / totalComEmbalagem else 0
        val mediaSemEmbalagemSegundos = if (totalSemEmbalagem > 0) tempoSemEmbalagem / totalSemEmbalagem else 0
        val mediaItensPorEndereco = if (totalEnderecos > 0) totalItens.toDouble() / totalEnderecos else 0.0

        // --- Atualização da Interface do Usuário (UI) - NOMES CORRIGIDOS AQUI ---
            binding.moduloGeralTextView.text = nomeModulo
            binding.tempoGeralTextView.text = formatarSegundos(mediaGeralSegundos)
            binding.tempoGeralCEmbalagemTextView.text = formatarSegundos(mediaComEmbalagemSegundos)
            //binding.tempoGeralSemEmbalagemTextView.text = formatarSegundos(mediaSemEmbalagemSegundos)

        //binding.mediaGeralTempoSemEmbalagemTextview.text = formatarSegundos(mediaSemEmbalagemSegundos)
//        binding.mediaGeralQtdMediaItensTextview.text = String.format("%.1f", mediaItensPorEndereco) // Formata para uma casa decimal




        Log.d(TAG, "Cálculos concluídos e UI atualizada.")
    }

    /**
     * Converte um valor total em segundos para uma String no formato "HH:MM:SS".
     */
    private fun formatarSegundos(segundosTotais: Long): String {
        if (segundosTotais <= 0) return "00:00:00"
        val horas = TimeUnit.SECONDS.toHours(segundosTotais)
        val minutos = TimeUnit.SECONDS.toMinutes(segundosTotais) % 60
        val segundos = segundosTotais % 60
        return String.format("%02d:%02d:%02d", horas, minutos, segundos)
    }
}
