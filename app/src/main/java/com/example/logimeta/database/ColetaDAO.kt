package com.example.logimeta.database

import android.content.Context
import com.example.logimeta.Coleta

class ColetaDAO(context : Context) : IColetaDAO {

    val escrita = DatabaseHelper(context).writableDatabase
    val leitura = DatabaseHelper(context).readableDatabase


    override fun salvar(coleta: Coleta): Boolean {
        TODO("Not yet implemented")
    }

    override fun atualizar(coleta: Coleta): Boolean {
        TODO("Not yet implemented")
    }

    override fun excluir(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun listar(): List<Coleta> {
        TODO("Not yet implemented")
    }


}