package com.example.logimeta

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast

class EscolherModuloActivity : AppCompatActivity() {

    private lateinit var autoCompleteTextViewModulo: AutoCompleteTextView
    private var moduloSelecionado: String? = null

    lateinit var avancar_escolher_modulo_button: Button
    lateinit var voltar_escolher_modulo_button: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_escolher_modulo)

        this@EscolherModuloActivity.avancar_escolher_modulo_button = findViewById(R.id.avancar_escolher_modulo_button)
        voltar_escolher_modulo_button = findViewById(R.id.voltar_escolher_modulo_button)
        autoCompleteTextViewModulo = findViewById(R.id.moduloSelecionadoAutoCompleteTextView)

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

        autoCompleteTextViewModulo.setAdapter(adapter)

        val avancar_escolher_modulo_button = findViewById<Button>(R.id.avancar_escolher_modulo_button)
        avancar_escolher_modulo_button.setOnClickListener {
            if (moduloSelecionado != null) {
                Toast.makeText(this, "Avançar para $moduloSelecionado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, selecione um módulo.", Toast.LENGTH_SHORT).show()
            }
        }

        autoCompleteTextViewModulo.setOnItemClickListener { parent, view, position, id ->
            moduloSelecionado = parent.getItemAtPosition(position).toString()
            Toast.makeText(this, "Módulo selecionado: $moduloSelecionado", Toast.LENGTH_SHORT).show()
        }

        autoCompleteTextViewModulo.setDropDownBackgroundDrawable(ColorDrawable(Color.parseColor("#622229")))

        this@EscolherModuloActivity.avancar_escolher_modulo_button.setOnClickListener {
            val intent = Intent(this, MediaGeralActivity::class.java)
            startActivity(intent)
        }

        voltar_escolher_modulo_button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}