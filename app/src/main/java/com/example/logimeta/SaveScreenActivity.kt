package com.example.logimeta
import RegistroColeta
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
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


    lateinit var saveButton: Button
    lateinit var notSaveButton: Button

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

        with(binding) {
            saveButton.setOnClickListener {
                salvar()
            }
            notSaveButton.setOnClickListener {
               // naoSalvar()
            }
        }

        saveButton = findViewById(R.id.save_button)
        notSaveButton = findViewById(R.id.not_save_button)

        val bundle = intent.extras
        if (bundle != null) {
            moduloSelecionado = bundle.getString("MODULO_SELECIONADO")
            totalEnderecos = bundle.getString("TOTAL_ENDERECOS")
            totalItens = bundle.getString("TOTAL_ITENS")
            nomeSeparador = bundle.getString("NOME_SEPARADOR")
            listaColeta = intent.getSerializableExtra("lista") as? ArrayList<RegistroColeta>

        }
        println("-------------------------------")
        println(listaColeta?.firstOrNull()?.tempoColeta)
        println(moduloSelecionado)
        println("--------------------------------")


        notSaveButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        saveButton.setOnClickListener {
            val intent = Intent(this, HistoricoDeTestesActivity::class.java)
            salvar()
            startActivity(intent)
            finish()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun salvar() {
        val sql_coleta = "INSERT INTO SessaoColeta (" +
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
            bancoDeDados.writableDatabase.execSQL(sql_coleta)
            Log.i("info_db", "Dados salvos com sucesso (SessãoColeta)")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("info_db", "Erro ao salvar dados (SessãoColeta)")
        }

        Log.d("info_db", "Iniciando loop. Tamanho da listaColeta: ${listaColeta?.size}")

        listaColeta?.forEach { registro ->
            val sql_registro = "INSERT INTO RegistroColeta (" +
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
                bancoDeDados.writableDatabase.execSQL(sql_registro)
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
    }
    fun naoSalvar() {

    }
}