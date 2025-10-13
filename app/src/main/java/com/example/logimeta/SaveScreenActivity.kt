package com.example.logimeta
import com.example.logimeta.model.RegistroColeta
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.logimeta.database.ColetaDAO
import com.example.logimeta.databinding.ActivitySaveScreenBinding
import com.example.logimeta.model.SessaoColeta
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

    private val coletaDAO by lazy {
        ColetaDAO(this)
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
        val sessaoParaSalvar = SessaoColeta(
            nomeSeparador = nomeSeparador ?: "",
            moduloSelecionado = moduloSelecionado ?: "",
            totalEnderecos = (totalEnderecos ?: "0").toInt(),
            totalItens = (totalItens ?: "0").toInt()
        )

        // Se a lista for nula, usamos uma lista vazia para evitar erros
        val registrosParaSalvar = listaColeta ?: emptyList()

        Log.d("info_db", "Activity: Chamando o DAO para salvar a coleta completa.")
        val sucesso = coletaDAO.salvarColetaCompleta(sessaoParaSalvar, registrosParaSalvar)

        if (sucesso) {
            Log.i("info_db", "Activity: DAO retornou sucesso. Navegando para a próxima tela.")
            val intent = Intent(this, HistoricoDeTestesActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Log.e("info_db", "Activity: DAO retornou falha. O salvamento não foi concluído.")
            Toast.makeText(this, "Erro ao salvar os dados. Tente novamente.", Toast.LENGTH_LONG).show()
        }
    }

    private fun naoSalvar() {
        Log.i("info_db", "Operação cancelada pelo usuário. Retornando para MainActivity.")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
