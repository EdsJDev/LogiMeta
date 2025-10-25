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

/**
 * Activity responsável por permitir que o usuário selecione um módulo específico
 * para visualizar as estatísticas de média geral.
 */
class EscolherModuloActivity : AppCompatActivity() {
    // Armazena a escolha do usuário para ser usada ao avançar para a próxima tela.
    private var moduloSelecionado: String? = null

    private val binding by lazy {
        ActivityEscolherModuloBinding.inflate(layoutInflater)
    }

    // O companion object agrupa constantes relacionadas à classe.
    companion object {
        /**
         * Chave pública usada para passar o nome do módulo via Intent,
         * garantindo consistência na comunicação entre Activities.
         */
        const val MODULO_SELECIONADO_EXTRA = "MODULO_SELECIONADO"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Define a lista de módulos que será exibida no menu suspenso.
        val modulos = arrayOf(
            "Plantas alto giro", "Plantas baixo giro", "Natalino alto giro",
            "Natalinos baixo giro", "Arvores alto giro", "Arvores baixo giro",
            "Vasos rua 23", "Vasos alto giro rua 9 até 11", "Vasos baixo giro rua 9 até 11",
            "Rua 8 Saldo base alto giro", "Rua 8 Saldo base baixo giro", "Rua 6 e 7 alto giro",
            "Rua 6 e 7 baixo giro", "Mezanino 1", "Mezanino 2", "Mezanino Pulmão",
            "Pulmão Rua 61 até 05", "Pulmão rua 09 até 16", "Pulmão Natalinos",
            "Produtos Pesados", "Indefinido"
        )

        // Associa a lista de módulos ao componente AutoCompleteTextView através de um ArrayAdapter.
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, modulos)
        binding.moduloSelecionadoAutoCompleteTextView.setAdapter(adapter)

        // Personaliza a cor de fundo do menu suspenso para manter a identidade visual do app.
        binding.moduloSelecionadoAutoCompleteTextView.setDropDownBackgroundDrawable(ColorDrawable(Color.parseColor("#622229")))

        // Define a ação a ser executada quando o usuário seleciona um item da lista.
        binding.moduloSelecionadoAutoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            // Armazena a string do item selecionado na variável da classe.
            moduloSelecionado = parent.getItemAtPosition(position).toString()
        }

        // Define a ação do botão "Avançar".
        binding.avancarEscolherModuloButton.setOnClickListener {
            // Garante que o usuário fez uma seleção antes de prosseguir.
            if (moduloSelecionado != null) {
                // Prepara a Intent para abrir a tela de visualização de médias.
                val intent = Intent(this, MediaGeralActivity::class.java)

                // Anexa o módulo selecionado à Intent para que a próxima tela possa recebê-lo.
                intent.putExtra(MODULO_SELECIONADO_EXTRA, moduloSelecionado)

                // Inicia a próxima atividade.
                startActivity(intent)
            } else {
                // Fornece um feedback ao usuário caso ele tente avançar sem fazer uma seleção.
                Toast.makeText(this, "Por favor, selecione um módulo.", Toast.LENGTH_SHORT).show()
            }
        }

        // Define a ação do botão "Voltar".
        binding.voltarEscolherModuloButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Ajusta o padding da tela para acomodar as barras do sistema (Edge-to-Edge).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
