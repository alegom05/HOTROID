package com.example.hotroid;

import android.content.Context;
import android.content.Intent; // Importar Intent para la navegación
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hotroid.bean.TaxiAlertasBeans; // Asegúrate que el paquete de TaxiAlertasBeans sea correcto
import java.util.List;

public class TaxiAlertasAdapter extends RecyclerView.Adapter<TaxiAlertasAdapter.ViewHolder> {

    private List<TaxiAlertasBeans> listaAlertas;
    private Context context; // Contexto de la actividad que usa el adaptador

    // Constructor que recibe el Context y la lista de alertas
    public TaxiAlertasAdapter(Context context, List<TaxiAlertasBeans> listaAlertas) {
        this.context = context;
        this.listaAlertas = listaAlertas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el layout 'item_taxi_alerta.xml' para cada ítem de la lista.
        // Este layout contiene el diseño de la tarjeta de notificación individual.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.taxi_alertas_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtenemos el objeto TaxiAlertasBeans en la posición actual
        TaxiAlertasBeans alerta = listaAlertas.get(position);

        // Concatenamos Nombres y Apellidos para mostrar un nombre completo en el TextView
        String nombreCompletoCliente = alerta.getNombresCliente() + " " + alerta.getApellidosCliente();
        holder.tvNombreNoti.setText(nombreCompletoCliente);

        // Asignamos el Origen y Destino
        holder.tvLugarNoti.setText("Origen: " + alerta.getOrigen());
        holder.tvLugarNoti2.setText("Destino: " + alerta.getDestino());

        // Obtenemos el tiempo transcurrido de forma dinámica usando el método del bean
        holder.tvTiempoNoti.setText(alerta.getTiempoTranscurrido());

        // Configuramos un OnClickListener para todo el ítem (la CardView).
        // Al hacer clic, se navegará a la actividad TaxiViaje.
        holder.itemView.setOnClickListener(v -> {
            // Creamos un Intent para especificar la actividad de destino
            Intent intent = new Intent(context, TaxiViaje.class);

            // Pasamos los datos relevantes de la notificación a la actividad TaxiViaje
            // Esto permite que TaxiViaje muestre los detalles del viaje específico.
            intent.putExtra("nombreCliente", nombreCompletoCliente); // Ya concatenado para mostrar
            intent.putExtra("origen", alerta.getOrigen());
            intent.putExtra("destino", alerta.getDestino());
            // Es crucial pasar el timestamp para que TaxiViaje pueda calcular el tiempo si es necesario
            intent.putExtra("timestamp", alerta.getTimestamp().getTime()); // Pasar el Date como long (milisegundos)

            // Iniciamos la nueva actividad
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        // Retorna el número total de elementos en la lista de notificaciones
        return listaAlertas.size();
    }

    /**
     * ViewHolder para las vistas de cada ítem del RecyclerView.
     * Contiene las referencias a los componentes de la UI definidos en 'item_taxi_alerta.xml'.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreNoti;
        TextView tvLugarNoti;
        TextView tvLugarNoti2;
        TextView tvTiempoNoti;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inicializamos las referencias a los TextViews buscando por su ID
            tvNombreNoti = itemView.findViewById(R.id.tvNombreNoti);
            tvLugarNoti = itemView.findViewById(R.id.tvLugarNoti);
            tvLugarNoti2 = itemView.findViewById(R.id.tvLugarNoti2);
            tvTiempoNoti = itemView.findViewById(R.id.tvTiempoNoti);
        }
    }

    /**
     * Método para actualizar la lista de notificaciones y notificar al adaptador de los cambios.
     * Esto es útil para la funcionalidad del buscador, que filtra la lista.
     * @param newAlertasList La nueva lista de objetos TaxiAlertasBeans (filtrada o completa).
     */
    public void updateList(List<TaxiAlertasBeans> newAlertasList) {
        this.listaAlertas = newAlertasList;
        // Notifica al RecyclerView que los datos han cambiado por completo,
        // lo que lo lleva a redibujar toda la lista.
        notifyDataSetChanged();
    }
}