package com.example.logimeta.model

data class Coleta(
    val nomeSeparador: String,
    val modulo: String,
    val enderecos: Int,
    val itens: Int,
    val tempoMedioComEmbalagem: String,
    val tempoMedioSemEmbalagem: String,
    //val tempoMedioCaixaFechada: String,
    val itensNaoEmbalados: Int
)