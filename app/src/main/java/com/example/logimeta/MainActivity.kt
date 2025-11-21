package com.example.logimeta

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
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

    lateinit var logo: ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.historicoButton.setOnClickListener {
            val intent = Intent(this, HistoricoDeTestesActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.mediaGeralButton.setOnClickListener {
            val intent = Intent(this, EscolherModuloActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnNovoTeste.setOnClickListener {
            val intent = Intent(this, PreencherDadosActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSobre.setOnClickListener {
            val intent = Intent(this, SobreActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.termosDeLicencaTextView.setOnClickListener {
            val intent = Intent(this, LicenseActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.imageView3.alpha = 0f
        binding.imageView3.animate().setDuration(1700).alpha(1f).start()


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}