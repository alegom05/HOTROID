package com.example.hotroid; // Make sure this matches your package name

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Evento;

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
        // Inflar el layout de la tarjeta de evento detallada
        View view = LayoutInflater.from(context).inflate(R.layout.item_evento_card, parent, false);
        return new EventoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        Evento currentEvento = eventosList.get(position);

        // Establecer los datos en los TextViews de la tarjeta
        holder.tvFechaCard.setText("Fecha: " + currentEvento.getFecha());
        holder.tvEventoTituloCard.setText(currentEvento.getEvento());
        holder.tvHotelCard.setText("Hotel: " + currentEvento.getHotel());

        // Para la descripción, puedes mostrarla completa o truncarla si es muy larga
        String descripcion = currentEvento.getDescripcion();
        if (descripcion != null && descripcion.length() > 100) { // Ejemplo: truncar si es mayor a 100 caracteres
            descripcion = descripcion.substring(0, 97) + "...";
        } else if (descripcion == null) {
            descripcion = ""; // Asegurarse de que no sea null
        }
        holder.tvDescripcionCard.setText(descripcion);


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
        // Declarar los TextViews que corresponden a item_evento_card.xml
        TextView tvFechaCard;
        TextView tvEventoTituloCard;
        TextView tvHotelCard;
        TextView tvDescripcionCard;

        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Enlazar los TextViews con sus IDs en item_evento_card.xml
            tvFechaCard = itemView.findViewById(R.id.tvFechaCard);
            tvEventoTituloCard = itemView.findViewById(R.id.tvEventoTituloCard);
            tvHotelCard = itemView.findViewById(R.id.tvHotelCard);
            tvDescripcionCard = itemView.findViewById(R.id.tvDescripcionCard);
        }
    }
}