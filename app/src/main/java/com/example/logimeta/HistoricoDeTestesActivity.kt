package com.example.logimeta

import android.content.Intent
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

    fun listar (){
        val sql =  "SELECT * FROM SessaoColeta"
        val cursor = bancoDeDados.readableDatabase.rawQuery(sql, null)
        cursor.moveToLast()
        val idSessao = cursor.getInt(cursor.getColumnIndexOrThrow("id_sessao"))
        val nomeSeparador = cursor.getString(cursor.getColumnIndexOrThrow("nome_separador"))
        val moduloSelecionado = cursor.getString(cursor.getColumnIndexOrThrow("modulo_selecionado"))
        val totalEnderecos = cursor.getInt(cursor.getColumnIndexOrThrow("total_enderecos"))
        val totalItens = cursor.getInt(cursor.getColumnIndexOrThrow("total_itens"))
        cursor.close()
        Log.i("info_db", "RETORNO DO BANCO DE DADOS ID da sessão: $idSessao")
        Log.i("info_db", "RETORNO DO BANCO DE DADOS Nome do separador: $nomeSeparador")
        Log.i("info_db", "RETORNO DO BANCO DE DADOS Módulo selecionado: $moduloSelecionado")
        Log.i("info_db", "RETORNO DO BANCO DE DADOS Total de endereços: $totalEnderecos")
        Log.i("info_db", "RETORNO DO BANCO DE DADOS Total de itens: $totalItens")
    }
}