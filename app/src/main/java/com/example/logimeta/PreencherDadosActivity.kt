package com.example.logimeta

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PreencherDadosActivity : AppCompatActivity() {

    lateinit var voltar_imageView: ImageView
    lateinit var iniciar_button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_preencher_dados)

        voltar_imageView = findViewById(R.id.voltar_imageView)
        iniciar_button = findViewById(R.id.iniciar_button)

        iniciar_button.setOnClickListener {
            val intent = Intent(this, ColetaDeDadosActivity::class.java)
            startActivity(intent)
        }

        voltar_imageView.setOnClickListener {
            finish()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}