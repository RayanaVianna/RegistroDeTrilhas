package com.example.registrodetrilhas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class TrilhaDAO {
        private BancoDadosHelper bancoHelper;
        public TrilhaDAO(Context context) {
            bancoHelper = new BancoDadosHelper(context);
        }

        // CONSULTAR TODAS AS TRILHAS
        public List<Trilha> listarTrilhas() {

            List<Trilha> lista = new ArrayList<>();

            SQLiteDatabase db = bancoHelper.getReadableDatabase();

            Cursor cursor = db.rawQuery(
                    "SELECT * FROM trilhas ORDER BY id DESC",
                    null
            );

            if (cursor.moveToFirst()) {
                do {

                    int id = cursor.getInt(
                            cursor.getColumnIndexOrThrow("id"));

                    String nome = cursor.getString(
                            cursor.getColumnIndexOrThrow("nome"));

                    String dataInicio = cursor.getString(
                            cursor.getColumnIndexOrThrow("data_inicio"));

                    String dataFim = cursor.getString(
                            cursor.getColumnIndexOrThrow("data_fim"));

                    double velocidadeMedia = cursor.getDouble(
                            cursor.getColumnIndexOrThrow("velocidade_media"));

                    double velocidadeMaxima = cursor.getDouble(
                            cursor.getColumnIndexOrThrow("velocidade_maxima"));

                    double distancia = cursor.getDouble(
                            cursor.getColumnIndexOrThrow("distancia"));

                    int duracao = cursor.getInt(
                            cursor.getColumnIndexOrThrow("duracao"));

                    Trilha trilha = new Trilha(
                            id,
                            nome,
                            dataInicio,
                            dataFim,
                            velocidadeMedia,
                            velocidadeMaxima,
                            distancia,
                            duracao
                    );

                    lista.add(trilha);

                } while (cursor.moveToNext());
            }

            cursor.close();

            return lista;
        }

        // EDITAR O NOME DE UMA TRILHA
        public boolean editarNome(int id, String novoNome) {

            if (novoNome == null || novoNome.trim().isEmpty()) {
                return false;
            }

            SQLiteDatabase db = bancoHelper.getWritableDatabase();

            ContentValues valores = new ContentValues();
            valores.put("nome", novoNome.trim());

            int linhasAlteradas = db.update(
                    "trilhas",
                    valores,
                    "id = ?",
                    new String[]{String.valueOf(id)}
            );

            return linhasAlteradas > 0;
        }

        // EXCLUIR UMA TRILHA
        public boolean excluirTrilha(int id) {

            SQLiteDatabase db = bancoHelper.getWritableDatabase();

            int linhasExcluidas = db.delete(
                    "trilhas",
                    "id = ?",
                    new String[]{String.valueOf(id)}
            );

            return linhasExcluidas > 0;
        }

        // FECHAR O ACESSO AO BANCO QUANDO NÃO PRECISAR MAIS
        public void fecharBanco() {
            bancoHelper.close();
        }
}


