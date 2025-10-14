package com.example.logimeta

import com.example.logimeta.model.Coleta

object ColetaRepository {
    val historico = mutableListOf<Coleta>()

    fun adicionarColeta(coleta: Coleta) {
        historico.add(coleta)
    }

    fun limpar() {
        historico.clear()
    }
}