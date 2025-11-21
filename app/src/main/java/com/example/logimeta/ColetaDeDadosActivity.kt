package com.example.logimeta

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.logimeta.model.RegistroColeta
import com.google.android.material.textfield.TextInputEditText
import java.util.ArrayList

class ColetaDeDadosActivity : AppCompatActivity() {

    // Views da interface
    private lateinit var voltar2_imageView: ImageView
    private lateinit var finalizaButton: Button
    private lateinit var proximoButton: Button
    private lateinit var contadorTextView: TextView
    private lateinit var rua_referente_ao_endereco_TextInputEditText: TextInputEditText
    private lateinit var quantidade_de_itens_coletados_TextInputEditText: TextInputEditText
    private lateinit var corte_no_endereco_sim_button: Button
    private lateinit var corte_no_endereco_nao_button: Button
    private lateinit var embalagem_nao_button: Button
    private lateinit var embalado_sim_button: Button
    private lateinit var caixa_fechada_sim_button: Button
    private lateinit var caixa_fechada_nao_button: Button

    // Armazenamento de dados da sessão
    private val listaDeDados = mutableListOf<RegistroColeta>()

    // Controle de estado dos botões de seleção
    private var produtoFoiEmbalado: Boolean? = null
    private var corteNoEndereco: Boolean? = null
    private var caixaFechada: Boolean? = null
    //private var corOriginalBotao: Int = 0

    // Controle do cronômetro
    private var segundos: Int = 0
    private var rodando: Boolean = true
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_coleta_de_dados)

        // Inicializa as Views
        inicializarViews()

        // Pega os dados passados da activity anterior
        val bundle = intent.extras
        val moduloSelecionado = bundle?.getString("MODULO_SELECIONADO")
        val totalEnderecos = bundle?.getString("TOTAL_ENDERECOS")
        val totalItens = bundle?.getString("TOTAL_ITENS")
        val nomeSeparador = bundle?.getString("NOME_SEPARADOR")

        configurarListenersBotoes()
        resetarSelecaoBotoesVisualmente()
        iniciarCronometro()

        proximoButton.setOnClickListener {
            processarProximoEndereco(moduloSelecionado, totalEnderecos, totalItens, nomeSeparador)
        }

        finalizaButton.setOnClickListener {
            if (listaDeDados.isNotEmpty()) {
                val intent = Intent(this, SaveScreenActivity::class.java).apply {
                    putExtra("MODULO_SELECIONADO", moduloSelecionado)
                    putExtra("TOTAL_ENDERECOS", totalEnderecos)
                    putExtra("TOTAL_ITENS", totalItens)
                    putExtra("NOME_SEPARADOR", nomeSeparador)
                    putExtra("lista", ArrayList(listaDeDados))
                }
                startActivity(intent)
                listaDeDados.clear()
                finish()
            } else {
                Toast.makeText(this, "Nenhuma coleta para finalizar. Adicione uma coleta ou cancele.", Toast.LENGTH_LONG).show()
            }
        }

        voltar2_imageView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun processarProximoEndereco(
        moduloSelecionado: String?,
        totalEnderecos: String?,
        totalItens: String?,
        nomeSeparador: String?
    ) {
        rodando = false
        val tempoDaColeta = segundos

        val rua = rua_referente_ao_endereco_TextInputEditText.text.toString().trim()
        val qtdItens = quantidade_de_itens_coletados_TextInputEditText.text.toString().trim()

        // --- Validação completa ---
        when {
            rua.isBlank() -> {
                mostrarErro("Preencha o campo 'Rua referente ao endereço'."); return
            }
            qtdItens.isBlank() -> {
                mostrarErro("Preencha o campo 'Quantidade de Itens'."); return
            }
            produtoFoiEmbalado == null -> {
                mostrarErro("Selecione se o produto foi embalado (Sim/Não)."); return
            }
            corteNoEndereco == null -> {
                mostrarErro("Selecione se houve corte no endereço (Sim/Não)."); return
            }
            caixaFechada == null -> {
                mostrarErro("Selecione se a caixa foi fechada (Sim/Não)."); return
            }
        }

        // --- Tudo preenchido, salva o registro ---
        val horas = tempoDaColeta / 3600
        val minutos = (tempoDaColeta % 3600) / 60
        val segs = tempoDaColeta % 60
        val tempoFormatado = String.format("%02d:%02d:%02d", horas, minutos, segs)

        val registro = RegistroColeta(
            tempoColeta = tempoFormatado,
            ruaEndereco = rua,
            qtdItensColetados = qtdItens,
            produtoFoiEmbalado = produtoFoiEmbalado ?: false,
            corteNoEndereco = corteNoEndereco ?: false,
            caixaFechada = caixaFechada ?: false
        )

        listaDeDados.add(registro)
        Log.i("INFO_COLETA", "Registro adicionado: $registro")

        reiniciarCronometro()
        resetarEstadoParaNovaColeta()
        rodando = true
    }

    private fun mostrarErro(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        rodando = true
    }

    private fun inicializarViews() {
        //corOriginalBotao = ContextCompat.getColor(this, R.color.button_background_color)
        voltar2_imageView = findViewById(R.id.voltar2_imageView)
        finalizaButton = findViewById(R.id.finalizar_ir_para_SaveScreen_button)
        proximoButton = findViewById(R.id.proximo_ir_saveScreen_button)
        contadorTextView = findViewById(R.id.contador_cronometro_textView)
        rua_referente_ao_endereco_TextInputEditText = findViewById(R.id.rua_referente_ao_endereco_TextInputEditText)
        quantidade_de_itens_coletados_TextInputEditText = findViewById(R.id.quantidade_de_itens_coletados_TextInputEditText)
        corte_no_endereco_sim_button = findViewById(R.id.corte_no_endereco_sim_button)
        corte_no_endereco_nao_button = findViewById(R.id.corte_no_endereco_nao_button)
        embalagem_nao_button = findViewById(R.id.embalado_nao_button)
        embalado_sim_button = findViewById(R.id.embalado_sim_button)
        caixa_fechada_sim_button = findViewById(R.id.caixa_fechada_sim_button)
        caixa_fechada_nao_button = findViewById(R.id.caixa_fechada_nao_button)
    }

    private fun configurarListenersBotoes() {
        embalado_sim_button.setOnClickListener {
            produtoFoiEmbalado = true
            selecionarBotao(embalado_sim_button, embalagem_nao_button)
        }
        embalagem_nao_button.setOnClickListener {
            produtoFoiEmbalado = false
            selecionarBotao(embalagem_nao_button, embalado_sim_button, isNao = true)
        }
        corte_no_endereco_sim_button.setOnClickListener {
            corteNoEndereco = true
            selecionarBotao(corte_no_endereco_sim_button, corte_no_endereco_nao_button)
        }
        corte_no_endereco_nao_button.setOnClickListener {
            corteNoEndereco = false
            selecionarBotao(corte_no_endereco_nao_button, corte_no_endereco_sim_button, isNao = true)
        }
        caixa_fechada_sim_button.setOnClickListener {
            caixaFechada = true
            selecionarBotao(caixa_fechada_sim_button, caixa_fechada_nao_button)
        }
        caixa_fechada_nao_button.setOnClickListener {
            caixaFechada = false
            selecionarBotao(caixa_fechada_nao_button, caixa_fechada_sim_button, isNao = true)
        }
    }

    private fun selecionarBotao(selecionado: Button, outro: Button, isNao: Boolean = false) {
        // 1. Define a cor de fundo correta (Verde ou Vermelho)
        val corFundo = if (isNao) Color.parseColor("#F44336") else Color.parseColor("#4CAF50")

        // 2. Define a cor da borda para o estado selecionado (Branco para contraste)
        val corBorda = Color.WHITE

        // 3. Define as dimensões da borda e dos cantos (converte dp para pixels)
        val cornerRadius = 25f * resources.displayMetrics.density
        val borderWidth = (2f * resources.displayMetrics.density).toInt()

        // 4. Cria um novo drawable para o botão SELECIONADO
        val selectedDrawable = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setColor(corFundo)                     // Cor de fundo
            setStroke(borderWidth, corBorda)      // Largura e cor da borda
            this.cornerRadius = cornerRadius      // Cantos arredondados
        }

        // 5. Aplica os fundos corretos
        selecionado.background = selectedDrawable             // Aplica o novo drawable com cor e borda
        outro.setBackgroundResource(R.drawable.button_glass) // Restaura o drawable de vidro original
    }

    private fun resetarSelecaoBotoesVisualmente() {
        val botoes = listOf(
            embalado_sim_button, embalagem_nao_button,
            corte_no_endereco_sim_button, corte_no_endereco_nao_button,
            caixa_fechada_sim_button, caixa_fechada_nao_button
        )

        // Garante que TODOS os botões comecem (ou voltem) ao estado de vidro original
        // SEM APLICAR NENHUM TINT
        botoes.forEach {
            it.setBackgroundResource(R.drawable.button_glass)
        }
    }




    private fun resetarEstadoParaNovaColeta() {
        produtoFoiEmbalado = null
        corteNoEndereco = null
        caixaFechada = null
        resetarSelecaoBotoesVisualmente()
        rua_referente_ao_endereco_TextInputEditText.text = null
        quantidade_de_itens_coletados_TextInputEditText.text = null
        rua_referente_ao_endereco_TextInputEditText.clearFocus()
        quantidade_de_itens_coletados_TextInputEditText.clearFocus()
    }

    private fun iniciarCronometro() {
        handler.post(object : Runnable {
            override fun run() {
                if (rodando) {
                    val horas = segundos / 3600
                    val minutos = (segundos % 3600) / 60
                    val segs = segundos % 60
                    contadorTextView.text = String.format("%02d:%02d:%02d", horas, minutos, segs)
                    segundos++
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun reiniciarCronometro() {
        segundos = 0
        contadorTextView.text = "00:00:00"
    }

    override fun onDestroy() {
        super.onDestroy()
        rodando = false
        handler.removeCallbacksAndMessages(null)
    }
}
