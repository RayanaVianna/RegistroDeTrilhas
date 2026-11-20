package com.example.registrodetrilhas;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConsultarTrilhas extends AppCompatActivity {

    private TrilhaDAO trilhaDAO;
    private ListView listViewTrilhas;
    private List<Trilha> listaTrilhas = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_trilhas);

        trilhaDAO = new TrilhaDAO(this);
        listViewTrilhas = findViewById(R.id.listViewTrilhas);

        findViewById(R.id.btnApagarTodas).setOnClickListener(v -> confirmarApagarTodas());
        findViewById(R.id.btnApagarIntervalo).setOnClickListener(v -> iniciarSelecaoIntervaloDatas());

        listViewTrilhas.setOnItemClickListener((parent, view, position, id) -> {
            Trilha selecionada = listaTrilhas.get(position);
            exibirMenuOpcoesTrilha(selecionada);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarListaTrilhas();
    }

    private void carregarListaTrilhas() {
        listaTrilhas = trilhaDAO.listarTodasTrilhas();
        List<String> itensFormatados = new ArrayList<>();

        for (Trilha t : listaTrilhas) {
            String resumo = t.getNome() + "\n" +
                    "Início: " + sdf.format(new Date(t.getDataInicio())) +
                    " | Distância: " + Maps.formatarDistancia(t.getDistanciaTotal());
            itensFormatados.add(resumo);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itensFormatados);
        listViewTrilhas.setAdapter(adapter);
    }

    private void exibirMenuOpcoesTrilha(Trilha trilha) {
        String[] opcoes = {"Consultar Trilha no Mapa", "Editar Nome da Trilha", "Apagar Esta Trilha"};

        new AlertDialog.Builder(this)
                .setTitle(trilha.getNome())
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(this, VisualizarTrilha.class);
                        intent.putExtra("TRILHA_ID", trilha.getId());
                        startActivity(intent);
                    } else if (which == 1) {
                        editarNomeTrilha(trilha);
                    } else if (which == 2) {
                        confirmarApagarEspecifica(trilha);
                    }
                })
                .show();
    }

    private void editarNomeTrilha(Trilha trilha) {
        EditText input = new EditText(this);
        input.setText(trilha.getNome());
        input.setSelection(trilha.getNome().length());

        new AlertDialog.Builder(this)
                .setTitle("Editar Nome")
                .setView(input)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String novoNome = input.getText().toString().trim();
                    if (!novoNome.isEmpty()) {
                        trilhaDAO.atualizarNomeTrilha(trilha.getId(), novoNome);
                        carregarListaTrilhas();
                        Toast.makeText(this, "Nome atualizado!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarApagarEspecifica(Trilha trilha) {
        new AlertDialog.Builder(this)
                .setTitle("Apagar Trilha")
                .setMessage("Deseja realmente apagar a trilha \"" + trilha.getNome() + "\"?")
                .setPositiveButton("Apagar", (dialog, which) -> {
                    trilhaDAO.apagarTrilha(trilha.getId());
                    carregarListaTrilhas();
                    Toast.makeText(this, "Trilha removida.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void iniciarSelecaoIntervaloDatas() {
        Calendar calInicio = Calendar.getInstance();
        DatePickerDialog dpInicio = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calInicio.set(year, month, dayOfMonth, 0, 0, 0);
            calInicio.set(Calendar.MILLISECOND, 0);

            Calendar calFim = Calendar.getInstance();
            DatePickerDialog dpFim = new DatePickerDialog(this, (view2, year2, month2, dayOfMonth2) -> {
                calFim.set(year2, month2, dayOfMonth2, 23, 59, 59);
                calFim.set(Calendar.MILLISECOND, 999);

                int removidas = trilhaDAO.apagarTrilhasPorIntervalo(calInicio.getTimeInMillis(), calFim.getTimeInMillis());
                carregarListaTrilhas();
                Toast.makeText(this, removidas + " trilha(s) apagada(s) no período.", Toast.LENGTH_SHORT).show();
            }, calFim.get(Calendar.YEAR), calFim.get(Calendar.MONTH), calFim.get(Calendar.DAY_OF_MONTH));

            dpFim.setTitle("Selecione a Data Final");
            dpFim.show();

        }, calInicio.get(Calendar.YEAR), calInicio.get(Calendar.MONTH), calInicio.get(Calendar.DAY_OF_MONTH));

        dpInicio.setTitle("Selecione a Data Inicial");
        dpInicio.show();
    }

    private void confirmarApagarTodas() {
        new AlertDialog.Builder(this)
                .setTitle("Apagar Todas as Trilhas")
                .setMessage("Tem certeza que deseja excluir permanentemente todas as trilhas registradas?")
                .setPositiveButton("Sim, apagar tudo", (dialog, which) -> {
                    trilhaDAO.apagarTodasTrilhas();
                    carregarListaTrilhas();
                    Toast.makeText(this, "Todas as trilhas foram apagadas.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}