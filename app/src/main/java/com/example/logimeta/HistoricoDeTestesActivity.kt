package com.example.logimeta

import android.content.Intent
import java.util.concurrent.TimeUnit
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.logimeta.database.DatabaseHelper
import com.example.logimeta.databinding.ActivityHistoricoDeTestesBinding

class HistoricoDeTestesActivity : AppCompatActivity() {

    private val bancoDeDados by lazy {
        DatabaseHelper(this)
    }

    private val binding by lazy {
        ActivityHistoricoDeTestesBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        listar()

        binding.historicoVoltarButton.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /*
     // Função de deleção para uso futuro.
     // A sintaxe correta é "DELETE FROM", e a query precisa ser executada.
     fun deletar() {
         try {
             val db = bancoDeDados.writableDatabase
             // A cláusula ON DELETE CASCADE no seu DatabaseHelper cuidará da tabela RegistroColeta
             db.execSQL("DELETE FROM SessaoColeta")
             Log.i("info_db", "Todos os registros foram deletados.")
             listar() // Atualiza a UI para refletir a exclusão
         } catch (e: Exception) {
             Log.e("info_db", "Erro ao deletar registros.", e)
         }
     }
    */

    fun listar() {
        var idSessao: Int = 0

        // 1. CORREÇÃO: Usar um alias claro (data_formatada) para a coluna de data para evitar ambiguidade.
        val sql = "SELECT id_sessao, nome_separador, modulo_selecionado, total_enderecos, total_itens, " +
                "strftime('%d/%m/%Y - %H:%M', data_sessao, 'localtime') AS data_formatada " +
                "FROM SessaoColeta"

        val cursor = bancoDeDados.readableDatabase.rawQuery(sql, null)

        if (cursor != null && cursor.moveToLast()) {
            idSessao = cursor.getInt(cursor.getColumnIndexOrThrow("id_sessao"))
            binding.idTextView.text = idSessao.toString()
            binding.nomeTextView.text = cursor.getString(cursor.getColumnIndexOrThrow("nome_separador"))
            binding.moduloTextView.text = cursor.getString(cursor.getColumnIndexOrThrow("modulo_selecionado"))
            // Usando o alias corrigido
            binding.dataTextView.text = cursor.getString(cursor.getColumnIndexOrThrow("data_formatada"))
            cursor.close()
        } else {
            Log.w("info_db", "Nenhuma SessaoColeta encontrada. Histórico vazio.")
            // Limpa a UI para não mostrar dados de uma sessão anterior
            binding.idTextView.text = "-"
            binding.nomeTextView.text = "-"
            binding.moduloTextView.text = "-"
            binding.dataTextView.text = "-"
            binding.tempoTotalTextView.text = "00:00:00"
            binding.itensTextView.text = "0"
            binding.enderecosTextView.text = "0"
            binding.itensComSacoFrutaTextView.text = "0"
            binding.itensSemSacoFrutaTextView.text = "0"
            // Adicione aqui a limpeza dos campos de média também se você os tiver no layout
            cursor?.close()
            return
        }

        if (idSessao == 0) return

        // --- Início da Lógica de Somas e Médias ---
        val sql2 = "SELECT * FROM RegistroColeta WHERE id_sessao = $idSessao"
        val cursor2 = bancoDeDados.readableDatabase.rawQuery(sql2, null)

        // 2. OBTER A CONTAGEM TOTAL DE ENDEREÇOS (LINHAS) ANTES DO LOOP
        val totalEnderecos = cursor2.count

        // Variáveis para TOTAIS
        var totalTempoEmSegundos: Long = 0
        var totalItensColetados: Int = 0
        var totalComSaco: Int = 0
        var totalCaixaFechada: Int = 0
        var totalCortes: Int = 0

        // Variáveis para somar o TEMPO de cada categoria
        var tempoCategoriaComSaco: Long = 0
        var tempoCategoriaSemSaco: Long = 0
        var tempoCategoriaCaixaFechada: Long = 0

        // 3. LOOP PARA PROCESSAR CADA REGISTRO (LINHA)
        while (cursor2.moveToNext()) {
            // Converte o tempo da linha atual para segundos
            var segundosDaLinha: Long = 0
            val tempoColetaStr = cursor2.getString(cursor2.getColumnIndexOrThrow("tempo_coleta"))
            if (!tempoColetaStr.isNullOrEmpty() && tempoColetaStr.contains(":")) {
                try {
                    val partes = tempoColetaStr.split(":")
                    segundosDaLinha = (partes[0].toLong() * 60) + partes[1].toLong()
                    totalTempoEmSegundos += segundosDaLinha // Acumula no total geral
                } catch (e: Exception) {
                    Log.e("info_db_soma", "Erro ao converter tempo: '$tempoColetaStr'", e)
                }
            }

            // Lê as flags e valores da linha
            val produtoEmbalado = cursor2.getInt(cursor2.getColumnIndexOrThrow("produto_embalado")) == 1
            val caixaFechada = cursor2.getInt(cursor2.getColumnIndexOrThrow("caixa_fechada")) == 1
            val corteNoEndereco = cursor2.getInt(cursor2.getColumnIndexOrThrow("corte_no_endereco")) == 1

            // Acumula os totais e os tempos por categoria
            totalItensColetados += cursor2.getInt(cursor2.getColumnIndexOrThrow("qtd_itens_coletados"))
            if (corteNoEndereco) totalCortes++

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
        // 4. FECHE O CURSOR AQUI, APÓS O FIM DO LOOP
        cursor2.close()

        // 5. FAÇA TODOS OS CÁLCULOS DEPOIS DE PROCESSAR OS DADOS
        val totalSemSaco = totalEnderecos - totalComSaco

        // Cálculo das médias, com proteção contra divisão por zero
        val mediaGeral = if (totalEnderecos > 0) totalTempoEmSegundos / totalEnderecos else 0
        val mediaComSaco = if (totalComSaco > 0) tempoCategoriaComSaco / totalComSaco else 0
        val mediaSemSaco = if (totalSemSaco > 0) tempoCategoriaSemSaco / totalSemSaco else 0
        val mediaCaixaFechada = if (totalCaixaFechada > 0) tempoCategoriaCaixaFechada / totalCaixaFechada else 0

        // Formatação dos resultados para exibição
        val tempoTotalFormatado = formatarSegundos(totalTempoEmSegundos)
        val mediaGeralFormatada = formatarSegundos(mediaGeral)
        val mediaComSacoFormatada = formatarSegundos(mediaComSaco)
        val mediaSemSacoFormatada = formatarSegundos(mediaSemSaco)
        val mediaCaixaFechadaFormatada = formatarSegundos(mediaCaixaFechada)

        // Logs de depuração
        Log.d("info_db_media", "Média Geral: $mediaGeralFormatada")
        Log.d("info_db_media", "Média Com Saco: $mediaComSacoFormatada")
        Log.d("info_db_media", "Média Sem Saco: $mediaSemSacoFormatada")
        Log.d("info_db_media", "Média Caixa Fechada: $mediaCaixaFechadaFormatada")

        // 6. ATRIBUA TODOS OS VALORES À UI DE UMA SÓ VEZ
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

    private fun formatarSegundos(segundosTotais: Long): String {
        // Correção de consistência: retorna sempre no formato HH:MM:SS
        if (segundosTotais <= 0) return "00:00:00"
        val horas = TimeUnit.SECONDS.toHours(segundosTotais)
        val minutos = TimeUnit.SECONDS.toMinutes(segundosTotais) % 60
        val segundos = segundosTotais % 60
        return String.format("%02d:%02d:%02d", horas, minutos, segundos)
    }
}
