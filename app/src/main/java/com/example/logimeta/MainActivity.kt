package com.example.logimeta

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.logimeta.databinding.ActivityMainBinding
import kotlin.getValue

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.historicoButton.setOnClickListener {
            val intent = Intent(this, HistoricoDeTestesActivity::class.java)
            startActivity(intent)
        }

        binding.mediaGeralButton.setOnClickListener {
            val intent = Intent(this, EscolherModuloActivity::class.java)
            startActivity(intent)
        }

        binding.btnNovoTeste.setOnClickListener {
            val intent = Intent(this, PreencherDadosActivity::class.java)
            startActivity(intent)
        }

        binding.btnSobre.setOnClickListener {
            val intent = Intent(this, SobreActivity::class.java)
            startActivity(intent)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}