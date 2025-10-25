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

    // Armazena a lista de IDs de sessão, ordenados do mais recente para o mais antigo.
    private var listaDeSessoes = mutableListOf<Int>()
    // Aponta para o índice da sessão atual que está sendo exibida.
    private var posicaoAtual = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Garante que, ao abrir, a tela sempre exiba a sessão mais recente.
        posicaoAtual = 0
        carregarSessoesDisponiveis()

        configurarNavegacao()
        configurarAcoes()
    }

    /**
     * Configura as ações de clique para os botões principais, como Voltar e Excluir.
     */
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


    /**
     * Exibe um diálogo de alerta para confirmar a exclusão de uma sessão,
     * prevenindo a perda acidental de dados.
     */
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
            // A ação de clique negativa pode ser nula, o que apenas fecha o diálogo.
            .setNegativeButton("Não", null)
            .create()
            .show()
    }

    /**
     * Deleta uma sessão e seus registros associados do banco de dados.
     * Utiliza uma transação para garantir a integridade dos dados (operação atômica).
     */
    private fun deletarSessao(idSessao: Int) {
        try {
            bancoDeDados.writableDatabase.beginTransaction()
            // Deleta os registros filhos primeiro para manter a integridade referencial.
            bancoDeDados.writableDatabase.delete(
                "RegistroColeta",
                "id_sessao = ?",
                arrayOf(idSessao.toString())
            )
            // Em seguida, deleta a sessão principal.
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

        // Após a exclusão, recarrega a lista e reseta a visão para a sessão mais recente.
        posicaoAtual = 0
        carregarSessoesDisponiveis()
    }

    /**
     * Busca todos os IDs de sessão no banco de dados, ordenando-os do mais recente
     * para o mais antigo (ORDER BY id_sessao DESC). Em seguida, aciona a exibição do primeiro item.
     */
    private fun carregarSessoesDisponiveis() {
        val ids = mutableListOf<Int>()
        // Ordena por ID decrescente para que o registro mais recente seja sempre o primeiro.
        val sql = "SELECT id_sessao FROM SessaoColeta ORDER BY id_sessao DESC"
        val cursor = bancoDeDados.readableDatabase.rawQuery(sql, null)

        cursor.use { // O 'use' garante que o cursor será fechado automaticamente, mesmo em caso de erro.
            if (it.moveToFirst()) {
                do {
                    ids.add(it.getInt(it.getColumnIndexOrThrow("id_sessao")))
                } while (it.moveToNext())
            }
        }

        listaDeSessoes = ids
        Log.d("info_db_nav", "Sessões encontradas (ordem DESC): $listaDeSessoes")

        if (listaDeSessoes.isNotEmpty()) {
            // Garante que a posição atual seja válida dentro dos limites da nova lista.
            if (posicaoAtual >= listaDeSessoes.size) {
                posicaoAtual = 0
            }
            exibirDadosDaSessao(posicaoAtual)
        } else {
            // Se não houver mais sessões, limpa a tela.
            Log.w("info_db_nav", "Nenhuma sessão encontrada no histórico.")
            limparTela()
        }
    }

    /**
     * Configura os listeners para os botões de navegação 'próximo' e 'anterior'.
     */
    private fun configurarNavegacao() {
        binding.nextImageView.setOnClickListener {
            // Navega para o próximo item (mais antigo) se não estiver no final da lista.
            if (posicaoAtual < listaDeSessoes.size - 1) {
                posicaoAtual++
                exibirDadosDaSessao(posicaoAtual)
            }
        }

        binding.previousImageView.setOnClickListener {
            // Navega para o item anterior (mais recente) se não estiver no início da lista.
            if (posicaoAtual > 0) {
                posicaoAtual--
                exibirDadosDaSessao(posicaoAtual)
            }
        }
    }

    /**
     * Função central que busca, calcula e exibe todos os dados para um ID de sessão específico.
     */
    private fun exibirDadosDaSessao(posicao: Int) {
        // Validação de segurança para evitar 'IndexOutOfBoundsException'.
        if (listaDeSessoes.isEmpty() || posicao !in listaDeSessoes.indices) {
            limparTela()
            return
        }

        val idSessao = listaDeSessoes[posicao]
        Log.d("info_db_nav", "Exibindo dados para a sessão ID: $idSessao (Posição na lista: $posicao)")

        // Atualiza a visibilidade dos botões de navegação antes de carregar os dados.
        atualizarVisibilidadeBotoes()

        // 1. Busca e exibe os dados da tabela 'SessaoColeta' (cabeçalho da tela).
        val sqlSessao = "SELECT *, strftime('%d/%m/%Y - %H:%M', data_sessao, 'localtime') AS data_formatada FROM SessaoColeta WHERE id_sessao = ?"
        bancoDeDados.readableDatabase.rawQuery(sqlSessao, arrayOf(idSessao.toString())).use { cursorSessao ->
            if (cursorSessao.moveToFirst()) {
                binding.idTextView.text = cursorSessao.getInt(cursorSessao.getColumnIndexOrThrow("id_sessao")).toString()
                binding.nomeTextView.text = cursorSessao.getString(cursorSessao.getColumnIndexOrThrow("nome_separador"))
                binding.moduloTextView.text = cursorSessao.getString(cursorSessao.getColumnIndexOrThrow("modulo_selecionado"))
                binding.dataTextView.text = cursorSessao.getString(cursorSessao.getColumnIndexOrThrow("data_formatada"))
            } else {
                limparTela()
                return // Encerra a função se os dados da sessão principal não forem encontrados.
            }
        }

        // 2. Busca todos os registros associados para realizar os cálculos.
        val sqlRegistros = "SELECT * FROM RegistroColeta WHERE id_sessao = ?"
        bancoDeDados.readableDatabase.rawQuery(sqlRegistros, arrayOf(idSessao.toString())).use { cursorRegistros ->
            val totalEnderecos = cursorRegistros.count
            var totalTempoEmSegundos: Long = 0
            var totalItensColetados: Int = 0
            var totalComSaco: Int = 0
            var totalSemSaco: Int = 0
            var totalCaixaFechada: Int = 0
            var tempoCategoriaComSaco: Long = 0
            var tempoCategoriaSemSaco: Long = 0
            var tempoCategoriaCaixaFechada: Long = 0

            // 3. Itera sobre os registros para acumular os totais.
            while (cursorRegistros.moveToNext()) {
                var segundosDaLinha: Long = 0
                val tempoColetaStr = cursorRegistros.getString(cursorRegistros.getColumnIndexOrThrow("tempo_coleta"))
                if (!tempoColetaStr.isNullOrEmpty() && tempoColetaStr.contains(":")) {
                    try {
                        val partes = tempoColetaStr.split(":")
                        // Converte o formato "MM:SS" para um total de segundos.
                        segundosDaLinha = (partes[0].toLong() * 60) + partes[1].toLong()
                        totalTempoEmSegundos += segundosDaLinha
                    } catch (e: Exception) {
                        Log.e("info_db_soma", "Erro ao converter tempo: '$tempoColetaStr'", e)
                    }
                }
                totalItensColetados += cursorRegistros.getInt(cursorRegistros.getColumnIndexOrThrow("qtd_itens_coletados"))

                // Separa a contagem e o tempo por categoria.
                when {
                    cursorRegistros.getInt(cursorRegistros.getColumnIndexOrThrow("caixa_fechada")) == 1 -> {
                        totalCaixaFechada++
                        tempoCategoriaCaixaFechada += segundosDaLinha
                    }
                    cursorRegistros.getInt(cursorRegistros.getColumnIndexOrThrow("produto_embalado")) == 1 -> {
                        totalComSaco++
                        tempoCategoriaComSaco += segundosDaLinha
                    }
                    else -> {
                        totalSemSaco++
                        tempoCategoriaSemSaco += segundosDaLinha
                    }
                }
            }

            // 4. Calcula as médias e previsões finais, com proteção contra divisão por zero.
            val mediaGeral = if (totalEnderecos > 0) totalTempoEmSegundos.toDouble() / totalEnderecos else 0.0
            val mediaComSaco = if (totalComSaco > 0) tempoCategoriaComSaco / totalComSaco else 0
            val mediaSemSaco = if (totalSemSaco > 0) tempoCategoriaSemSaco / totalSemSaco else 0
            val mediaCaixaFechada = if (totalCaixaFechada > 0) tempoCategoriaCaixaFechada / totalCaixaFechada else 0
            val previsaoEm1h = if (mediaGeral > 0) (3600 / mediaGeral).toInt() else 0
            val previsaoEm7h20m = if (mediaGeral > 0) (26400 / mediaGeral).toInt() else 0


            // 5. Atualiza a UI com todos os dados calculados.
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
            binding.tarefasEm1h.text = previsaoEm1h.toString()
            binding.tarefasEm720h.text = previsaoEm7h20m.toString()
        }
    }

    /**
     * Controla a visibilidade dos botões de navegação e exclusão com base
     * na existência de dados e na posição atual na lista.
     */
    private fun atualizarVisibilidadeBotoes() {
        val temSessoes = listaDeSessoes.isNotEmpty()
        binding.previousImageView.visibility = if (temSessoes && posicaoAtual > 0) View.VISIBLE else View.INVISIBLE
        binding.nextImageView.visibility = if (temSessoes && posicaoAtual < listaDeSessoes.size - 1) View.VISIBLE else View.INVISIBLE
        binding.deleteTextView.visibility = if (temSessoes) View.VISIBLE else View.GONE
    }

    /**
     * Limpa todos os campos da tela, usado quando não há histórico
     * ou após a exclusão de todos os registros.
     */
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

    /**
     * Utilitário para formatar um total de segundos no formato de exibição HH:mm:ss.
     */
    private fun formatarSegundos(segundosTotais: Long): String {
        if (segundosTotais <= 0) return "00:00:00"
        val horas = TimeUnit.SECONDS.toHours(segundosTotais)
        val minutos = TimeUnit.SECONDS.toMinutes(segundosTotais) % 60
        val segundos = segundosTotais % 60
        return String.format("%02d:%02d:%02d", horas, minutos, segundos)
    }
}
