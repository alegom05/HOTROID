package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Necesario para el arrowIcon
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hotroid.bean.CheckoutFirebase;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder>{

    private List<CheckoutFirebase> checkoutList;
    private OnItemClickListener listener;

    public CheckoutAdapter(List<CheckoutFirebase> checkoutList) {
        this.checkoutList = checkoutList;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout, parent, false);
        return new CheckoutViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        CheckoutFirebase current = checkoutList.get(position);

        // Asignación de texto a los TextViews
        holder.tvClientName.setText(current.getClientName());
        holder.tvRoomNumber.setText("Habitación: " + current.getRoomNumber());

        // Formatear la fecha de fin para mostrarla de forma legible
        if (current.getCheckoutDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvCheckoutDate.setText("Fecha Fin: " + sdf.format(current.getCheckoutDate()));
        } else {
            holder.tvCheckoutDate.setText("Fecha Fin: N/A");
        }

        // Configuración del click listener para todo el item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Importante: usar getAdapterPosition() para obtener la posición correcta
                // en caso de que la lista cambie (filtrado, eliminación).
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) { // Asegurarse de que la posición sea válida
                    listener.onItemClick(clickedPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return checkoutList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // Este es el método que usará AdminCheckout para actualizar la lista
    // Ya lo tenías como 'actualizarLista', lo renombro a 'filterList' para consistencia
    // con el AdminCheckout.java que te proporcioné, pero la funcionalidad es la misma.
    public void filterList(List<CheckoutFirebase> filteredList) {
        this.checkoutList = filteredList;
        notifyDataSetChanged();
    }

    public static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvClientName, tvRoomNumber, tvCheckoutDate;
        ImageView arrowIcon; // Incluido para coincidir con tu adaptador

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
            tvCheckoutDate = itemView.findViewById(R.id.tvCheckoutDate);
            arrowIcon = itemView.findViewById(R.id.arrowIcon); // Asegúrate de que este ID existe en item_checkout.xml
        }
    }
}