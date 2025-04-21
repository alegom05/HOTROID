package com.example.hotroid;
import com.example.hotroid.R;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.example.hotroid.bean.Notificacion;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.List;

public class NotificacionesAdapterUser extends RecyclerView.Adapter<NotificacionesAdapterUser.ViewHolder> {

    private List<Notificacion> notificaciones;

    public NotificacionesAdapterUser(List<Notificacion> notificaciones){
        this.notificaciones = notificaciones;
    }

    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacion_user, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacion noti = notificaciones.get(position);
        holder.tvMensajeResumen.setText(noti.getMensajeResumen());
        //holder.imgAdjunto.setImageResource(noti.getRecursoImagen());
        int recurso = noti.getRecursoImagen();
        holder.imgAdjunto.setImageResource(recurso);

        // Cambiar color según el ícono
        if (recurso == R.drawable.qr_code_2_24) {
            holder.imgAdjunto.setColorFilter(Color.BLACK);
        } else if (recurso == R.drawable.ic_euro) {
            holder.imgAdjunto.setColorFilter(Color.parseColor("#4CAF50")); // verde tipo material design
        } else {
            holder.imgAdjunto.clearColorFilter(); // Por si hay otros íconos sin color específico
        }

        //mostrar el popup
        holder.itemView.setOnClickListener(v -> {
            View popup= LayoutInflater.from(v.getContext())
                    .inflate(R.layout.user_detalle_notificacion,null);
            TextView tvMensaje = popup.findViewById(R.id.tvMensajeCompleto);
            ImageView qrGenerado = popup.findViewById(R.id.qrGenerado);

            tvMensaje.setText(noti.getMensajeCompleto());
            if (noti.tieneCodigoQr()){
                Bitmap qrBitmap = generarCodigoQR(noti.getCodigoQr());
                qrGenerado.setImageBitmap(qrBitmap);
                qrGenerado.setVisibility(View.VISIBLE);
            }else{
                qrGenerado.setVisibility(View.GONE);
            }

            new AlertDialog.Builder(v.getContext())
                    .setView(popup)
                    .setCancelable(true)
                    .create()
                    .show();
        });
    }
    @Override
    public int getItemCount() {
        return notificaciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMensajeResumen;
        ImageView imgAdjunto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMensajeResumen = itemView.findViewById(R.id.tvMensajeResumen);
            imgAdjunto = itemView.findViewById(R.id.imgAdjunto);
        }
    }

    private Bitmap generarCodigoQR(String texto) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            int size = 512;
            com.google.zxing.common.BitMatrix bitMatrix =
                    qrCodeWriter.encode(texto, BarcodeFormat.QR_CODE, size, size);

            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}