package com.example.logimeta

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EscolherModuloActivity : AppCompatActivity() {

    lateinit var avancar_escolher_modulo_button: Button
    lateinit var voltar_escolher_modulo_button: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_escolher_modulo)

        avancar_escolher_modulo_button = findViewById(R.id.avancar_escolher_modulo_button)
        voltar_escolher_modulo_button = findViewById(R.id.voltar_escolher_modulo_button)

        avancar_escolher_modulo_button.setOnClickListener {
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