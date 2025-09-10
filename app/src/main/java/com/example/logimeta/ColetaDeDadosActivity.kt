package com.example.logimeta

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
import androidx.core.content.ContextCompat // Importar para cores de recursos
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class ColetaDeDadosActivity : AppCompatActivity() {

    lateinit var voltar2_imageView: ImageView
    lateinit var finalizaButton: Button
    lateinit var proximoButton: Button
    lateinit var contadorTextView: TextView

    lateinit var rua_referente_ao_endereco_TextInputEditText: TextInputEditText
    lateinit var quantidade_de_itens_coletados_TextInputEditText: TextInputEditText

    lateinit var corte_no_endereco_sim_button: Button
    lateinit var corte_no_endereco_nao_button: Button

    lateinit var embalagem_nao_button: Button
    lateinit var embalado_sim_button: Button

    private var produtoFoiEmbalado: Boolean? = null
    private var corteNoEndereco: Boolean? = null

    private var segundos = 0
    private var rodando = true
    private val handler = Handler(Looper.getMainLooper())

    private val listaTempos = mutableListOf<String>()

    private var corOriginalBotao: Int = 0 // Será inicializada no onCreate

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

        resetarSelecaoBotoesVisualmente() // Chama para garantir estado inicial visual

        iniciarCronometro()

        embalado_sim_button.setOnClickListener {
            produtoFoiEmbalado = true
            embalado_sim_button.setBackgroundColor(Color.parseColor("#4CAF50"))
            embalagem_nao_button.setBackgroundColor(corOriginalBotao)
            Toast.makeText(this, "Produto marcado como embalado", Toast.LENGTH_SHORT).show()
        }

        embalagem_nao_button.setOnClickListener {
            produtoFoiEmbalado = false
            embalagem_nao_button.setBackgroundColor(Color.parseColor("#F44336"))
            embalado_sim_button.setBackgroundColor(corOriginalBotao)
            Toast.makeText(this, "Produto marcado como NÃO embalado", Toast.LENGTH_SHORT).show()
        }

        corte_no_endereco_sim_button.setOnClickListener {
            corteNoEndereco = true
            corte_no_endereco_sim_button.setBackgroundColor(Color.parseColor("#4CAF50"))
            corte_no_endereco_nao_button.setBackgroundColor(corOriginalBotao)
            Toast.makeText(this, "Corte no endereço: Sim", Toast.LENGTH_SHORT).show()
        }

        corte_no_endereco_nao_button.setOnClickListener {
            corteNoEndereco = false
            corte_no_endereco_nao_button.setBackgroundColor(Color.parseColor("#F44336"))
            corte_no_endereco_sim_button.setBackgroundColor(corOriginalBotao)
            Toast.makeText(this, "Corte no endereço: Não", Toast.LENGTH_SHORT).show()
        }

        finalizaButton.setOnClickListener {
            if (produtoFoiEmbalado == null) {
                Toast.makeText(this, "Por favor, selecione se o produto foi embalado.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (corteNoEndereco == null) {
                Toast.makeText(this, "Por favor, selecione se houve corte no endereço.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val intent = Intent(this, SaveScreenActivity::class.java)
            intent.putStringArrayListExtra("tempos", ArrayList(listaTempos))
            intent.putExtra("rua_referente_ao_endereco", rua_referente_ao_endereco_TextInputEditText.text.toString())
            intent.putExtra("quantidade_de_itens_coletados", quantidade_de_itens_coletados_TextInputEditText.text.toString())
            intent.putExtra("PRODUTO_FOI_EMBALADO", produtoFoiEmbalado)
            intent.putExtra("CORTE_NO_ENDERECO", corteNoEndereco)
            startActivity(intent)
        }

        proximoButton.setOnClickListener {
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
    }

    private fun resetarEstadoSelecao() {
        // Reseta as variáveis de estado lógico
        produtoFoiEmbalado = null
        corteNoEndereco = null

        // Reseta o feedback visual dos botões
        resetarSelecaoBotoesVisualmente()

        // Opcional: Limpar campos de texto também, se fizer sentido para o seu fluxo
        // rua_referente_ao_endereco_TextInputEditText.text = null
        // quantidade_de_itens_coletados_TextInputEditText.text = null

        Toast.makeText(this, "Pronto para nova coleta.", Toast.LENGTH_SHORT).show()
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

