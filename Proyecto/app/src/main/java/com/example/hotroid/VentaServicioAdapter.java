// app/src/main/java/com/example/hotroid/adapters/VentaServicioAdapter.java
package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.VentaServicioConsolidado; // Correct import for your bean

import java.util.List;
import java.util.Locale;

public class VentaServicioAdapter extends RecyclerView.Adapter<VentaServicioAdapter.VentaServicioViewHolder> {

    private List<VentaServicioConsolidado> ventasList;

    public VentaServicioAdapter(List<VentaServicioConsolidado> ventasList) {
        this.ventasList = ventasList;
    }

    /**
     * Updates the data in the adapter and notifies the RecyclerView to refresh.
     * @param newVentasList The new list of consolidated sales data.
     */
    public void updateData(List<VentaServicioConsolidado> newVentasList) {
        this.ventasList = newVentasList;
        notifyDataSetChanged(); // Notify adapter that the dataset has changed
    }

    @NonNull
    @Override
    public VentaServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single item in the RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venta_servicio, parent, false);
        return new VentaServicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VentaServicioViewHolder holder, int position) {
        VentaServicioConsolidado venta = ventasList.get(position);

        // Bind data to the TextViews in the ViewHolder
        holder.tvServicioNombre.setText(venta.getNombreServicio());
        holder.tvCantidad.setText(String.valueOf(venta.getCantidadTotal()));
        // Format the total amount to two decimal places and display
        holder.tvMontoTotal.setText(String.format(Locale.getDefault(), "%.2f", venta.getMontoTotal()));
    }

    @Override
    public int getItemCount() {
        return ventasList.size(); // Return the total number of items in the list
    }

    // ViewHolder class to hold references to the views for each item
    public static class VentaServicioViewHolder extends RecyclerView.ViewHolder {
        TextView tvServicioNombre;
        TextView tvCantidad;
        TextView tvMontoTotal;

        public VentaServicioViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views by their IDs from the item_venta_servicio.xml layout
            tvServicioNombre = itemView.findViewById(R.id.tvItemServicioNombre);
            tvCantidad = itemView.findViewById(R.id.tvItemCantidad);
            tvMontoTotal = itemView.findViewById(R.id.tvItemMontoTotal);
        }
    }
}