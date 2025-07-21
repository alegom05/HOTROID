package com.example.hotroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Evento;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

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
        View view = LayoutInflater.from(context).inflate(R.layout.item_evento_card, parent, false);
        return new EventoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        Evento evento = eventosList.get(position);

        // Formatear la fecha y hora
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String fechaHora = sdf.format(evento.getFechaHora().toDate());

        holder.tvEventoTituloCard.setText(evento.getEvento());
        holder.tvFechaCard.setText("Fecha: " + fechaHora);
        holder.tvHotelCard.setText("Hotel: " + evento.getHotel());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventoClick(evento);
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
        TextView tvEventoTituloCard, tvFechaCard, tvHotelCard;

        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventoTituloCard = itemView.findViewById(R.id.tvEventoTituloCard);
            tvFechaCard = itemView.findViewById(R.id.tvFechaCard);
            tvHotelCard = itemView.findViewById(R.id.tvHotelCard);
        }
    }
}