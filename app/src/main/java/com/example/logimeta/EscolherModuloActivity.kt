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
import com.example.logimeta.databinding.ActivityEscolherModuloBinding

class EscolherModuloActivity : AppCompatActivity() {

    // Variável para guardar o módulo que o usuário seleciona.
    private var moduloSelecionado: String? = null

    private val binding by lazy {
        ActivityEscolherModuloBinding.inflate(layoutInflater)
    }

    // Chave para passar o dado na Intent
    companion object {
        const val MODULO_SELECIONADO_EXTRA = "MODULO_SELECIONADO"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Lista de módulos para o AutoCompleteTextView
        val modulos = arrayOf(
            "Plantas alto giro", "Plantas baixo giro", "Natalino alto giro",
            "Natalinos baixo giro", "Arvores alto giro", "Arvores baixo giro",
            "Vasos rua 23", "Vasos alto giro rua 9 até 11", "Vasos baixo giro rua 9 até 11",
            "Rua 8 Saldo base alto giro", "Rua 8 Saldo base baixo giro", "Rua 6 e 7 alto giro",
            "Rua 6 e 7 baixo giro", "Mezanino 1", "Mezanino 2", "Mezanino Pulmão",
            "Pulmão Rua 61 até 05", "Pulmão rua 09 até 16", "Pulmão Natalinos",
            "Produtos Pesados", "Indefinido"
        )

        // Configura o adapter para a lista de módulos usando o binding
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, modulos)
        binding.moduloSelecionadoAutoCompleteTextView.setAdapter(adapter)

        // Define o fundo do dropdown
        binding.moduloSelecionadoAutoCompleteTextView.setDropDownBackgroundDrawable(ColorDrawable(Color.parseColor("#622229")))

        // Listener para quando um item é clicado na lista
        binding.moduloSelecionadoAutoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            // Salva o módulo selecionado na variável
            moduloSelecionado = parent.getItemAtPosition(position).toString()
            Toast.makeText(this, "Módulo selecionado: $moduloSelecionado", Toast.LENGTH_SHORT).show()
        }

        // Listener para o botão de avançar (lógica unificada)
        binding.avancarEscolherModuloButton.setOnClickListener {
            // Verifica se o usuário realmente selecionou um módulo
            if (moduloSelecionado != null) {
                // Se selecionou, cria a Intent para a próxima tela
                val intent = Intent(this, MediaGeralActivity::class.java)

                // Adiciona o módulo selecionado como um "extra" na Intent
                intent.putExtra(MODULO_SELECIONADO_EXTRA, moduloSelecionado)

                // Inicia a próxima atividade
                startActivity(intent)
            } else {
                // Se não selecionou, avisa o usuário
                Toast.makeText(this, "Por favor, selecione um módulo.", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener para o botão de voltar
        binding.voltarEscolherModuloButton.setOnClickListener {
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
