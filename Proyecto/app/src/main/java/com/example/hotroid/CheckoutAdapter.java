package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull; // Asegúrate de que esta importación esté presente
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.CheckoutFirebase; // Importa la nueva clase CheckoutFirebase
import java.util.List; // Usaremos List en lugar de ArrayList para mayor flexibilidad

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder>{

    // Cambiado de ArrayList<Checkout> a List<CheckoutFirebase>
    private List<CheckoutFirebase> checkoutList;
    private OnItemClickListener listener;

    public CheckoutAdapter(List<CheckoutFirebase> checkoutList) { // Cambiado el tipo de parámetro
        this.checkoutList = checkoutList;
    }

    @NonNull // Anotación para indicar que no puede ser null
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Asegúrate de que 'item_checkout' es el nombre correcto de tu archivo XML de diseño para cada elemento del RecyclerView
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout, parent, false);
        return new CheckoutViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) { // Anotación para indicar que no puede ser null
        CheckoutFirebase current = checkoutList.get(position); // Ahora trabaja con CheckoutFirebase

        // Asignación de texto a los TextViews
        holder.tvRoomNumber.setText(current.getRoomNumber()); // Puedes ajustar el formato si lo deseas
        holder.tvClientName.setText(current.getClientName());

        // El arrowIcon ya lo tienes, si quieres que haga algo al hacer clic en él,
        // puedes añadir un setOnClickListener aquí para ese ImageView en particular.
        // holder.arrowIcon.setOnClickListener(v -> { ... });


        // Configuración del click listener para todo el item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Notificamos al listener, pasando la posición del item clicado
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

    // Método para actualizar la lista de datos y notificar al adaptador
    public void actualizarLista(List<CheckoutFirebase> nuevaLista) { // Cambiado el tipo de parámetro
        this.checkoutList = nuevaLista;
        notifyDataSetChanged(); // Notifica al RecyclerView que los datos han cambiado
    }

    public static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomNumber, tvClientName;
        ImageView arrowIcon; // Mantienes el ImageView si tu layout item_checkout lo usa

        public CheckoutViewHolder(@NonNull View itemView) { // Anotación para indicar que no puede ser null
            super(itemView);
            // Asegúrate de que estos IDs existen en tu archivo item_checkout.xml
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            arrowIcon = itemView.findViewById(R.id.arrowIcon); // Si este ID no existe en item_checkout.xml, eliminar esta línea
        }
    }
}