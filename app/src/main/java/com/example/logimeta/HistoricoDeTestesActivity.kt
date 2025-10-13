package com.example.logimeta

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class HistoricoDeTestesActivity : AppCompatActivity() {


    lateinit var historico_voltar_button: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historico_de_testes)

        historico_voltar_button = findViewById(R.id.historico_voltar_button)

        val historicoTextView = findViewById<TextView>(R.id.textView34)
        val builder = StringBuilder()

        for ((index, coleta) in ColetaRepository.historico.withIndex()) {
            builder.append("Teste ${index + 1}\n")
            builder.append("Nome: ${coleta.nomeSeparador}\n")
            builder.append("Módulo: ${coleta.modulo}\n")
            builder.append("Endereços: ${coleta.enderecos}\n")
            builder.append("Itens: ${coleta.itens}\n")
            builder.append("Itens não embalados: ${coleta.itensNaoEmbalados}\n")
            builder.append("Tempo c/ embalagem: ${coleta.tempoMedioComEmbalagem}\n")
            builder.append("Tempo s/ embalagem: ${coleta.tempoMedioSemEmbalagem}\n")
            builder.append("----------------------\n")
        }

        historicoTextView.text = builder.toString()

        historico_voltar_button.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}