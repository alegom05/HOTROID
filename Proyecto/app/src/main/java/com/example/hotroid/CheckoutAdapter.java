package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hotroid.bean.CheckoutFirebase;
import java.util.List;
import java.text.SimpleDateFormat; // Para formatear la fecha
import java.util.Locale; // Para el locale en SimpleDateFormat

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
        holder.tvRoomNumber.setText("Habitación: " + current.getRoomNumber()); // Añadir prefijo para claridad

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
                listener.onItemClick(position);
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

    public void actualizarLista(List<CheckoutFirebase> nuevaLista) {
        this.checkoutList = nuevaLista;
        notifyDataSetChanged();
    }

    public static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvClientName, tvRoomNumber, tvCheckoutDate; // Añadido tvCheckoutDate
        ImageView arrowIcon;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
            tvCheckoutDate = itemView.findViewById(R.id.tvCheckoutDate); // Asegúrate de que este ID existe en item_checkout.xml
            arrowIcon = itemView.findViewById(R.id.arrowIcon);
        }
    }
}