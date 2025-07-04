package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.VentaClienteConsolidado;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VentaClienteAdapter extends RecyclerView.Adapter<VentaClienteAdapter.VentaClienteViewHolder> implements Filterable {

    private List<VentaClienteConsolidado> listaVentasCliente;
    private List<VentaClienteConsolidado> listaVentasClienteFull; // Lista completa para el filtrado

    public VentaClienteAdapter(List<VentaClienteConsolidado> listaVentasCliente) {
        this.listaVentasCliente = listaVentasCliente;
        this.listaVentasClienteFull = new ArrayList<>(listaVentasCliente); // Copia para el filtrado
    }

    // Método para actualizar la lista completa cuando los datos cambian desde Firestore
    public void setListaVentasCliente(List<VentaClienteConsolidado> nuevasVentas) {
        this.listaVentasCliente.clear();
        this.listaVentasCliente.addAll(nuevasVentas);
        this.listaVentasClienteFull = new ArrayList<>(nuevasVentas); // Actualiza la lista full también
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VentaClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venta_usuario_card, parent, false);
        return new VentaClienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VentaClienteViewHolder holder, int position) {
        VentaClienteConsolidado venta = listaVentasCliente.get(position);
        holder.bind(venta);
    }

    @Override
    public int getItemCount() {
        return listaVentasCliente.size();
    }

    @Override
    public Filter getFilter() {
        return ventasClienteFilter;
    }

    private Filter ventasClienteFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<VentaClienteConsolidado> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(listaVentasClienteFull); // Si el filtro está vacío, muestra la lista completa
            } else {
                String filterPattern = constraint.toString().toLowerCase(Locale.getDefault()).trim();

                for (VentaClienteConsolidado item : listaVentasClienteFull) {
                    // Filtra por el nombre completo del cliente
                    if (item.getNombreCompletoCliente().toLowerCase(Locale.getDefault()).contains(filterPattern)) {
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
            listaVentasCliente.clear();
            listaVentasCliente.addAll((List<VentaClienteConsolidado>) results.values);
            notifyDataSetChanged();
        }
    };

    public static class VentaClienteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreUsuarioCard;
        TextView tvCantidadServiciosCard;
        TextView tvMontoTotalGastadoCard;

        public VentaClienteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreUsuarioCard = itemView.findViewById(R.id.tvNombreUsuarioCard);
            tvCantidadServiciosCard = itemView.findViewById(R.id.tvCantidadServiciosCard);
            tvMontoTotalGastadoCard = itemView.findViewById(R.id.tvMontoTotalGastadoCard);
        }

        public void bind(VentaClienteConsolidado venta) {
            DecimalFormat df = new DecimalFormat("0.00");
            tvNombreUsuarioCard.setText(venta.getNombreCompletoCliente());
            tvCantidadServiciosCard.setText("Servicios Comprados: " + venta.getCantidadTotalServicios());
            tvMontoTotalGastadoCard.setText("Monto Gastado: S/. " + df.format(venta.getMontoTotal()));
        }
    }
}