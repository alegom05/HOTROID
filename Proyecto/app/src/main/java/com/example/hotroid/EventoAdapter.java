package com.example.hotroid; // Make sure this matches your package name

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
    private List<Evento> eventosList;
    private OnEventoClickListener listener;

    public interface OnEventoClickListener {
        void onEventoClick(Evento evento);
    }

    public EventoAdapter(Context context, List<Evento> eventosList, OnEventoClickListener listener) {
        this.context = context;
        this.eventosList = eventosList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout which now only has two TextViews
        View view = LayoutInflater.from(context).inflate(R.layout.super_eventos_item, parent, false);
        return new EventoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        Evento currentEvento = eventosList.get(position);

        holder.tvFechaItem.setText(currentEvento.getFecha());

        // *************** CRITICAL CHANGE HERE ***************
        // Concatenate the event description and the hotel name
        String combinedEventoText = currentEvento.getEvento() + " en el hotel " + currentEvento.getHotel();
        holder.tvEventoItem.setText(combinedEventoText);
        // ****************************************************

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventoClick(currentEvento);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventosList.size();
    }

    public void actualizarLista(List<Evento> nuevaLista) {
        this.eventosList = nuevaLista;
        notifyDataSetChanged();
    }

    public static class EventoViewHolder extends RecyclerView.ViewHolder {
        TextView tvFechaItem;
        TextView tvEventoItem; // Only one TextView for the combined event description

        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFechaItem = itemView.findViewById(R.id.tvFechaItem);
            tvEventoItem = itemView.findViewById(R.id.tvEventoItem);
            // Ensure these IDs match your super_lista_eventos_item.xml
        }
    }
}