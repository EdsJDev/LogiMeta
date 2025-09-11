package com.example.logimeta

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PreencherDadosActivity : AppCompatActivity() {

    private lateinit var moduloAutoCompleteTextView: AutoCompleteTextView
    private var moduloSelecionado: String? = null
    private lateinit var voltarImageView: ImageView
    private lateinit var iniciarButton: Button

    private lateinit var totalEnderecosEditText: EditText

    private lateinit var totalItensEditText: EditText

    private lateinit var nomeSeparadorEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_preencher_dados)

        voltarImageView = findViewById(R.id.voltar_imageView)
        iniciarButton = findViewById(R.id.iniciar_button)
        moduloAutoCompleteTextView = findViewById(R.id.moduloSelecionado_AutoCompleteTextView)
        totalEnderecosEditText = findViewById(R.id.totalEnderecos_editText)
        totalItensEditText = findViewById(R.id.totalItens_editText)
        nomeSeparadorEditText = findViewById(R.id.nomeSeparador_editText)



        val modulos = arrayOf(
            "Plantas alto giro",
            "Plantas baixo giro",
            "Natalino alto giro",
            "Natalinos baixo giro",
            "Arvores alto giro",
            "Arvores baixo giro",
            "Vasos rua 23",
            "Vasos alto giro rua 9 até 11",
            "Vasos baixo giro rua 9 até 11",
            "Rua 8 Saldo base alto giro",
            "Rua 8 Saldo base baixo giro",
            "Rua 6 e 7 alto giro",
            "Rua 6 e 7 baixo giro",
            "Mezanino 1",
            "Mezanino 2",
            "Mezanino Pulmão",
            "Pulmão Rua 61 até 05",
            "Pulmão rua 09 até 16",
            "Pulmão Natalinos",
            "Produtos Pesados",
            "Indefinido"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, modulos)
        moduloAutoCompleteTextView.setAdapter(adapter)

        moduloAutoCompleteTextView.setOnClickListener {
            moduloAutoCompleteTextView.showDropDown()
        }

        moduloAutoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            moduloSelecionado = parent.getItemAtPosition(position).toString()
        }

        iniciarButton.setOnClickListener {
            if (moduloSelecionado != null) {
                val intent = Intent(this, ColetaDeDadosActivity::class.java)
                intent.putExtra("MODULO_SELECIONADO", moduloSelecionado)
                intent.putExtra("TOTAL_ENDERECOS", totalEnderecosEditText.text.toString())
                intent.putExtra("TOTAL_ITENS", totalItensEditText.text.toString())
                intent.putExtra("NOME_SEPARADOR", nomeSeparadorEditText.text.toString())


                startActivity(intent)


            } else {
                Toast.makeText(this, "Por favor, selecione um módulo.", Toast.LENGTH_SHORT).show()
            }
        }

        voltarImageView.setOnClickListener {
            finish()
        }

        val corDropdown = ColorDrawable(Color.parseColor("#622229"))
        @Suppress("DEPRECATION")
        moduloAutoCompleteTextView.setDropDownBackgroundDrawable(corDropdown)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}


