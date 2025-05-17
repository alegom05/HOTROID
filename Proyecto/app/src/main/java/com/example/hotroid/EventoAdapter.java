package com.example.hotroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventoAdapter extends RecyclerView.Adapter<EventoAdapter.EventoViewHolder> {

    private Context context;
    private List<Evento> listaEventos; // Cambiado a List<Evento>
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Evento evento); // Cambiado a Evento evento
    }

    public EventoAdapter(Context context, List<Evento> listaEventos, OnItemClickListener listener) { // Cambiado a List<Evento>
        this.context = context;
        this.listaEventos = listaEventos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.super_eventos_item, parent, false);
        return new EventoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        Evento evento = listaEventos.get(position); // Cambiado a Evento evento
        holder.tvFechaEvento.setText(evento.getFecha());
        holder.tvEvento.setText(evento.getEvento());
        holder.tvHotelEvento.setText(evento.getHotel());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(evento);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaEventos.size();
    }

    public void actualizarLista(List<Evento> nuevaLista) { // Cambiado a List<Evento>
        this.listaEventos = nuevaLista;
        notifyDataSetChanged();
    }

    static class EventoViewHolder extends RecyclerView.ViewHolder {
        TextView tvFechaEvento;
        TextView tvEvento;
        TextView tvHotelEvento;

        EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFechaEvento = itemView.findViewById(R.id.tvFechaEvento);
            tvEvento = itemView.findViewById(R.id.tvEvento);
            tvHotelEvento = itemView.findViewById(R.id.tvHotelEvento);
        }
    }
}