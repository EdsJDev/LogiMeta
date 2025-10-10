package com.example.logimeta

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.logimeta.database.DatabaseHelper

class MainActivity : AppCompatActivity() {

    lateinit var btn_NovoTeste: Button
    lateinit var historico_button: Button
    lateinit var media_geral_button: Button

    private val bancoDeDados by lazy {
        DatabaseHelper(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // teste para verificar se o banco de dados está funcionando
        try {
            bancoDeDados.writableDatabase.execSQL(
                "INSERT INTO SessaoColeta (" +
                        "    nome_separador," +
                        "    modulo_selecionado," +
                        "    total_enderecos," +
                        "    total_itens" +
                        ")" +
                        "VALUES (" +
                        "    'Alice Silva'," +
                        "    'Mod A - Picking Lento'," +
                        "    25," +
                        "    150" +
                        ");"
            )
        }catch (e: Exception){
            e.printStackTrace()
        }
        // fim do teste



        btn_NovoTeste = findViewById(R.id.btn_NovoTeste)
        historico_button = findViewById(R.id.historico_button)
        media_geral_button = findViewById(R.id.media_geral_button)



        historico_button.setOnClickListener {
            val intent = Intent(this, HistoricoDeTestesActivity::class.java)
            startActivity(intent)
        }

        media_geral_button.setOnClickListener {
            val intent = Intent(this, EscolherModuloActivity::class.java)
            startActivity(intent)
        }

        btn_NovoTeste.setOnClickListener {
            val intent = Intent(this, PreencherDadosActivity::class.java)
            startActivity(intent)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}