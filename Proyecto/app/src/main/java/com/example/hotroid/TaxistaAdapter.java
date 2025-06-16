package com.example.hotroid;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hotroid.bean.Taxista;

import java.util.List;

public class TaxistaAdapter extends RecyclerView.Adapter<TaxistaAdapter.TaxistaViewHolder> {

    private List<Taxista> lista;
    private Context context;

    public TaxistaAdapter(List<Taxista> lista, Context context) {
        this.lista = lista;
        this.context = context;
    }

    @NonNull
    @Override
    public TaxistaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_taxista, parent, false);
        return new TaxistaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxistaViewHolder holder, int position) {
        Taxista taxista = lista.get(position);

        if (holder.tvNombre != null) {
            holder.tvNombre.setText(taxista.getNombres());
        } else {
            Log.e("TaxistaAdapter", "tvNombre es null en ViewHolder para posición: " + position);
        }

        // --- ¡CORRECCIÓN AQUÍ! Mostrar estadoDeViaje en tvEstado ---
        if (holder.tvEstado != null) {
            holder.tvEstado.setText(taxista.getEstadoDeViaje());
        } else {
            Log.e("TaxistaAdapter", "tvEstado es null en ViewHolder para posición: " + position);
        }
        // -----------------------------------------------------------


        if (holder.imgTaxista != null) {
            if (taxista.getFotoPerfilUrl() != null && !taxista.getFotoPerfilUrl().isEmpty()) {
                Glide.with(context)
                        .load(taxista.getFotoPerfilUrl())
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .into(holder.imgTaxista);
            } else {
                holder.imgTaxista.setImageResource(R.drawable.ic_user_placeholder);
                Log.w("TaxistaAdapter", "fotoPerfilUrl es null o vacía para " + taxista.getNombres());
            }
        } else {
            Log.e("TaxistaAdapter", "imgTaxista es null en ViewHolder para posición: " + position);
        }


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminTaxistaDetalles.class);

            intent.putExtra("taxista_firestore_id", "some_id_if_from_firebase");
            intent.putExtra("taxista_nombres", taxista.getNombres());
            intent.putExtra("taxista_apellidos", taxista.getApellidos());
            intent.putExtra("taxista_tipo_documento", taxista.getTipoDocumento());
            intent.putExtra("taxista_numero_documento", taxista.getNumeroDocumento());
            intent.putExtra("taxista_nacimiento", taxista.getNacimiento());
            intent.putExtra("taxista_correo", taxista.getCorreo());
            intent.putExtra("taxista_telefono", taxista.getTelefono());
            intent.putExtra("taxista_direccion", taxista.getDireccion());
            intent.putExtra("taxista_placa", taxista.getPlaca());
            intent.putExtra("taxista_foto_perfil_url", taxista.getFotoPerfilUrl());
            intent.putExtra("taxista_foto_vehiculo_url", taxista.getFotoVehiculoUrl());
            intent.putExtra("taxista_estado", taxista.getEstado()); // Estado principal (activado, etc.)
            intent.putExtra("taxista_estado_viaje", taxista.getEstadoDeViaje()); // El estado de viaje

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void actualizarLista(List<Taxista> nuevaLista) {
        lista.clear();
        lista.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    static class TaxistaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgTaxista;
        TextView tvNombre;
        TextView tvEstado;

        public TaxistaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTaxista = itemView.findViewById(R.id.imgTaxista);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}