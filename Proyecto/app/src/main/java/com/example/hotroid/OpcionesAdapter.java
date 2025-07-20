package com.example.hotroid;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.RoomGroupOption;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

public class OpcionesAdapter extends RecyclerView.Adapter<OpcionesAdapter.ViewHolder> {
    private List<RoomGroupOption> listaOpciones;
    private RoomGroupOption opcionSeleccionada;
    private Context context;
    private OnOpcionClickListener listener;

    public interface OnOpcionClickListener {
        void onVerDetalle(RoomGroupOption opcion);

        void onSeleccionar(RoomGroupOption opcion);
    }

    public OpcionesAdapter(List<RoomGroupOption> listaOpciones, Context context, OnOpcionClickListener listener) {
        this.listaOpciones = listaOpciones;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habitacion, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoomGroupOption opcion = listaOpciones.get(position);
        holder.bind(opcion);
    }

    @Override
    public int getItemCount() {
        return listaOpciones.size();
    }

    public RoomGroupOption getOpcionSeleccionada() {
        return opcionSeleccionada;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView roomTypeText, capacityText, roomCountText, priceText, tvHabitaciones;
        private MaterialButton btnSeleccionar;
        private MaterialCardView cardView;
        private View selectionIndicator;
        private ImageView roomImage;
        private ImageButton btnVerDetalle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            roomTypeText = itemView.findViewById(R.id.roomTypeText);
            capacityText = itemView.findViewById(R.id.capacityText);
            roomCountText = itemView.findViewById(R.id.roomCountText);
            priceText = itemView.findViewById(R.id.priceText);
            btnSeleccionar = itemView.findViewById(R.id.selectButton);
            cardView = (MaterialCardView) itemView;
            selectionIndicator = itemView.findViewById(R.id.selectionIndicator);
            roomImage = itemView.findViewById(R.id.roomImage);
            tvHabitaciones = itemView.findViewById(R.id.tvHabitaciones);

//            // Botón "ver detalle" si lo agregas en el layout
            btnVerDetalle = itemView.findViewById(R.id.btnVerDetalle);
//            btnVerDetalle = new MaterialButton(itemView.getContext());
//            btnVerDetalle.setText("Ver detalle");
//            btnVerDetalle.setTextSize(12);
//            btnVerDetalle.setCornerRadius(20);
//            btnVerDetalle.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cliente1)));
//            btnVerDetalle.setTextColor(Color.WHITE);

            // Puedes añadirlo dinámicamente al layout si no existe en XML
            //((LinearLayout) cardView.findViewById(R.id.amenitiesLayout)).addView(btnVerDetalle);
        }

        public void bind(RoomGroupOption opcion) {
            roomTypeText.setText(opcion.getRoomType());
            // Capacidad
            String capacidad = opcion.getTotalAdults() + " adulto" + (opcion.getTotalAdults() > 1 ? "s" : "");
            if (opcion.getTotalChildren() > 0) {
                capacidad += ", " + opcion.getTotalChildren() + " niño" + (opcion.getTotalChildren() > 1 ? "s" : "");
            }
            capacityText.setText(capacidad);
            // Habitaciones disponibles
//            String habitaciones = opcion.getHabitacionesNecesarias() + " habitación" +
//                    (opcion.getHabitacionesNecesarias() > 1 ? "es" : "") + " (" +
//                    opcion.getDisponibles() + " disponible" + (opcion.getDisponibles() > 1 ? "s" : "") + ")";
//            roomCountText.setText(habitaciones);

            // Habitaciones disponibles
            String habitaciones = opcion.getDisponibles() + " habitación" + (opcion.getDisponibles() > 1 ? "es" : "") + " disponible" + (opcion.getDisponibles() > 1 ? "s" : "");
            roomCountText.setText(habitaciones);
            // Habitaciones requeridas
            String requeridas = "Se requieren " + opcion.getHabitacionesNecesarias() + " habitación" + (opcion.getHabitacionesNecesarias() > 1 ? "es" : "");
            tvHabitaciones.setText(requeridas);
            // Precio
            double precioTotal = opcion.getPrecioPorHabitacion() * opcion.getHabitacionesNecesarias();
            priceText.setText(String.format(Locale.getDefault(), "S/. %.2f", precioTotal));
            // Imagen de tipo habitación
            setRoomImage(opcion.getRoomType());
            // Estado de selección
            updateSelectionState(opcion.equals(opcionSeleccionada));
            // Listeners
            btnSeleccionar.setOnClickListener(v -> seleccionar(opcion));
            cardView.setOnClickListener(v -> seleccionar(opcion));
            btnVerDetalle.setOnClickListener(v -> listener.onVerDetalle(opcion));
            ;
        }

        private void seleccionar(RoomGroupOption opcion) {
            int anterior = listaOpciones.indexOf(opcionSeleccionada);
            opcionSeleccionada = opcion;
            notifyItemChanged(anterior);
            notifyItemChanged(getAdapterPosition());

            if (listener != null) {
                listener.onSeleccionar(opcion);
            }
        }

        private void updateSelectionState(boolean isSelected) {
            if (isSelected) {
                cardView.setStrokeWidth(4);
                cardView.setStrokeColor(ContextCompat.getColor(context, R.color.cliente1));
                selectionIndicator.setVisibility(View.VISIBLE);
                btnSeleccionar.setText("Seleccionado");
                btnSeleccionar.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue_status)));
            } else {
                cardView.setStrokeWidth(0);
                selectionIndicator.setVisibility(View.GONE);
                btnSeleccionar.setText("Seleccionar");
                btnSeleccionar.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cliente1)));
            }
        }

        private void setRoomImage(String roomType) {
            int imageResource;
            switch (roomType.toLowerCase()) {
                case "estándar":
                case "standard":
                    imageResource = R.drawable.hotel_room;
                    break;
                case "suite":
                    imageResource = R.drawable.hotel_room_deluxe;
                    break;
                case "deluxe":
                    imageResource = R.drawable.hotel_room_doble;
                    break;
                case "familiar":
                case "family":
                    imageResource = R.drawable.hotel_room;
                    break;
                default:
                    imageResource = R.drawable.hotel_room;
                    break;
            }
            roomImage.setImageResource(imageResource);
        }

    }
}
