package com.example.logimeta.model

data class ResultadosCalculados(
    val nomeModulo: String,
    val mediaGeralSegundos: Long,
    val mediaComEmbalagemSegundos: Long,
    val mediaSemEmbalagemSegundos: Long,
    val mediaCaixaFechadaSegundos: Long,
    val mediaItensPorEndereco: Double,
    val previsaoEm1h: Int,
    val previsaoEm7h20m: Int
)