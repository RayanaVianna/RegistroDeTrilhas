package com.example.registrodetrilhas;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class BancoDadosHelper extends SQLiteOpenHelper {

        private static final String NOME_BANCO = "registro_trilhas.db";
        private static final int VERSAO_BANCO = 1;

        public static final String TABELA_TRILHAS = "trilhas";
        public static final String TABELA_PONTOS = "pontos_trilha";

        public BancoDadosHelper(Context context) {
            super(context, NOME_BANCO, null, VERSAO_BANCO);
        }

        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);
            db.setForeignKeyConstraintsEnabled(true);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            String sqlTrilhas = "CREATE TABLE " + TABELA_TRILHAS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nome TEXT NOT NULL, " +
                    "data_inicio TEXT, " +
                    "data_fim TEXT, " +
                    "velocidade_media REAL, " +
                    "velocidade_maxima REAL, " +
                    "distancia REAL, " +
                    "duracao INTEGER)";

            String sqlPontos = "CREATE TABLE " + TABELA_PONTOS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "trilha_id INTEGER NOT NULL, " +
                    "latitude REAL NOT NULL, " +
                    "longitude REAL NOT NULL, " +
                    "data_hora TEXT, " +
                    "FOREIGN KEY (trilha_id) REFERENCES " +
                    TABELA_TRILHAS + "(id) ON DELETE CASCADE)";

            db.execSQL(sqlTrilhas);
            db.execSQL(sqlPontos);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABELA_PONTOS);
            db.execSQL("DROP TABLE IF EXISTS " + TABELA_TRILHAS);
            onCreate(db);
        }
    }

