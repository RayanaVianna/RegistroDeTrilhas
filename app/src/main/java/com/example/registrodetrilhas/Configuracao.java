package com.example.registrodetrilhas;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.GoogleMap;

public class Configuracao extends AppCompatActivity {

    private PrefsMaps prefsMaps;
    private RadioButton rbVetorial, rbSatelite, rbNorthUp, rbCourseUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracao);

        prefsMaps = new PrefsMaps(this);

        rbVetorial = findViewById(R.id.rbVetorial);
        rbSatelite = findViewById(R.id.rbSatelite);
        rbNorthUp = findViewById(R.id.rbNorthUp);
        rbCourseUp = findViewById(R.id.rbCourseUp);

        carregarConfiguracoesAtuais();

        findViewById(R.id.btnSalvarConfig).setOnClickListener(v -> salvarConfiguracoes());
    }

    private void carregarConfiguracoesAtuais() {
        if (prefsMaps.getTipoMapa() == GoogleMap.MAP_TYPE_SATELLITE) {
            rbSatelite.setChecked(true);
        } else {
            rbVetorial.setChecked(true);
        }

        if (prefsMaps.getModoNavegacao() == PrefsMaps.NAV_COURSE_UP) {
            rbCourseUp.setChecked(true);
        } else {
            rbNorthUp.setChecked(true);
        }
    }

    private void salvarConfiguracoes() {
        int tipoMapa = rbSatelite.isChecked() ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL;
        int modoNav = rbCourseUp.isChecked() ? PrefsMaps.NAV_COURSE_UP : PrefsMaps.NAV_NORTH_UP;

        prefsMaps.setTipoMapa(tipoMapa);
        prefsMaps.setModoNavegacao(modoNav);

        Toast.makeText(this, "Configurações salvas!", Toast.LENGTH_SHORT).show();
        finish();
    }
}