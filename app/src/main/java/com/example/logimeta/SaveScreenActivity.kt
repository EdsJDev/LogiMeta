package com.example.logimeta
import RegistroColeta
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SaveScreenActivity : AppCompatActivity() {

    lateinit var saveButton: Button
    lateinit var notSaveButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_save_screen)

        saveButton = findViewById(R.id.save_button)
        notSaveButton = findViewById(R.id.not_save_button)



        // Dentro do onCreate da SaveScreenActivity
        val bundle = intent.extras
        if (bundle != null) {
            val lista = intent.getSerializableExtra("lista") as? ArrayList<RegistroColeta>
            println(lista?.firstOrNull()?.nomeSeparador)
        }


        notSaveButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        saveButton.setOnClickListener {

            val intent = Intent(this, HistoricoDeTestesActivity::class.java)
            /*
          -- IMPLEMENTAÇÃO DO MÉTODO SALVAR
            }
            */
            startActivity(intent)
        }



        /*
            -- IMPLEMENTAÇÃO DO MÉTODO NÃO SALVAR

         */



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}