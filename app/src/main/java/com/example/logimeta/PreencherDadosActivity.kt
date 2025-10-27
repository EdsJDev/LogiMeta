package com.example.logimeta

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
            "Plantas Alto Giro", "Plantas Baixo Giro", "Natalino Alto Giro",
            "Natalinos Baixo Giro", "Arvores Alto Giro", "Arvores Baixo Giro",
            "Vasos Rua 23", "Vasos Alto Giro Rua 9 Até 11", "Vasos Baixo Giro Rua 9 Até 11",
            "Rua 8 Saldo Base Alto Giro", "Rua 8 Saldo Base Baixo Giro", "Rua 6 e 7 Alto Giro",
            "Rua 6 e 7 Baixo Giro", "Mezanino 1", "Mezanino 2", "Mezanino Pulmão",
            "Pulmão Rua 61 Até 05", "Pulmão Rua 09 Até 16", "Pulmão Natalinos",
            "Produtos Pesados", "Coleta 1", "Coleta 2", "Coleta 3","Coleta 4",
            "Coleta 5", "Coleta 6", "Coleta 7", "Coleta 8", "Coleta 9", "Coleta 10"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, modulos)

        // Configura o adapter usando o binding.
        binding.moduloSelecionadoAutoCompleteTextView.setAdapter(adapter)

        // Busca a cor diretamente do arquivo colors.xml
        val corDeFundoDoTema = ContextCompat.getColor(this, R.color.card_internal_background_color)
        val corDropdown = ColorDrawable(corDeFundoDoTema)
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
