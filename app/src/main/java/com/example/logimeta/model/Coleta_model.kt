package com.example.logimeta.model

data class Coleta_model(
    val moduloSelecionado: String,
    val totalEnderecos: Int,
    val totalItens: Int,
    val nomeSeparador: String,
    val tempoColeta: String,
    val ruaEndereco: String,
    val qtdItensColetados: String,
    val produtoFoiEmbalado: Boolean,
    val corteNoEndereco: Boolean,
    val caixaFechada: Boolean
)