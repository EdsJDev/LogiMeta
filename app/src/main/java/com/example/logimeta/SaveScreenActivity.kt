package com.example.logimeta
import RegistroColeta
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.logimeta.database.DatabaseHelper
import com.example.logimeta.databinding.ActivitySaveScreenBinding
import kotlin.getValue

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
        //println(listaColeta?.firstOrNull()?.tempoColeta) // teste para verificar se os dados estão chegando com null

        binding.notSaveButton.setOnClickListener {
            naoSalvar()
        }

        binding.saveButton.setOnClickListener {
            salvar()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun salvar() {
        val sqlColeta = "INSERT INTO SessaoColeta (" +
                "nome_separador," +
                "modulo_selecionado," +
                "total_enderecos," +
                "total_itens" +
                ")" +
                "VALUES (" +
                "'${nomeSeparador}'," +
                "'${moduloSelecionado}'," +
                "'${totalEnderecos}'," +
                "'${totalItens}'" +
                ");"


        try {
            bancoDeDados.writableDatabase.execSQL(sqlColeta)
            Log.i("info_db", "Dados salvos com sucesso (SessãoColeta)")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("info_db", "Erro ao salvar dados (SessãoColeta)")
        }

        Log.d("info_db", "Iniciando loop. Tamanho da listaColeta: ${listaColeta?.size}")

        listaColeta?.forEach { registro ->
            val sqlRegistro = "INSERT INTO RegistroColeta (" +
                    "id_sessao," +
                    " tempo_coleta," +
                    " rua_endereco," +
                    " qtd_itens_coletados," +
                    " produto_embalado," +
                    " corte_no_endereco, " +
                    "caixa_fechada) " +
                    "VALUES (" +
                     "-1, " +
                    "'${registro.tempoColeta}', " +
                    "'${registro.ruaEndereco}', " +
                    "${registro.qtdItensColetados}, " +
                    "${registro.produtoFoiEmbalado}, " +
                    "${registro.corteNoEndereco}, " +
                    "${registro.caixaFechada}" +
                    ");"

            try {
                bancoDeDados.writableDatabase.execSQL(sqlRegistro)
                Log.i(
                    "info_db",
                    "SUCESSO: Registro salvo para o endereço '${registro.ruaEndereco}'."
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(
                    "info_db",
                    "ERRO ao salvar registro para o endereço '${registro.ruaEndereco}': ${e.message}"
                )
            }
        }

        val intent = Intent(this, HistoricoDeTestesActivity::class.java)
        startActivity(intent)
    }
    fun naoSalvar() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}