package com.example.hotroid; // Make sure this package matches your project

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.VentaServicioConsolidado;

import java.text.DecimalFormat;
import java.util.List;

public class VentaServicioAdapter extends RecyclerView.Adapter<VentaServicioAdapter.VentaServicioViewHolder> {

    private List<VentaServicioConsolidado> ventasList;

    public VentaServicioAdapter(List<VentaServicioConsolidado> ventasList) {
        this.ventasList = ventasList;
    }

    public void updateData(List<VentaServicioConsolidado> newList) {
        this.ventasList.clear();
        this.ventasList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VentaServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // *** IMPORTANT: INFLATE THE CORRECT LAYOUT FOR THE TABLE ROW ***
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venta_servicio, parent, false);
        return new VentaServicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VentaServicioViewHolder holder, int position) {
        VentaServicioConsolidado venta = ventasList.get(position);
        holder.bind(venta);
    }

    @Override
    public int getItemCount() {
        return ventasList.size();
    }

    public static class VentaServicioViewHolder extends RecyclerView.ViewHolder {
        private TextView tvServiceNombre;
        private TextView tvCantidadTotal;
        private TextView tvMontoTotal;
        private DecimalFormat df = new DecimalFormat("0.00"); // For formatting currency

        public VentaServicioViewHolder(@NonNull View itemView) {
            super(itemView);
            // *** IMPORTANT: FIND THE CORRECT VIEWS BY THEIR IDS IN item_venta_servicio_row.xml ***
            tvServiceNombre = itemView.findViewById(R.id.tvServiceNombre);
            tvCantidadTotal = itemView.findViewById(R.id.tvCantidadTotal);
            tvMontoTotal = itemView.findViewById(R.id.tvMontoTotal);
        }

        public void bind(VentaServicioConsolidado venta) {
            tvServiceNombre.setText(venta.getNombreServicio());
            tvCantidadTotal.setText(String.valueOf(venta.getCantidadTotal()));
            tvMontoTotal.setText(df.format(venta.getMontoTotal())); // Format the amount
        }
    }
}