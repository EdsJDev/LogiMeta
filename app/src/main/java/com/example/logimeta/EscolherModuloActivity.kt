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
            "Plantas Alto Giro", "Plantas Baixo Giro", "Natalino Alto Giro",
            "Natalinos Baixo Giro", "Arvores Alto Giro", "Arvores Baixo Giro",
            "Vasos Rua 23", "Vasos Alto Giro Rua 9 Até 11", "Vasos Baixo Giro Rua 9 Até 11",
            "Rua 8 Saldo Base Alto Giro", "Rua 8 Saldo Base Baixo Giro", "Rua 6 e 7 Alto Giro",
            "Rua 6 e 7 Baixo Giro", "Mezanino 1", "Mezanino 2", "Mezanino Pulmão",
            "Pulmão Rua 61 Até 05", "Pulmão Rua 09 Até 16", "Pulmão Natalinos",
            "Produtos Pesados", "Coleta 1", "Coleta 2", "Coleta 3","Coleta 4",
            "Coleta 5", "Coleta 6", "Coleta 7", "Coleta 8", "Coleta 9", "Coleta 10"
        )

        // Associa a lista de módulos ao componente AutoCompleteTextView através de um ArrayAdapter.
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, modulos)
        binding.moduloSelecionadoAutoCompleteTextView.setAdapter(adapter)

        // Personaliza a cor de fundo do menu suspenso para manter a identidade visual do app.
        val corDeFundoDoTema = ContextCompat.getColor(this, R.color.card_internal_background_color)
        val corDropdown = ColorDrawable(corDeFundoDoTema)
        binding.moduloSelecionadoAutoCompleteTextView.setDropDownBackgroundDrawable(corDropdown)

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
