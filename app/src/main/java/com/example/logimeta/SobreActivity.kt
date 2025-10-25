package com.example.logimeta

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.logimeta.databinding.ActivitySobreBinding

class SobreActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySobreBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.constraintLayout.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // O 'finish()' fecha a tela atual (SobreActivity) e volta para a anterior (MainActivity).
        binding.voltarSobreButton.setOnClickListener {
            finish()
        }
    }
}
