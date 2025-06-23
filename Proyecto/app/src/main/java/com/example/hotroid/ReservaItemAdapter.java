package com.example.hotroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.R;
import com.example.hotroid.bean.ReservaConHotel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReservaItemAdapter extends RecyclerView.Adapter<ReservaItemAdapter.ReservaViewHolder> {

    private final List<ReservaConHotel> reservas;
    private final Context context;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public ReservaItemAdapter(Context context, List<ReservaConHotel> reservas) {
        this.context = context;
        this.reservas = reservas;
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_reserva, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        ReservaConHotel item = reservas.get(position);

        // Mostrar datos del hotel
        holder.tvNombreHotel.setText(item.getReserva().getNombreHotel());
        holder.tvHabitacion.setText("Habitación: " + item.getReserva().getRoomNumber());

        // Mostrar datos de la reserva
        String fechaInicio = dateFormat.format(item.getReserva().getFechaInicio());
        String fechaFin = dateFormat.format(item.getReserva().getFechaFin());
        holder.tvFechas.setText("Desde: " + fechaInicio + "\nHasta: " + fechaFin);

        // Mostrar huéspedes
        String huespedesText = item.getReserva().getAdultos() + " adultos";
        if (item.getReserva().getNinos() > 0) {
            huespedesText += ", " + item.getReserva().getNinos() + " niños";
        }
        holder.tvHuespedes.setText(huespedesText);

        // Mostrar precio
        holder.tvPrecio.setText(String.format("PEN %.2f", item.getReserva().getPrecioTotal()));

        // Configurar imagen del hotel
        if (item.getHotel() != null) {
            holder.imagenHotel.setImageResource(item.getHotel().getImageResourceId());
        } else {
            // Imagen por defecto si no hay hotel
            holder.imagenHotel.setImageResource(R.drawable.hotel_placeholder);
        }

        // Configurar botón según estado
        if (item.getReserva().getEstado().equals("activo")) {
            holder.btnAccion.setText("Cancelar reserva");
            holder.btnAccion.setBackgroundTintList(context.getColorStateList(R.color.cliente2));
            holder.btnAccion.setVisibility(View.VISIBLE);
        } else if (item.getReserva().getEstado().equals("pasado")) {
            holder.btnAccion.setText("Valorar estancia");
            holder.btnAccion.setBackgroundTintList(context.getColorStateList(R.color.cliente1));
            holder.btnAccion.setVisibility(item.getReserva().isTieneValoracion() ? View.GONE : View.VISIBLE);
        } else {
            holder.btnAccion.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardView;
        final ShapeableImageView imagenHotel;
        final TextView tvNombreHotel, tvHabitacion, tvFechas, tvHuespedes, tvPrecio;
        final MaterialButton btnAccion;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewReserva);
            imagenHotel = itemView.findViewById(R.id.imagenHotel);
            tvNombreHotel = itemView.findViewById(R.id.tvNombreHotel);
            tvHabitacion = itemView.findViewById(R.id.tvHabitacion);
            tvFechas = itemView.findViewById(R.id.tvFechas);
            tvHuespedes = itemView.findViewById(R.id.tvHuespedes);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            btnAccion = itemView.findViewById(R.id.btnAccion);
        }
    }
}