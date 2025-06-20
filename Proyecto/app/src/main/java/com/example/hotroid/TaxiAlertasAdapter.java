package com.example.hotroid;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hotroid.bean.TaxiAlertasBeans;
import java.util.List;

public class TaxiAlertasAdapter extends RecyclerView.Adapter<TaxiAlertasAdapter.ViewHolder> {

    private List<TaxiAlertasBeans> listaAlertas;
    private Context context;

    public TaxiAlertasAdapter(Context context, List<TaxiAlertasBeans> listaAlertas) {
        this.context = context;
        this.listaAlertas = listaAlertas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.taxi_alertas_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaxiAlertasBeans alerta = listaAlertas.get(position);

        String nombreCompletoCliente = alerta.getNombresCliente() + " " + alerta.getApellidosCliente();
        holder.tvNombreNoti.setText(nombreCompletoCliente);

        holder.tvLugarNoti.setText("Origen: " + alerta.getOrigen());
        holder.tvLugarNoti2.setText("Destino: " + alerta.getDestino());

        holder.tvTiempoNoti.setText(alerta.getTiempoTranscurrido());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TaxiViaje.class);

            intent.putExtra("documentId", alerta.getDocumentId());
            intent.putExtra("nombresCliente", alerta.getNombresCliente());
            intent.putExtra("apellidosCliente", alerta.getApellidosCliente());
            intent.putExtra("origen", alerta.getOrigen());
            intent.putExtra("destino", alerta.getDestino());
            intent.putExtra("timestamp", alerta.getTimestamp() != null ? alerta.getTimestamp().getTime() : 0);
            intent.putExtra("estadoViaje", alerta.getEstadoViaje());
            intent.putExtra("region", alerta.getRegion());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaAlertas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreNoti;
        TextView tvLugarNoti;
        TextView tvLugarNoti2;
        TextView tvTiempoNoti;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreNoti = itemView.findViewById(R.id.tvNombreNoti);
            tvLugarNoti = itemView.findViewById(R.id.tvLugarNoti);
            tvLugarNoti2 = itemView.findViewById(R.id.tvLugarNoti2);
            tvTiempoNoti = itemView.findViewById(R.id.tvTiempoNoti);
        }
    }

    public void updateList(List<TaxiAlertasBeans> newAlertasList) {
        this.listaAlertas = newAlertasList;
        notifyDataSetChanged();
    }
}