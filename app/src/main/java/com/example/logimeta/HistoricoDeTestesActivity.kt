package com.example.logimeta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
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

    // Armazena os IDs de todas as sessões de coleta disponíveis.
    private var listaDeSessoes = listOf<Int>()
    // Controla o índice da sessão atualmente exibida na lista.
    private var posicaoAtual = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Carrega a lista de sessões disponíveis do banco de dados.
        carregarSessoesDisponiveis()

        // Configura os cliques dos botões de navegação.
        configurarNavegacao()

        binding.historicoVoltarButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    /**
     * Busca todos os IDs de sessão no banco de dados, ordena do mais antigo ao mais recente,
     * e exibe a sessão mais recente (a última da lista).
     */
    private fun carregarSessoesDisponiveis() {
        val ids = mutableListOf<Int>()
        val sql = "SELECT id_sessao FROM SessaoColeta ORDER BY id_sessao ASC"
        val cursor = bancoDeDados.readableDatabase.rawQuery(sql, null)

        if (cursor.moveToFirst()) {
            do {
                ids.add(cursor.getInt(cursor.getColumnIndexOrThrow("id_sessao")))
            } while (cursor.moveToNext())
        }
        cursor.close()

        listaDeSessoes = ids
        Log.d("info_db_nav", "Sessões encontradas: $listaDeSessoes")

        // Verifica se alguma sessão foi encontrada.
        if (listaDeSessoes.isNotEmpty()) {
            // Define a posição inicial para a última sessão (a mais recente).
            posicaoAtual = listaDeSessoes.size - 1
            exibirDadosDaSessao(posicaoAtual)
        } else {
            // Se não houver sessões, limpa a tela.
            Log.w("info_db_nav", "Nenhuma sessão encontrada no histórico.")
            limparTela()
        }
    }

    /**
     * Configura os listeners de clique para os botões de navegação 'previous' e 'next'.
     */
    private fun configurarNavegacao() {
        binding.nextImageView.setOnClickListener {
            // Avança para a próxima posição se não estiver no final da lista.
            if (posicaoAtual < listaDeSessoes.size - 1) {
                posicaoAtual++
                exibirDadosDaSessao(posicaoAtual)
            }
        }

        binding.previousImageView.setOnClickListener {
            // Retrocede para a posição anterior se não estiver no início da lista.
            if (posicaoAtual > 0) {
                posicaoAtual--
                exibirDadosDaSessao(posicaoAtual)
            }
        }
    }

    /**
     * Busca e exibe todos os dados (informações da sessão, totais e médias)
     * para uma sessão específica, com base em sua posição na lista.
     */
    private fun exibirDadosDaSessao(posicao: Int) {
        if (listaDeSessoes.isEmpty() || posicao < 0 || posicao >= listaDeSessoes.size) {
            Log.e("info_db_nav", "Posição inválida ou lista de sessões vazia.")
            limparTela()
            return
        }

        val idSessao = listaDeSessoes[posicao]
        Log.d("info_db_nav", "Exibindo dados para a sessão ID: $idSessao (Posição: $posicao)")

        // Atualiza a visibilidade dos botões de navegação.
        atualizarVisibilidadeBotoes()

        // Busca e exibe as informações principais da sessão.
        val sqlSessao = "SELECT *, strftime('%d/%m/%Y - %H:%M', data_sessao, 'localtime') AS data_formatada FROM SessaoColeta WHERE id_sessao = ?"
        val cursorSessao = bancoDeDados.readableDatabase.rawQuery(sqlSessao, arrayOf(idSessao.toString()))

        if (cursorSessao.moveToFirst()) {
            binding.idTextView.text = cursorSessao.getInt(cursorSessao.getColumnIndexOrThrow("id_sessao")).toString()
            binding.nomeTextView.text = cursorSessao.getString(cursorSessao.getColumnIndexOrThrow("nome_separador"))
            binding.moduloTextView.text = cursorSessao.getString(cursorSessao.getColumnIndexOrThrow("modulo_selecionado"))
            binding.dataTextView.text = cursorSessao.getString(cursorSessao.getColumnIndexOrThrow("data_formatada"))
        }
        cursorSessao.close()

        // Busca os registros de coleta para calcular totais e médias.
        val sqlRegistros = "SELECT * FROM RegistroColeta WHERE id_sessao = ?"
        val cursorRegistros = bancoDeDados.readableDatabase.rawQuery(sqlRegistros, arrayOf(idSessao.toString()))

        val totalEnderecos = cursorRegistros.count
        var totalTempoEmSegundos: Long = 0
        var totalItensColetados: Int = 0
        var totalComSaco: Int = 0
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

            val produtoEmbalado = cursorRegistros.getInt(cursorRegistros.getColumnIndexOrThrow("produto_embalado")) == 1
            val caixaFechada = cursorRegistros.getInt(cursorRegistros.getColumnIndexOrThrow("caixa_fechada")) == 1

            totalItensColetados += cursorRegistros.getInt(cursorRegistros.getColumnIndexOrThrow("qtd_itens_coletados"))

            if (produtoEmbalado) {
                totalComSaco++
                tempoCategoriaComSaco += segundosDaLinha
            } else {
                tempoCategoriaSemSaco += segundosDaLinha
            }
            if (caixaFechada) {
                totalCaixaFechada++
                tempoCategoriaCaixaFechada += segundosDaLinha
            }
        }
        cursorRegistros.close()

        // Calcula os totais e médias finais.
        val totalSemSaco = totalEnderecos - totalComSaco
        val mediaGeral = if (totalEnderecos > 0) totalTempoEmSegundos / totalEnderecos else 0
        val mediaComSaco = if (totalComSaco > 0) tempoCategoriaComSaco / totalComSaco else 0
        val mediaSemSaco = if (totalSemSaco > 0) tempoCategoriaSemSaco / totalSemSaco else 0
        val mediaCaixaFechada = if (totalCaixaFechada > 0) tempoCategoriaCaixaFechada / totalCaixaFechada else 0

        // Formata os resultados para exibição.
        val tempoTotalFormatado = formatarSegundos(totalTempoEmSegundos)
        val mediaGeralFormatada = formatarSegundos(mediaGeral)
        val mediaComSacoFormatada = formatarSegundos(mediaComSaco)
        val mediaSemSacoFormatada = formatarSegundos(mediaSemSaco)
        val mediaCaixaFechadaFormatada = formatarSegundos(mediaCaixaFechada)

        // Atribui todos os valores à UI.
        binding.tempoTotalTextView.text = tempoTotalFormatado
        binding.itensTextView.text = totalItensColetados.toString()
        binding.enderecosTextView.text = totalEnderecos.toString()
        binding.itensComSacoFrutaTextView.text = totalComSaco.toString()
        binding.itensSemSacoFrutaTextView.text = totalSemSaco.toString()
        binding.tempoMedioTotalTextView.text = mediaGeralFormatada
        binding.tempoComSacoFrutaTextView.text = mediaComSacoFormatada
        binding.tempoSemSacoFrutaTextView.text = mediaSemSacoFormatada
        binding.tempoCaixaFechadaTextView.text = mediaCaixaFechadaFormatada
    }

    /**
     * Atualiza a visibilidade dos botões de navegação.
     * Oculta o botão 'previous' se estiver no primeiro item.
     * Oculta o botão 'next' se estiver no último item.
     */
    private fun atualizarVisibilidadeBotoes() {
        binding.previousImageView.visibility = if (posicaoAtual > 0) View.VISIBLE else View.INVISIBLE
        binding.nextImageView.visibility = if (posicaoAtual < listaDeSessoes.size - 1) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Limpa todos os campos de texto da tela e oculta os botões de navegação.
     * Usado quando não há histórico para exibir.
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
        binding.tempoMedioTotalTextView.text = "00:00:00"
        binding.tempoComSacoFrutaTextView.text = "00:00:00"
        binding.tempoSemSacoFrutaTextView.text = "00:00:00"
        binding.tempoCaixaFechadaTextView.text = "00:00:00"
        atualizarVisibilidadeBotoes() // Garante que os botões fiquem ocultos.
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
