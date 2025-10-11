package com.example.logimeta.database

import com.example.logimeta.Coleta

interface IColetaDAO {

    fun salvar(coleta: Coleta): Boolean
    fun atualizar(coleta: Coleta): Boolean
    fun excluir(id: Int): Boolean
    fun listar(): List<Coleta>
    //fun buscarPorId(id: Long): Coleta?

}