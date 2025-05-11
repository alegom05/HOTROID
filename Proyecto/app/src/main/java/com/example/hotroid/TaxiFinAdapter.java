package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class TaxiFinAdapter extends RecyclerView.Adapter<TaxiFinAdapter.ViewHolder> {

    private final List<ViajeFinBeans> listaViajes;

    public TaxiFinAdapter(List<ViajeFinBeans> listaViajes) {
        this.listaViajes = listaViajes;
    }

    @NonNull
    @Override
    public TaxiFinAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.taxi_fin_item, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxiFinAdapter.ViewHolder holder, int position) {
        ViajeFinBeans viaje = listaViajes.get(position);

        holder.tvNombre.setText(viaje.getNombre());
        holder.tvHorario.setText(viaje.getHorario());
        holder.tvRol.setText(viaje.getRol());

        // Si manejas imágenes dinámicas, aquí podrías usar Glide o Picasso
        holder.imgUsuario.setImageResource(viaje.getImagenResId());
    }

    @Override
    public int getItemCount() {
        return listaViajes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvHorario, tvRol;
        ImageView imgUsuario;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvHorario = itemView.findViewById(R.id.tvHorario);
            tvRol = itemView.findViewById(R.id.tvRol);
            imgUsuario = itemView.findViewById(R.id.imgUsuario);
        }
    }
}
