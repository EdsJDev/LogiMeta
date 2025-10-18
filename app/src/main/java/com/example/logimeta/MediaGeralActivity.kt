package com.example.logimeta

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.logimeta.database.DatabaseHelper
import com.example.logimeta.databinding.ActivityMediaGeralBinding

class MediaGeralActivity : AppCompatActivity() {



    private val binding by lazy {
        ActivityMediaGeralBinding.inflate(layoutInflater)
    }
    private val bancoDeDados by lazy {
        DatabaseHelper(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_media_geral)



        binding.mediaGeralVoltarButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fun listarDados(){
            val sql = """SELECT
            rc.tempo_coleta,
            rc.qtd_itens_coletados,
            rc.produto_embalado,
            rc.caixa_fechada
        FROM
            RegistroColeta rc
        JOIN
            SessaoColeta sc ON rc.id_sessao = sc.id_sessao
        WHERE
            sc.modulo_selecionado = 'Vasos rua 23'
    """.trimIndent()

            val cursor = bancoDeDados.readableDatabase.rawQuery(sql, null)
            cursor.moveToFirst()

            val tempoColeta = cursor.getString(cursor.getColumnIndexOrThrow("tempo_coleta"))
            val qtdItensColetados = cursor.getInt(cursor.getColumnIndexOrThrow("qtd_itens_coletados"))
            val produtoEmbalado = cursor.getInt(cursor.getColumnIndexOrThrow("produto_embalado"))
            val caixaFechada = cursor.getInt(cursor.getColumnIndexOrThrow("caixa_fechada"))

            cursor.close()







        }

    }
}