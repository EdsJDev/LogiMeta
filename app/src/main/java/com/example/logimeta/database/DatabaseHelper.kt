package com.example.logimeta.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context): SQLiteOpenHelper(context, "logi.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {

        try {
            db?.execSQL("""
                CREATE TABLE SessaoColeta (
                -- Chave Primária, única e auto-incrementada
                id_sessao INTEGER PRIMARY KEY AUTOINCREMENT,
                nome_separador TEXT NOT NULL,
                modulo_selecionado TEXT NOT NULL,
                total_enderecos INTEGER,
                total_itens INTEGER,
                data_sessao TEXT DEFAULT (datetime('now'))
            );
        """)

            db?.execSQL("""
                CREATE TABLE RegistroColeta (
                id_coleta INTEGER PRIMARY KEY AUTOINCREMENT,
                -- Coluna que armazena a referência (o valor deve existir na SessaoColeta)
                id_sessao INTEGER NOT NULL, 
                tempo_coleta TEXT,
                rua_endereco TEXT,
                qtd_itens_coletados INTEGER,
                produto_embalado INTEGER,
                corte_no_endereco INTEGER,
                caixa_fechada INTEGER,
                
                -- Restrição de Chave Estrangeira
                FOREIGN KEY (id_sessao) 
                    REFERENCES SessaoColeta(id_sessao) 
                    ON DELETE CASCADE
            );
        """)
            Log.i("info_db", "Banco de dados criado com sucesso")
        }catch (e: Exception){
            Log.i("info_db", "Erro ao criar banco de dados")
            e.printStackTrace()
        }
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
        TODO("Not yet implemented")
    }
}