package com.example.logimeta
import RegistroColeta
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.logimeta.database.DatabaseHelper
import com.example.logimeta.databinding.ActivitySaveScreenBinding

class SaveScreenActivity : AppCompatActivity() {

    private var listaColeta: ArrayList<RegistroColeta>? = null
    private var moduloSelecionado: String? = null
    private var totalEnderecos: String? = null
    private var totalItens: String? = null
    private var nomeSeparador: String? = null

    private val binding by lazy {
        ActivitySaveScreenBinding.inflate(layoutInflater)
    }

    private val bancoDeDados by lazy {
        DatabaseHelper(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val bundle = intent.extras
        if (bundle != null) {
            moduloSelecionado = bundle.getString("MODULO_SELECIONADO")
            totalEnderecos = bundle.getString("TOTAL_ENDERECOS")
            totalItens = bundle.getString("TOTAL_ITENS")
            nomeSeparador = bundle.getString("NOME_SEPARADOR")
            listaColeta = intent.getSerializableExtra("lista") as? ArrayList<RegistroColeta>
        }

        binding.notSaveButton.setOnClickListener {
            naoSalvar()
        }

        binding.saveButton.setOnClickListener {
            salvar()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun salvar() {
        val sessaoValues = ContentValues().apply {
            put("nome_separador", nomeSeparador)
            put("modulo_selecionado", moduloSelecionado)
            put("total_enderecos", totalEnderecos)
            put("total_itens", totalItens)
        }

        val idNovaSessao: Long
        try {
            idNovaSessao = bancoDeDados.writableDatabase.insert("SessaoColeta", null, sessaoValues)

            if (idNovaSessao == -1L) {
                Log.e("info_db", "ERRO: Falha ao inserir na tabela SessaoColeta.")
                return
            } else {
                Log.i("info_db", "SUCESSO: SessaoColeta salva com o ID: $idNovaSessao")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("info_db", "ERRO CRÍTICO ao salvar na tabela SessaoColeta: ${e.message}")
            return
        }

        Log.d("info_db", "Iniciando loop. Tamanho da listaColeta: ${listaColeta?.size}")

        listaColeta?.forEach { registro ->
            println(listaColeta?.size)
            val registroValues = ContentValues().apply {
                put("id_sessao", idNovaSessao)
                put("tempo_coleta", registro.tempoColeta)
                put("rua_endereco", registro.ruaEndereco)
                put("qtd_itens_coletados", registro.qtdItensColetados)
                put("produto_embalado", registro.produtoFoiEmbalado)
                put("corte_no_endereco", registro.corteNoEndereco)
                put("caixa_fechada", registro.caixaFechada)
            }

            try {
                bancoDeDados.writableDatabase.insert("RegistroColeta", null, registroValues)
                Log.i("info_db", "SUCESSO: Registro salvo para o endereço '${registro.ruaEndereco}'.")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("info_db", "ERRO ao salvar registro para o endereço '${registro.ruaEndereco}': ${e.message}")
            }
        }

        Log.i("info_db", "Processo de salvamento concluído. Navegando para HistoricoDeTestesActivity.")
        val intent = Intent(this, HistoricoDeTestesActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun naoSalvar() {
        Log.i("info_db", "Operação cancelada pelo usuário. Retornando para MainActivity.")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
