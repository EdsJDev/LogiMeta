package com.example.logimeta

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ColetaDeDadosActivity : AppCompatActivity() {

    lateinit var voltar2_imageView: ImageView
    lateinit var finalizaButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_coleta_de_dados)

        voltar2_imageView = findViewById(R.id.voltar2_imageView)

        finalizaButton = findViewById(R.id.finalizar_button)

        finalizaButton.setOnClickListener {
            val intent = Intent(this, SaveScreenActivity::class.java)
            startActivity(intent)
        }




        voltar2_imageView.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}