// app/src/main/java/com/example/hotroid/adapters/VentaClienteAdapter.java
package com.example.hotroid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.R;
import com.example.hotroid.bean.VentaClienteConsolidado;

import java.text.DecimalFormat;
import java.util.List;

public class VentaClienteAdapter extends RecyclerView.Adapter<VentaClienteAdapter.VentaClienteViewHolder> {

    private List<VentaClienteConsolidado> ventasConsolidadas;
    private DecimalFormat decimalFormat;

    public VentaClienteAdapter(List<VentaClienteConsolidado> ventasConsolidadas) {
        this.ventasConsolidadas = ventasConsolidadas;
        this.decimalFormat = new DecimalFormat("0.00");
    }

    @NonNull
    @Override
    public VentaClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venta_cliente, parent, false);
        return new VentaClienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VentaClienteViewHolder holder, int position) {
        VentaClienteConsolidado venta = ventasConsolidadas.get(position);
        holder.tvNombreCliente.setText(venta.getNombreCompletoCliente());
        holder.tvMontoTotal.setText(decimalFormat.format(venta.getMontoTotal()));
    }

    @Override
    public int getItemCount() {
        return ventasConsolidadas.size();
    }

    public void updateData(List<VentaClienteConsolidado> newData) {
        this.ventasConsolidadas.clear();
        this.ventasConsolidadas.addAll(newData);
        notifyDataSetChanged();
    }

    public static class VentaClienteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreCliente;
        TextView tvMontoTotal;

        public VentaClienteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreCliente = itemView.findViewById(R.id.tvNombreCliente);
            tvMontoTotal = itemView.findViewById(R.id.tvMontoTotal);
        }
    }
}