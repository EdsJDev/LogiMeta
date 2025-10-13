package com.example.logimeta.database

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.example.logimeta.model.RegistroColeta
import com.example.logimeta.model.SessaoColeta

class ColetaDAO(context: Context){
    private val dbHelper = DatabaseHelper(context)
    private val escrita = dbHelper.writableDatabase
    private val leitura = dbHelper.readableDatabase

    /**
     * Salva uma sessão de coleta completa, incluindo a sessão principal
     * e todos os seus registros de coleta associados, usando uma transação.
     * @return Boolean Retorna 'true' se a operação for bem-sucedida, 'false' caso contrário.
     */
    fun salvarColetaCompleta(sessao: SessaoColeta, registros: List<RegistroColeta>): Boolean {
        escrita.beginTransaction()
        try {
            val sessaoValues = ContentValues().apply {
                put("nome_separador", sessao.nomeSeparador)
                put("modulo_selecionado", sessao.moduloSelecionado)
                put("total_enderecos", sessao.totalEnderecos)
                put("total_itens", sessao.totalItens)
            }
            val idNovaSessao = escrita.insertOrThrow("SessaoColeta", null, sessaoValues)
            Log.i("info_db_dao", "DAO: SessaoColeta salva com o ID: $idNovaSessao")

            registros.forEach { registro ->
                val registroValues = ContentValues().apply {
                    put("id_sessao", idNovaSessao)
                    put("tempo_coleta", registro.tempoColeta)
                    put("rua_endereco", registro.ruaEndereco)
                    put("qtd_itens_coletados", registro.qtdItensColetados)
                    put("produto_embalado", registro.produtoFoiEmbalado)
                    put("corte_no_endereco", registro.corteNoEndereco)
                    put("caixa_fechada", registro.caixaFechada)
                }
                escrita.insertOrThrow("RegistroColeta", null, registroValues)
            }

            escrita.setTransactionSuccessful()
            Log.i("info_db_dao", "DAO: Transação concluída com sucesso.")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("info_db_dao", "DAO: Erro durante a transação. Revertendo. Erro: ${e.message}")
            return false
        } finally {
            // Finaliza a transação. Se setTransactionSuccessful() foi chamado, as mudanças são salvas (commit).
            // Caso contrário, são descartadas (rollback).
            escrita.endTransaction()
        }
    }

    // override fun salvar(coleta: Coleta): Boolean { ... }
    // override fun atualizar(coleta: Coleta): Boolean { ... }
    // override fun excluir(id: Int): Boolean { ... }
    // override fun listar(): List<Coleta> { ... }
}

