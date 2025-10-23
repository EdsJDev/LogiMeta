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

        // --- ALTERAÇÃO IMPORTANTE ---
        // Garante que a primeira carga sempre começará do registro mais recente.
        posicaoAtual = 0
        carregarSessoesDisponiveis()

        configurarNavegacao()

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

        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza de que deseja excluir esta coleta (ID: $idSessaoParaDeletar)?\n\nEsta ação não pode ser desfeita.")
            .setIcon(R.drawable.delete_data_24)
            .setPositiveButton("Sim, Excluir") { dialog, _ ->
                deletarSessao(idSessaoParaDeletar)
                dialog.dismiss()
            }
            .setNegativeButton("Não") { dialog, _ ->
                dialog.dismiss()
            }
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

        // --- ALTERAÇÃO IMPORTANTE ---
        // Reseta a posição para 0 para mostrar o novo item mais recente após a exclusão.
        posicaoAtual = 0
        carregarSessoesDisponiveis()
    }

    /**
     * Busca todos os IDs de sessão, ordena e exibe a sessão mais recente.
     */
    private fun carregarSessoesDisponiveis() {
        val ids = mutableListOf<Int>()
        // --- ALTERAÇÃO PRINCIPAL AQUI ---
        // Ordena por ID em ordem DECRESCENTE (DESC) para que o maior ID venha primeiro.
        val sql = "SELECT id_sessao FROM SessaoColeta ORDER BY id_sessao DESC"
        val cursor = bancoDeDados.readableDatabase.rawQuery(sql, null)

        if (cursor.moveToFirst()) {
            do {
                ids.add(cursor.getInt(cursor.getColumnIndexOrThrow("id_sessao")))
            } while (cursor.moveToNext())
        }
        cursor.close()

        listaDeSessoes = ids
        Log.d("info_db_nav", "Sessões encontradas (ordem DESC): $listaDeSessoes")

        if (listaDeSessoes.isNotEmpty()) {
            // Se a posição atual for inválida, ajusta para a primeira (0), que é a mais recente.
            if (posicaoAtual >= listaDeSessoes.size) {
                posicaoAtual = 0
            }
            exibirDadosDaSessao(posicaoAtual)
        } else {
            Log.w("info_db_nav", "Nenhuma sessão encontrada no histórico.")
            posicaoAtual = 0
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
        if (listaDeSessoes.isEmpty() || posicao < 0 || posicao >= listaDeSessoes.size) {
            limparTela()
            return
        }

        val idSessao = listaDeSessoes[posicao]
        Log.d("info_db_nav", "Exibindo dados para a sessão ID: $idSessao (Posição na lista: $posicao)")

        atualizarVisibilidadeBotoes()

        // O resto desta função não precisa de alteração, pois ela já busca os dados pelo ID correto.
        val sqlSessao = "SELECT *, strftime('%d/%m/%Y - %H:%M', data_sessao, 'localtime') AS data_formatada FROM SessaoColeta WHERE id_sessao = ?"
        val cursorSessao = bancoDeDados.readableDatabase.rawQuery(sqlSessao, arrayOf(idSessao.toString()))

        if (cursorSessao.moveToFirst()) {
            binding.idTextView.text = cursorSessao.getInt(cursorSessao.getColumnIndexOrThrow("id_sessao")).toString()
            binding.nomeTextView.text = cursorSessao.getString(cursorSessao.getColumnIndexOrThrow("nome_separador"))
            binding.moduloTextView.text = cursorSessao.getString(cursorSessao.getColumnIndexOrThrow("modulo_selecionado"))
            binding.dataTextView.text = cursorSessao.getString(cursorSessao.getColumnIndexOrThrow("data_formatada"))
            cursorSessao.close()
        } else {
            cursorSessao.close()
            limparTela()
            return
        }

        val sqlRegistros = "SELECT * FROM RegistroColeta WHERE id_sessao = ?"
        val cursorRegistros = bancoDeDados.readableDatabase.rawQuery(sqlRegistros, arrayOf(idSessao.toString()))

        val totalEnderecos = cursorRegistros.count
        var totalTempoEmSegundos: Long = 0
        var totalItensColetados: Int = 0
        var totalComSaco: Int = 0
        var totalSemSaco: Int = 0
        var totalCaixaFechada: Int = 0
        var tempoCategoriaComSaco: Long = 0
        var tempoCategoriaSemSaco: Long = 0
        var tempoCategoriaCaixaFechada: Long = 0

        while (cursorRegistros.moveToNext()) {
            var segundosDaLinha: Long = 0
            val tempoColetaStr = cursorRegistros.getString(cursorRegistros.getColumnIndexOrThrow("tempo_coleta"))
            if (!tempoColetaStr.isNullOrEmpty() && tempoColetaStr.contains(":")) {
                try {
                    val partes = tempoColetaStr.split(":")
                    segundosDaLinha = (partes[0].toLong() * 60) + partes[1].toLong()
                    totalTempoEmSegundos += segundosDaLinha
                } catch (e: Exception) {
                    Log.e("info_db_soma", "Erro ao converter tempo: '$tempoColetaStr'", e)
                }
            }
            totalItensColetados += cursorRegistros.getInt(cursorRegistros.getColumnIndexOrThrow("qtd_itens_coletados"))
            val caixaFechada = cursorRegistros.getInt(cursorRegistros.getColumnIndexOrThrow("caixa_fechada")) == 1
            if (caixaFechada) {
                totalCaixaFechada++
                tempoCategoriaCaixaFechada += segundosDaLinha
                continue
            }
            val produtoEmbalado = cursorRegistros.getInt(cursorRegistros.getColumnIndexOrThrow("produto_embalado")) == 1
            if (produtoEmbalado) {
                totalComSaco++
                tempoCategoriaComSaco += segundosDaLinha
            } else {
                totalSemSaco++
                tempoCategoriaSemSaco += segundosDaLinha
            }
        }
        cursorRegistros.close()

        val mediaGeral = if (totalEnderecos > 0) totalTempoEmSegundos.toDouble() / totalEnderecos else 0.0
        val mediaComSaco = if (totalComSaco > 0) tempoCategoriaComSaco / totalComSaco else 0
        val mediaSemSaco = if (totalSemSaco > 0) tempoCategoriaSemSaco / totalSemSaco else 0
        val mediaCaixaFechada = if (totalCaixaFechada > 0) tempoCategoriaCaixaFechada / totalCaixaFechada else 0

        binding.tempoTotalTextView.text = formatarSegundos(totalTempoEmSegundos)
        binding.itensTextView.text = totalItensColetados.toString()
        binding.enderecosTextView.text = totalEnderecos.toString()
        binding.itensComSacoFrutaTextView.text = totalComSaco.toString()
        binding.itensSemSacoFrutaTextView.text = totalSemSaco.toString()
        binding.itensCaixaFechadaTextView.text = totalCaixaFechada.toString()
        binding.tempoMedioTotalTextView.text = formatarSegundos(mediaGeral.toLong())
        binding.tempoComSacoFrutaTextView.text = formatarSegundos(mediaComSaco)
        binding.tempoSemSacoFrutaTextView.text = formatarSegundos(mediaSemSaco)
        binding.tempoCaixaFechadaTextView.text = formatarSegundos(mediaCaixaFechada)

        var previsaoEm1h = 0
        var previsaoEm7h20m = 0
        if (mediaGeral > 0) {
            previsaoEm1h = (3600 / mediaGeral).toInt()
            previsaoEm7h20m = (26400 / mediaGeral).toInt()
        }
        binding.tarefasEm1h.text = previsaoEm1h.toString()
        binding.tarefasEm720h.text = previsaoEm7h20m.toString()
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
