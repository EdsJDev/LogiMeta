package com.example.logimeta
import RegistroColeta
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class ColetaDeDadosActivity : AppCompatActivity() {

    lateinit var voltar2_imageView: ImageView
    lateinit var finalizaButton: Button
    lateinit var proximoButton: Button
    lateinit var contadorTextView: TextView
    private val ListaDeDados = mutableListOf<RegistroColeta>()
    lateinit var rua_referente_ao_endereco_TextInputEditText: TextInputEditText
    lateinit var quantidade_de_itens_coletados_TextInputEditText: TextInputEditText
    lateinit var corte_no_endereco_sim_button: Button
    lateinit var corte_no_endereco_nao_button: Button
    lateinit var embalagem_nao_button: Button
    lateinit var embalado_sim_button: Button
    lateinit var caixa_fechada_sim_button: Button
    lateinit var caixa_fechada_nao_button: Button
    private var produtoFoiEmbalado: Boolean? = null
    private var corteNoEndereco: Boolean? = null
    private var caixaFechada: Boolean? = null
    private var segundos = 0
    private var rodando = true
    private val handler = Handler(Looper.getMainLooper())
    private val listaTempos = mutableListOf<String>()
    private var corOriginalBotao: Int = 0 // Será inicializada no onCreate
    //private val DEBUG_PREFIX = "SaveScreenData: "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_coleta_de_dados)

        corOriginalBotao = Color.parseColor("#7c3c33")

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

        val bundle = intent.extras

        val moduloSelecionado = bundle?.getString("MODULO_SELECIONADO")
        val totalEnderecos = bundle?.getString("TOTAL_ENDERECOS")
        val totalItens = bundle?.getString("TOTAL_ITENS")
        val nomeSeparador = bundle?.getString("NOME_SEPARADOR")

        resetarSelecaoBotoesVisualmente() // Chama para garantir estado inicial visual

        iniciarCronometro()

        embalado_sim_button.setOnClickListener {
            produtoFoiEmbalado = true
            embalado_sim_button.setBackgroundColor(Color.parseColor("#4CAF50"))
            embalagem_nao_button.setBackgroundColor(corOriginalBotao)
        }

        embalagem_nao_button.setOnClickListener {
            produtoFoiEmbalado = false
            embalagem_nao_button.setBackgroundColor(Color.parseColor("#F44336"))
            embalado_sim_button.setBackgroundColor(corOriginalBotao)
        }

        corte_no_endereco_sim_button.setOnClickListener {
            corteNoEndereco = true
            corte_no_endereco_sim_button.setBackgroundColor(Color.parseColor("#4CAF50"))
            corte_no_endereco_nao_button.setBackgroundColor(corOriginalBotao)
        }

        corte_no_endereco_nao_button.setOnClickListener {
            corteNoEndereco = false
            corte_no_endereco_nao_button.setBackgroundColor(Color.parseColor("#F44336"))
            corte_no_endereco_sim_button.setBackgroundColor(corOriginalBotao)
        }

        caixa_fechada_sim_button.setOnClickListener {
            caixaFechada = true
            caixa_fechada_sim_button.setBackgroundColor(Color.parseColor("#4CAF50"))
            caixa_fechada_nao_button.setBackgroundColor(corOriginalBotao)
        }

        caixa_fechada_nao_button.setOnClickListener {
            caixaFechada = false
            caixa_fechada_nao_button.setBackgroundColor(Color.parseColor("#F44336"))
            caixa_fechada_sim_button.setBackgroundColor(corOriginalBotao)
        }

        finalizaButton.setOnClickListener {
            // Dentro do finalizaButton.setOnClickListener na ColetaDeDadosActivity
            val intent = Intent(this, SaveScreenActivity::class.java).apply{
                putExtra("MODULO_SELECIONADO", moduloSelecionado)
                putExtra("TOTAL_ENDERECOS", totalEnderecos)
                putExtra("TOTAL_ITENS", totalItens)
                putExtra("NOME_SEPARADOR", nomeSeparador)
                putExtra("lista", ArrayList(ListaDeDados))
            }
                startActivity(intent)
                ListaDeDados.clear() // Limpar a lista após o clique no botão "Finalizar"

            }

        proximoButton.setOnClickListener {

            //var modulo = moduloSelecionado
            var enderecos = totalEnderecos
            var itens = totalItens
            var tempoDoEndereco = contadorTextView.text


            moduloSelecionado
            enderecos?.toIntOrNull()
            itens?.toIntOrNull()
            nomeSeparador

            val registro = RegistroColeta(
                tempoDoEndereco.toString(),
                rua_referente_ao_endereco_TextInputEditText.text.toString(),
                quantidade_de_itens_coletados_TextInputEditText.text.toString(),
                produtoFoiEmbalado,
                corteNoEndereco,
                caixaFechada
            )
            ListaDeDados.add(registro)
            println("Nome do separador: ${nomeSeparador}")
            println("Módulo selecionado: moduloSelecionado}")
            println("Total de endereços: ${totalEnderecos}")
            println("Total de itens: ${totalItens}")
            for (Lista in ListaDeDados){
                println("Tempo do endereço: ${Lista.tempoColeta}")
                println("Rua referente ao endereço: ${Lista.ruaEndereco}")
                println("Quantidade de itens coletados: ${Lista.qtdItensColetados}")
                println(
                    "Produto foi embalado? ${
                        if (Lista.produtoFoiEmbalado == true) "Sim" else "Não"
                    }"
                )
                println(
                    "Corte no endereço? ${
                        if (Lista.corteNoEndereco == true) "Sim" else "Não"
                    }"
                )
                println(
                    "Caixa fechada? ${
                        if (Lista.caixaFechada == true) "Sim" else "Não"
                    }"
                )
            }

            salvarTempo()
            reiniciarCronometro()
            resetarEstadoSelecao() // Chama a função para resetar
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

    private fun resetarSelecaoBotoesVisualmente() {
        embalado_sim_button.setBackgroundColor(corOriginalBotao)
        embalagem_nao_button.setBackgroundColor(corOriginalBotao)
        corte_no_endereco_sim_button.setBackgroundColor(corOriginalBotao)
        corte_no_endereco_nao_button.setBackgroundColor(corOriginalBotao)
        caixa_fechada_sim_button.setBackgroundColor(corOriginalBotao)
        caixa_fechada_nao_button.setBackgroundColor(corOriginalBotao)
    }

    private fun resetarEstadoSelecao() {
        // Reseta as variáveis de estado lógico
        produtoFoiEmbalado = null
        corteNoEndereco = null
        caixaFechada = null

        // Reseta o feedback visual dos botões
        resetarSelecaoBotoesVisualmente()

        // Opcional: Limpar campos de texto também
         //rua_referente_ao_endereco_TextInputEditText.text = null
         quantidade_de_itens_coletados_TextInputEditText.text = null

        //Toast.makeText(this, "Pronto para nova coleta.", Toast.LENGTH_SHORT).show()
    }

    private fun iniciarCronometro() {
        // ... (código do cronômetro) ...
        handler.post(object : Runnable {
            override fun run() {
                if (rodando) {
                    val minutos = segundos / 60
                    val seg = segundos % 60
                    contadorTextView.text = String.format("%02d:%02d", minutos, seg)
                    segundos++
                }
                handler.postDelayed(this, 1000)
            }
        })
    }
    private fun salvarTempo() {
        // ... (código de salvar tempo) ...
        val tempoAtual = contadorTextView.text.toString()
        listaTempos.add(tempoAtual)
    }

    private fun reiniciarCronometro() {
        // ... (código de reiniciar cronômetro) ...
        segundos = 0
        contadorTextView.text = "00:00"
    }

    override fun onDestroy() {
        super.onDestroy()
        rodando = false
        handler.removeCallbacksAndMessages(null)
    }
}


// botão caixa fechada implementado //