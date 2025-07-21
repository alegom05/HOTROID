package com.example.hotroid;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

    // Método auxiliar para obtener información de huéspedes
    private String getGuestsInfo(ReservaConHotel item) {
        String info = item.getReserva().getAdultos() + " adultos";
        if (item.getReserva().getNinos() > 0) {
            info += ", " + item.getReserva().getNinos() + " niños";
        }else {
            info += ", 0 niños";
        }
        return info;
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        ReservaConHotel item = reservas.get(position);
        // Configurar nombre del hotel
        if (item.getHotel() != null) {
            holder.tvNombreHotel.setText(item.getHotel().getName());

            // Cargar imagen del hotel
            List<String> urls = item.getHotel().getImageUrls();
            // Cargar imagen del hotel
            if (urls != null && !urls.isEmpty()) {
                Glide.with(context)
                        .load(urls.get(0))
                        .placeholder(R.drawable.placeholder_hotel)
                        .error(R.drawable.ic_hotel_error)
                        .centerCrop()
                        .into(holder.imagenHotel);
            } else {
                holder.imagenHotel.setImageResource(R.drawable.hotel_placeholder);
            }
        } else {
            holder.tvNombreHotel.setText("Hotel no disponible");
            holder.imagenHotel.setImageResource(R.drawable.placeholder_hotel);
        }
        // Configurar número de habitación
        holder.tvHabitacion.setText("Habitación: " + item.getReserva().getRoomNumber());

        // Mostrar fechas de la reserva
        String fechaInicio = dateFormat.format(item.getReserva().getFechaInicio());
        String fechaFin = dateFormat.format(item.getReserva().getFechaFin());
        holder.tvFechas.setText("Desde: " + fechaInicio + "\nHasta: " + fechaFin);
//        String fechas = "Desde: " + dateFormat.format(item.getReserva().getFechaInicio()) +
//                "\nHasta: " + dateFormat.format(item.getReserva().getFechaFin());
//        holder.tvFechas.setText(fechas);

        // Mostrar huéspedes usando el método auxiliar
        holder.tvHuespedes.setText(getGuestsInfo(item));

        // Mostrar precio
        holder.tvPrecio.setText(String.format(Locale.getDefault(), "S/. %.2f",
                item.getReserva().getPrecioTotal()));

//        // Configurar imagen del hotel
//        if (item.getHotel() != null) {
//            holder.imagenHotel.setImageResource(item.getHotel().getImageResourceId());
//        } else {
//            // Imagen por defecto si no hay hotel
//            holder.imagenHotel.setImageResource(R.drawable.hotel_placeholder);
//        }

        // Eliminar cualquier onClick listener previamente establecido
        holder.btnAccion.setOnClickListener(null);

        // Configurar botón según estado
        if (item.getReserva().getEstado().equals("activo")) {
            holder.btnAccion.setText("Detalles");  // Cambiar texto del botón
            holder.btnAccion.setBackgroundTintList(context.getColorStateList(R.color.cliente2));
            holder.btnAccion.setVisibility(View.VISIBLE);

            // Configurar el OnClickListener para abrir DetalleReservaActivo
            holder.btnAccion.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(context, DetalleReservaActivo.class);

                    // Enviamos los datos necesarios para DetalleReservaActivo
                    intent.putExtra("hotel_name", item.getReserva().getNombreHotel());

                    // Si hay información de ubicación del hotel, la incluimos
                    if (item.getHotel() != null) {
                        String ciudad = item.getHotel().getDireccion();
                        intent.putExtra("city", ciudad != null ? ", " + ciudad : "");
                        intent.putExtra("hotel_location", item.getHotel().getDireccion());
                    } else {
                        intent.putExtra("city", "");
                        intent.putExtra("hotel_location", "");
                    }

                    // Información de la habitación
                    intent.putExtra("room_details", "Habitación: " + item.getReserva().getRoomNumber());

                    // Estado, fechas y código de reserva
                    intent.putExtra("estado", "activo");
                    intent.putExtra("checkInDate", fechaInicio);
                    intent.putExtra("checkOutDate", fechaFin);
                    intent.putExtra("reservationCode", item.getReserva().getIdReserva());

                    // Usar el método auxiliar para la información de huéspedes
                    intent.putExtra("guestsInfo", getGuestsInfo(item));

                    // Iniciamos la actividad
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "Error al abrir detalles: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            });
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