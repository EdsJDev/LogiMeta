package com.example.logimeta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.logimeta.database.DatabaseHelper
import com.example.logimeta.databinding.ActivityHistoricoDeTestesBinding
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

/**
 * Activity para visualizar o histórico de coletas de dados.
 * Permite navegar entre as sessões salvas, ver estatísticas detalhadas
 * e excluir registros individuais.
 */
class HistoricoDeTestesActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityHistoricoDeTestesBinding.inflate(layoutInflater)
    }
    private val bancoDeDados by lazy {
        DatabaseHelper(this)
    }

    private var listaDeSessoes = mutableListOf<Int>()
    private var posicaoAtual = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        posicaoAtual = 0
        carregarSessoesDisponiveis()

        configurarNavegacao()
        configurarAcoes()
    }

    private fun configurarAcoes() {
        binding.historicoVoltarButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.deleteTextView.setOnClickListener {
            if (listaDeSessoes.isNotEmpty()) {
                mostrarDialogoDeConfirmacao()
            }
        }
    }

    private fun mostrarDialogoDeConfirmacao() {
        val idSessaoParaDeletar = listaDeSessoes[posicaoAtual]

        AlertDialog.Builder(this, R.style.AlertDialogTheme_Custom)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza de que deseja excluir esta coleta (ID: $idSessaoParaDeletar)?\n\nEsta ação não pode ser desfeita.")
            .setIcon(R.drawable.delete_data_24)
            .setPositiveButton("Sim, Excluir") { dialog, _ ->
                deletarSessao(idSessaoParaDeletar)
                dialog.dismiss()
            }
            .setNegativeButton("Não", null)
            .create()
            .show()
    }

    private fun deletarSessao(idSessao: Int) {
        try {
            bancoDeDados.writableDatabase.beginTransaction()
            bancoDeDados.writableDatabase.delete(
                "RegistroColeta",
                "id_sessao = ?",
                arrayOf(idSessao.toString())
            )
            bancoDeDados.writableDatabase.delete(
                "SessaoColeta",
                "id_sessao = ?",
                arrayOf(idSessao.toString())
            )
            bancoDeDados.writableDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("info_db_delete", "Erro ao deletar sessão $idSessao", e)
        } finally {
            bancoDeDados.writableDatabase.endTransaction()
        }

        posicaoAtual = 0
        carregarSessoesDisponiveis()
    }

    private fun carregarSessoesDisponiveis() {
        val ids = mutableListOf<Int>()
        val sql = "SELECT id_sessao FROM SessaoColeta ORDER BY id_sessao DESC"
        val cursor = bancoDeDados.readableDatabase.rawQuery(sql, null)

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    ids.add(it.getInt(it.getColumnIndexOrThrow("id_sessao")))
                } while (it.moveToNext())
            }
        }

        listaDeSessoes = ids
        Log.d("info_db_nav", "Sessões encontradas (ordem DESC): $listaDeSessoes")

        if (listaDeSessoes.isNotEmpty()) {
            if (posicaoAtual >= listaDeSessoes.size) {
                posicaoAtual = 0
            }
            exibirDadosDaSessao(posicaoAtual)
        } else {
            limparTela()
        }
    }

    private fun configurarNavegacao() {
        binding.nextImageView.setOnClickListener {
            if (posicaoAtual < listaDeSessoes.size - 1) {
                posicaoAtual++
                exibirDadosDaSessao(posicaoAtual)
            }
        }

        binding.previousImageView.setOnClickListener {
            if (posicaoAtual > 0) {
                posicaoAtual--
                exibirDadosDaSessao(posicaoAtual)
            }
        }
    }

    private fun exibirDadosDaSessao(posicao: Int) {
        if (listaDeSessoes.isEmpty() || posicao !in listaDeSessoes.indices) {
            limparTela()
            return
        }

        val idSessao = listaDeSessoes[posicao]
        Log.d("info_db_nav", "Exibindo dados para a sessão ID: $idSessao (Posição: $posicao)")

        atualizarVisibilidadeBotoes()

        val sqlSessao =
            "SELECT *, strftime('%d/%m/%Y - %H:%M', data_sessao, 'localtime') AS data_formatada FROM SessaoColeta WHERE id_sessao = ?"
        bancoDeDados.readableDatabase.rawQuery(sqlSessao, arrayOf(idSessao.toString())).use { cursorSessao ->
            if (cursorSessao.moveToFirst()) {
                binding.idTextView.text = cursorSessao.getInt(cursorSessao.getColumnIndexOrThrow("id_sessao")).toString()
                binding.nomeTextView.text = cursorSessao.getString(cursorSessao.getColumnIndexOrThrow("nome_separador"))
                binding.moduloTextView.text = cursorSessao.getString(cursorSessao.getColumnIndexOrThrow("modulo_selecionado"))
                binding.dataTextView.text = cursorSessao.getString(cursorSessao.getColumnIndexOrThrow("data_formatada"))
            } else {
                limparTela()
                return
            }
        }

        val sqlRegistros = "SELECT * FROM RegistroColeta WHERE id_sessao = ?"
        bancoDeDados.readableDatabase.rawQuery(sqlRegistros, arrayOf(idSessao.toString())).use { cursorRegistros ->
            if (!cursorRegistros.moveToFirst()) {
                limparTela()
                return@use
            }

            var tempoTotalSessao: Long = 0
            var totalItensColetados: Int = 0

            var totalEnderecosComSaco = 0
            var tempoTotalComSaco: Long = 0

            var totalEnderecosSemSaco = 0
            var tempoTotalSemSaco: Long = 0

            var totalTarefasCaixaFechada = 0
            var tempoTotalCaixaFechada: Long = 0

            do {
                var segundosDaLinha: Long = 0
                val tempoColetaStr = cursorRegistros.getString(cursorRegistros.getColumnIndexOrThrow("tempo_coleta"))
                if (!tempoColetaStr.isNullOrEmpty()) {
                    try {
                        val partes = tempoColetaStr.split(":")
                        segundosDaLinha = when (partes.size) {
                            3 -> (partes[0].toLong() * 3600) + (partes[1].toLong() * 60) + partes[2].toLong()
                            2 -> (partes[0].toLong() * 60) + partes[1].toLong()
                            else -> 0
                        }
                        tempoTotalSessao += segundosDaLinha
                    } catch (e: Exception) {
                        Log.e("info_db_soma", "Erro ao converter tempo: '$tempoColetaStr'", e)
                    }
                }

                val qtdItensStr = cursorRegistros.getString(cursorRegistros.getColumnIndexOrThrow("qtd_itens_coletados"))
                totalItensColetados += qtdItensStr.toIntOrNull() ?: 0

                if (cursorRegistros.getInt(cursorRegistros.getColumnIndexOrThrow("caixa_fechada")) == 1) {
                    totalTarefasCaixaFechada++
                    tempoTotalCaixaFechada += segundosDaLinha
                } else {
                    if (cursorRegistros.getInt(cursorRegistros.getColumnIndexOrThrow("produto_embalado")) == 1) {
                        totalEnderecosComSaco++
                        tempoTotalComSaco += segundosDaLinha
                    } else {
                        totalEnderecosSemSaco++
                        tempoTotalSemSaco += segundosDaLinha
                    }
                }
            } while (cursorRegistros.moveToNext())

            val totalEnderecosDePicking = totalEnderecosComSaco + totalEnderecosSemSaco
            val tempoTotalDePicking = tempoTotalComSaco + tempoTotalSemSaco

            // agora a média geral soma todos os tipos de endereço
            val totalEnderecosGerais = totalEnderecosDePicking + totalTarefasCaixaFechada
            val tempoTotalGeral = tempoTotalDePicking + tempoTotalCaixaFechada

            val mediaGeral = if (totalEnderecosGerais > 0)
                tempoTotalGeral.toDouble() / totalEnderecosGerais
            else 0.0

            //  previsões com base na média geral real
            val previsaoEm1h = if (mediaGeral > 0) (3600 / mediaGeral).roundToLong() else 0
            val previsaoEm7h20m = if (mediaGeral > 0) (25200 / mediaGeral).roundToLong() else 0

            val mediaComSaco = if (totalEnderecosComSaco > 0)
                tempoTotalComSaco.toDouble() / totalEnderecosComSaco else 0.0
            val mediaSemSaco = if (totalEnderecosSemSaco > 0)
                tempoTotalSemSaco.toDouble() / totalEnderecosSemSaco else 0.0
            val mediaCaixaFechada = if (totalTarefasCaixaFechada > 0)
                tempoTotalCaixaFechada.toDouble() / totalTarefasCaixaFechada else 0.0

            //  arredondamento da média para segundos mais próximos
            binding.tempoTotalTextView.text = formatarSegundos(tempoTotalSessao)
            binding.itensTextView.text = totalItensColetados.toString()
            binding.enderecosTextView.text = totalEnderecosGerais.toString()

            binding.itensComSacoFrutaTextView.text = totalEnderecosComSaco.toString()
            binding.itensSemSacoFrutaTextView.text = totalEnderecosSemSaco.toString()
            binding.itensCaixaFechadaTextView.text = totalTarefasCaixaFechada.toString()

            binding.tempoMedioTotalTextView.text = formatarSegundos(mediaGeral.roundToLong())
            binding.tempoComSacoFrutaTextView.text = formatarSegundos(mediaComSaco.roundToLong())
            binding.tempoSemSacoFrutaTextView.text = formatarSegundos(mediaSemSaco.roundToLong())
            binding.tempoCaixaFechadaTextView.text = formatarSegundos(mediaCaixaFechada.roundToLong())

            binding.tarefasEm1h.text = previsaoEm1h.toString()
            binding.tarefasEm720h.text = previsaoEm7h20m.toString()
        }
    }

    private fun atualizarVisibilidadeBotoes() {
        val temSessoes = listaDeSessoes.isNotEmpty()
        binding.previousImageView.visibility = if (temSessoes && posicaoAtual > 0) View.VISIBLE else View.INVISIBLE
        binding.nextImageView.visibility = if (temSessoes && posicaoAtual < listaDeSessoes.size - 1) View.VISIBLE else View.INVISIBLE
        binding.deleteTextView.visibility = if (temSessoes) View.VISIBLE else View.GONE
    }

    private fun limparTela() {
        binding.idTextView.text = "-"
        binding.nomeTextView.text = "-"
        binding.moduloTextView.text = "-"
        binding.dataTextView.text = "-"
        binding.tempoTotalTextView.text = "00:00:00"
        binding.itensTextView.text = "0"
        binding.enderecosTextView.text = "0"
        binding.itensComSacoFrutaTextView.text = "0"
        binding.itensSemSacoFrutaTextView.text = "0"
        binding.itensCaixaFechadaTextView.text = "0"
        binding.tempoMedioTotalTextView.text = "00:00:00"
        binding.tempoComSacoFrutaTextView.text = "00:00:00"
        binding.tempoSemSacoFrutaTextView.text = "00:00:00"
        binding.tempoCaixaFechadaTextView.text = "00:00:00"
        binding.tarefasEm1h.text = "0"
        binding.tarefasEm720h.text = "0"
        atualizarVisibilidadeBotoes()
    }

    private fun formatarSegundos(segundosTotais: Long): String {
        if (segundosTotais <= 0) return "00:00:00"
        val horas = TimeUnit.SECONDS.toHours(segundosTotais)
        val minutos = TimeUnit.SECONDS.toMinutes(segundosTotais) % 60
        val segundos = segundosTotais % 60
        return String.format("%02d:%02d:%02d", horas, minutos, segundos)
    }
}
