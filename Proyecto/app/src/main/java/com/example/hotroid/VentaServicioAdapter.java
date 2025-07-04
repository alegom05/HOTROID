package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.VentaServicioConsolidado;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VentaServicioAdapter extends RecyclerView.Adapter<VentaServicioAdapter.VentaServicioViewHolder> implements Filterable {

    private List<VentaServicioConsolidado> listaVentas;
    private List<VentaServicioConsolidado> listaVentasFull; // Lista completa para el filtrado

    public VentaServicioAdapter(List<VentaServicioConsolidado> listaVentas) {
        this.listaVentas = listaVentas;
        // Inicializa listaVentasFull con una copia profunda para evitar modificar la original
        this.listaVentasFull = new ArrayList<>(listaVentas);
    }

    // Método para actualizar la lista completa cuando los datos cambian desde Firestore
    public void setListaVentas(List<VentaServicioConsolidado> nuevasVentas) {
        this.listaVentas.clear();
        this.listaVentas.addAll(nuevasVentas);
        this.listaVentasFull = new ArrayList<>(nuevasVentas); // Actualiza la lista full también
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VentaServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venta_servicio_card, parent, false);
        return new VentaServicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VentaServicioViewHolder holder, int position) {
        VentaServicioConsolidado venta = listaVentas.get(position);
        holder.bind(venta);
    }

    @Override
    public int getItemCount() {
        return listaVentas.size();
    }

    @Override
    public Filter getFilter() {
        return ventasFilter;
    }

    private Filter ventasFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<VentaServicioConsolidado> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(listaVentasFull); // Si el filtro está vacío, muestra la lista completa
            } else {
                String filterPattern = constraint.toString().toLowerCase(Locale.getDefault()).trim();

                for (VentaServicioConsolidado item : listaVentasFull) {
                    if (item.getNombreServicio().toLowerCase(Locale.getDefault()).contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Asegúrate de que 'results.values' sea casteado correctamente
            listaVentas.clear();
            listaVentas.addAll((List<VentaServicioConsolidado>) results.values);
            notifyDataSetChanged();
        }
    };

    public static class VentaServicioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreServicioCard;
        TextView tvCantidadCard;
        TextView tvMontoCard;

        public VentaServicioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreServicioCard = itemView.findViewById(R.id.tvNombreServicioCard);
            tvCantidadCard = itemView.findViewById(R.id.tvCantidadCard);
            tvMontoCard = itemView.findViewById(R.id.tvMontoCard);
        }

        public void bind(VentaServicioConsolidado venta) {
            DecimalFormat df = new DecimalFormat("0.00");
            tvNombreServicioCard.setText(venta.getNombreServicio());
            tvCantidadCard.setText("Cantidad: " + venta.getCantidadTotal());
            tvMontoCard.setText("Monto: S/. " + df.format(venta.getMontoTotal()));
        }
    }
}
