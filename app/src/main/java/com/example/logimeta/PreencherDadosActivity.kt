package com.example.logimeta

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.logimeta.databinding.ActivityPreencherDadosBinding

class PreencherDadosActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityPreencherDadosBinding.inflate(layoutInflater)
    }
    private var moduloSelecionado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupViews()
        setupClickListeners()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Função para configurar as views, como o adapter do AutoCompleteTextView.
     */
    private fun setupViews() {
        val modulos = arrayOf(
            "Plantas alto giro", "Plantas baixo giro", "Natalino alto giro",
            "Natalinos baixo giro", "Arvores alto giro", "Arvores baixo giro",
            "Vasos rua 23", "Vasos alto giro rua 9 até 11", "Vasos baixo giro rua 9 até 11",
            "Rua 8 Saldo base alto giro", "Rua 8 Saldo base baixo giro", "Rua 6 e 7 alto giro",
            "Rua 6 e 7 baixo giro", "Mezanino 1", "Mezanino 2", "Mezanino Pulmão",
            "Pulmão Rua 61 até 05", "Pulmão rua 09 até 16", "Pulmão Natalinos",
            "Produtos Pesados", "Indefinido"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, modulos)

        // Configura o adapter usando o binding.
        binding.moduloSelecionadoAutoCompleteTextView.setAdapter(adapter)

        // Define a cor de fundo do dropdown.
        val corDropdown = ColorDrawable(Color.parseColor("#622229"))
        binding.moduloSelecionadoAutoCompleteTextView.setDropDownBackgroundDrawable(corDropdown)
    }


    private fun setupClickListeners() {
        binding.moduloSelecionadoAutoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            moduloSelecionado = parent.getItemAtPosition(position).toString()
        }

        binding.iniciarButton.setOnClickListener {
            iniciarColeta()
        }

        binding.voltarImageView.setOnClickListener {
           finish()
        }
    }

    /**
     * Valida os campos e, se tudo estiver correto, inicia a próxima atividade.
     */
    private fun iniciarColeta() {
        val nomeSeparador = binding.nomeSeparadorEditText.text.toString()
        val totalEnderecos = binding.totalEnderecosEditText.text.toString()
        val totalItens = binding.totalItensEditText.text.toString()

        // Validação robusta de todos os campos obrigatórios.
        when {
            moduloSelecionado == null -> {
                Toast.makeText(this, "Por favor, selecione um módulo.", Toast.LENGTH_SHORT).show()
                binding.moduloSelecionadoAutoCompleteTextView.requestFocus() // Foca no campo que precisa de atenção.
            }
            nomeSeparador.isBlank() -> {
                Toast.makeText(this, "Por favor, preencha o nome do separador.", Toast.LENGTH_SHORT).show()
                binding.nomeSeparadorEditText.requestFocus()
            }
            totalEnderecos.isBlank() -> {
                Toast.makeText(this, "Por favor, informe o total de endereços.", Toast.LENGTH_SHORT).show()
                binding.totalEnderecosEditText.requestFocus()
            }
            totalItens.isBlank() -> {
                Toast.makeText(this, "Por favor, informe o total de itens.", Toast.LENGTH_SHORT).show()
                binding.totalItensEditText.requestFocus()
            }
            else -> {
                // Se todas as validações passarem, prepara e inicia a próxima Intent.
                val intent = Intent(this, ColetaDeDadosActivity::class.java)
                intent.putExtra("MODULO_SELECIONADO", moduloSelecionado)
                intent.putExtra("TOTAL_ENDERECOS", totalEnderecos)
                intent.putExtra("TOTAL_ITENS", totalItens)
                intent.putExtra("NOME_SEPARADOR", nomeSeparador)
                startActivity(intent)
                finish()
            }
        }
    }
}
