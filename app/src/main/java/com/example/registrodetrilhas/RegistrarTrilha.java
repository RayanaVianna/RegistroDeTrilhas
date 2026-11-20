package com.example.registrodetrilhas;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegistrarTrilha extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQ_LOCATION_PERMISSION = 1001;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private PrefsMaps prefsMaps;
    private TrilhaDAO trilhaDAO;

    private Marker userMarker;
    private Circle accuracyCircle;
    private Polyline rotaPolyline;
    private final List<LatLng> pontosRotaLatLng = new ArrayList<>();
    private final List<Trilha.Ponto> pontosColetados = new ArrayList<>();

    private TextView tvVelAtual, tvVelMaxima, tvDistanciaTotal;
    private Chronometer chronometer;
    private Button btnIniciarParar;

    private boolean gravando = false;
    private long dataInicioMs = 0;
    private double distanciaTotalMetros = 0.0;
    private double velocidadeMaximaKmh = 0.0;
    private float ultimoRumo = 0f;
    private Location ultimaLocalizacao = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_trilha);

        prefsMaps = new PrefsMaps(this);
        trilhaDAO = new TrilhaDAO(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        tvVelAtual = findViewById(R.id.tvVelAtual);
        tvVelMaxima = findViewById(R.id.tvVelMaxima);
        tvDistanciaTotal = findViewById(R.id.tvDistanciaTotal);
        chronometer = findViewById(R.id.chronometerTrilha);
        btnIniciarParar = findViewById(R.id.btnIniciarParar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapRegistrar);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnIniciarParar.setOnClickListener(v -> {
            if (!gravando) {
                iniciarGravacao();
            } else {
                finalizarGravacao();
            }
        });

        configurarLocationCallback();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(prefsMaps.getTipoMapa());
        mMap.getUiSettings().setCompassEnabled(true);

        rotaPolyline = mMap.addPolyline(new PolylineOptions()
                .color(Color.BLUE)
                .width(10f));

        verificarPermissaoEIniciarGps();
    }

    private void verificarPermissaoEIniciarGps() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION_PERMISSION);
            return;
        }
        iniciarAtualizacoesGps();
    }

    private void iniciarAtualizacoesGps() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void configurarLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location == null || mMap == null) return;

                atualizarInterfaceEMapa(location);
            }
        };
    }

    private void atualizarInterfaceEMapa(Location location) {
        LatLng posAtual = new LatLng(location.getLatitude(), location.getLongitude());

        float velKmh = 0f;
        if (location.hasSpeed()) {
            velKmh = location.getSpeed() * 3.6f;
        } else if (ultimaLocalizacao != null) {
            long dtMs = location.getTime() - ultimaLocalizacao.getTime();
            if (dtMs > 0) {
                float dist = ultimaLocalizacao.distanceTo(location);
                velKmh = (dist / (dtMs / 1000f)) * 3.6f;
            }
        }

        if (location.hasBearing() && location.getBearing() != 0f) {
            ultimoRumo = location.getBearing();
        } else if (ultimaLocalizacao != null && ultimaLocalizacao.distanceTo(location) > 1.5f) {
            ultimoRumo = ultimaLocalizacao.bearingTo(location);
        }

        if (userMarker == null) {
            userMarker = mMap.addMarker(new MarkerOptions()
                    .position(posAtual)
                    .title("Minha Posição")
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .icon(MapaUtils.bitmapDescriptorFromVector(this, R.drawable.ic_marcador_usuario)));
        } else {
            userMarker.setPosition(posAtual);
        }

        if (prefsMaps.getModoNavegacao() == PrefsMaps.NAV_COURSE_UP) {
            userMarker.setRotation(ultimoRumo);
        } else {
            userMarker.setRotation(0f);
        }

        float acuracia = location.hasAccuracy() ? location.getAccuracy() : 10f;
        if (accuracyCircle == null) {
            accuracyCircle = mMap.addCircle(new CircleOptions()
                    .center(posAtual)
                    .radius(acuracia)
                    .strokeWidth(2f)
                    .strokeColor(Color.argb(180, 21, 101, 192))
                    .fillColor(Color.argb(55, 33, 150, 243)));
        } else {
            accuracyCircle.setCenter(posAtual);
            accuracyCircle.setRadius(acuracia);
        }

        float bearingCamera = (prefsMaps.getModoNavegacao() == PrefsMaps.NAV_COURSE_UP) ? ultimoRumo : 0f;
        float tiltCamera = (prefsMaps.getModoNavegacao() == PrefsMaps.NAV_COURSE_UP) ? 35f : 0f;

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(posAtual)
                .zoom(17f)
                .bearing(bearingCamera)
                .tilt(tiltCamera)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        if (gravando) {
            if (ultimaLocalizacao != null) {
                float deltaDist = ultimaLocalizacao.distanceTo(location);
                if (deltaDist >= 1.0f) {
                    distanciaTotalMetros += deltaDist;
                }
            }

            if (velKmh > velocidadeMaximaKmh) {
                velocidadeMaximaKmh = velKmh;
            }

            tvVelAtual.setText(String.format(Locale.getDefault(), "%.1f km/h", velKmh));
            tvVelMaxima.setText(String.format(Locale.getDefault(), "%.1f km/h", velocidadeMaximaKmh));
            tvDistanciaTotal.setText(MapaUtils.formatarDistancia(distanciaTotalMetros));

            pontosRotaLatLng.add(posAtual);
            rotaPolyline.setPoints(pontosRotaLatLng);

            pontosColetados.add(new Trilha.Ponto(
                    location.getLatitude(),
                    location.getLongitude(),
                    System.currentTimeMillis(),
                    velKmh,
                    acuracia
            ));
        }

        ultimaLocalizacao = location;
    }

    private void iniciarGravacao() {
        gravando = true;
        dataInicioMs = System.currentTimeMillis();
        distanciaTotalMetros = 0.0;
        velocidadeMaximaKmh = 0.0;
        pontosRotaLatLng.clear();
        pontosColetados.clear();
        if (rotaPolyline != null) rotaPolyline.setPoints(pontosRotaLatLng);

        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        btnIniciarParar.setText("Encerrar e Salvar Trilha");
        btnIniciarParar.setBackgroundColor(Color.parseColor("#C62828"));
        Toast.makeText(this, "Monitoramento iniciado!", Toast.LENGTH_SHORT).show();
    }

    private void finalizarGravacao() {
        gravando = false;
        chronometer.stop();
        long dataFimMs = System.currentTimeMillis();

        btnIniciarParar.setText("Iniciar Registro");

        if (pontosColetados.isEmpty()) {
            Toast.makeText(this, "Nenhum ponto GPS registrado para salvar.", Toast.LENGTH_LONG).show();
            return;
        }

        EditText inputNome = new EditText(this);
        String sugestaoNome = "Trilha " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(dataInicioMs));
        inputNome.setText(sugestaoNome);
        inputNome.setSelection(sugestaoNome.length());

        new AlertDialog.Builder(this)
                .setTitle("Salvar Trilha")
                .setMessage("Informe um nome para a trilha realizada:")
                .setView(inputNome)
                .setCancelable(false)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String nome = inputNome.getText().toString().trim();
                    if (nome.isEmpty()) nome = sugestaoNome;

                    long duracaoMs = Math.max(1000, dataFimMs - dataInicioMs);
                    double horas = duracaoMs / 3600000.0;
                    double velMediaKmh = (distanciaTotalMetros / 1000.0) / horas;

                    Trilha trilha = new Trilha(
                            nome,
                            dataInicioMs,
                            dataFimMs,
                            velMediaKmh,
                            velocidadeMaximaKmh,
                            distanciaTotalMetros
                    );

                    long idSalvo = trilhaDAO.salvarTrilhaCompleta(trilha, pontosColetados);
                    if (idSalvo != -1) {
                        Toast.makeText(this, "Trilha salva com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Erro ao salvar no banco de dados.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Descartar", (dialog, which) ->
                        Toast.makeText(this, "Trilha descartada.", Toast.LENGTH_SHORT).show())
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            iniciarAtualizacoesGps();
        } else {
            Toast.makeText(this, "Permissão de localização necessária para registrar trilhas.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}