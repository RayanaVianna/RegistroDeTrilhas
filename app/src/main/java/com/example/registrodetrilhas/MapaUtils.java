package com.example.registrodetrilhas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.Locale;

public class MapaUtils {
    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) return BitmapDescriptorFactory.defaultMarker();
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static String formatarDistancia(double metros) {
        if (metros >= 1000) {
            return String.format(Locale.getDefault(), "%.2f km", metros / 1000.0);
        }
        return String.format(Locale.getDefault(), "%.0f m", metros);
    }

    public static String formatarDuracao(long duracaoMs) {
        long segundosTotais = duracaoMs / 1000;
        long horas = segundosTotais / 3600;
        long minutos = (segundosTotais % 3600) / 60;
        long segundos = segundosTotais % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos, segundos);
    }
}