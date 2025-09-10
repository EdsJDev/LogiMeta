package com.example.logimeta

object ColetaRepository {
    val historico = mutableListOf<Coleta>()

    fun adicionarColeta(coleta: Coleta) {
        historico.add(coleta)
    }

    fun limpar() {
        historico.clear()
    }
}