package com.example.hotroid;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.ReservaItemAdapter;
import com.example.hotroid.bean.Hotel;
import com.example.hotroid.bean.ReservaConHotel;
import com.example.hotroid.bean.Room;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ReservasPagerAdapterUser extends RecyclerView.Adapter<ReservasPagerAdapterUser.ReservasPagerViewHolder> {

    private final Context context;
    private final List<List<ReservaConHotel>> listasPorEstado; // [0]=Activas, [1]=Pasadas, [2]=Canceladas
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Listas filtradas por estado
    private List<ReservaConHotel> reservasActivas;
    private List<ReservaConHotel> reservasPasadas;
    private List<ReservaConHotel> reservasCanceladas;


    public ReservasPagerAdapterUser(Context context, List<List<ReservaConHotel>> listasPorEstado) {
        this.context = context;
        this.listasPorEstado = listasPorEstado;
        // Inicializar las listas filtradas
//        filtrarReservasPorEstado();
    }

    @NonNull
    @Override
    public ReservasPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_content_reservas, parent, false);
        return new ReservasPagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservasPagerViewHolder holder, int position) {
        List<ReservaConHotel> reservasActuales = listasPorEstado.get(position);
//        List<ReservaConHotel> reservasActuales = getReservasPorTab(position);


        if (reservasActuales.isEmpty()) {
            // Mostrar mensaje de que no hay reservas
            holder.recyclerView.setVisibility(View.GONE);
            holder.mensajeVacio.setVisibility(View.VISIBLE);

            switch (position) {
                case 0:
                    holder.mensajeVacio.setText("No tienes reservas activas.");
                    break;
                case 1:
                    holder.mensajeVacio.setText("No tienes reservas pasadas.");
                    break;
                case 2:
                    holder.mensajeVacio.setText("No tienes reservas canceladas.");
                    break;
            }
        } else {
            // Mostrar lista de reservas
            holder.recyclerView.setVisibility(View.VISIBLE);
            holder.mensajeVacio.setVisibility(View.GONE);
            // Configurar RecyclerView
            ReservaItemAdapter adapter = new ReservaItemAdapter(context, reservasActuales);
            holder.recyclerView.setAdapter(adapter);
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
            // Cargar datos de hoteles si es necesario
            loadHotelDataForReservas(reservasActuales, adapter);
        }
    }
    @Override
    public int getItemCount() {
        return 3; // Siempre 3 tabs: Activas, Pasadas, Canceladas
    }

    /**
     * ViewHolder para cada tab
     */
    static class ReservasPagerViewHolder extends RecyclerView.ViewHolder {
        final RecyclerView recyclerView;
        final MaterialTextView mensajeVacio;

        public ReservasPagerViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.rvReservas);
            mensajeVacio = itemView.findViewById(R.id.tvMensajeVacio);
        }
    }

//    /**
//     * Filtra las reservas por estado para cada tab
//     */
//    private void filtrarReservasPorEstado() {
//        reservasActivas = new ArrayList<>();
//        reservasPasadas = new ArrayList<>();
//        reservasCanceladas = new ArrayList<>();
//
//        for (ReservaConHotel reserva : todasLasReservas) {
//            String estado = reserva.getReserva().getEstado().toLowerCase();
//
//            switch (estado) {
//                case "activo":
//                case "confirmada":
//                case "activa":
//                    reservasActivas.add(reserva);
//                    break;
//
//                case "pasado":
//                case "completada":
//                case "finalizada":
//                    reservasPasadas.add(reserva);
//                    break;
//
//                case "cancelada":
//                case "cancelado":
//                    reservasCanceladas.add(reserva);
//                    break;
//
//                default:
//                    // Por defecto, considerar como activa si no coincide
//                    reservasActivas.add(reserva);
//                    break;
//            }
//        }
//    }

    /**
     * Obtiene la lista de reservas según la posición del tab
     * @param position 0 = Activas, 1 = Pasadas, 2 = Canceladas
     * @return Lista correspondiente al tab
     */
    private List<ReservaConHotel> getReservasPorTab(int position) {
        switch (position) {
            case 0: return reservasActivas;   // Tab "Activas"
            case 1: return reservasPasadas;   // Tab "Pasadas"
            case 2: return reservasCanceladas; // Tab "Canceladas"
            default: return new ArrayList<>();
        }
    }

    /**
     * Carga los datos del hotel desde Firestore para las reservas que no los tengan
     */
    private void loadHotelDataForReservas(List<ReservaConHotel> reservas, ReservaItemAdapter adapter) {
        for (int i = 0; i < reservas.size(); i++) {
            ReservaConHotel reserva = reservas.get(i);

            // Solo cargar si no tiene datos del hotel
            if (reserva.getHotel() == null && reserva.getReserva().getIdHotel() != null) {
                final int position = i;

                db.collection("hoteles")
                        .document(reserva.getReserva().getIdHotel())
                        .get()
                        .addOnSuccessListener(hotelDoc -> {
                            if (hotelDoc.exists()) {
                                Hotel hotel = hotelDoc.toObject(Hotel.class);
                                if (hotel != null) {
                                    hotel.setIdHotel(hotelDoc.getId());
                                    reserva.setHotel(hotel);

                                    // Actualizar solo este item específico
                                    adapter.notifyItemChanged(position);

                                    // Opcionalmente, cargar datos de la habitación
                                    loadHabitacionData(reserva, adapter, position);
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ReservasPager", "Error cargando hotel", e);
                        });
            }
        }
    }

//    /**
//     * Actualiza todas las listas cuando cambian los datos
//     */
//    public void actualizarDatos(List<ReservaConHotel> nuevasReservas) {
//        this.listasPorEstado.clear();
//        this.listasPorEstado.addAll(nuevasReservas);
//        filtrarReservasPorEstado();
//        notifyDataSetChanged();
//    }

//    /**
//     * Actualiza una reserva específica (útil para cambios de estado)
//     */
//    public void actualizarReserva(ReservaConHotel reservaActualizada) {
//        // Buscar y actualizar en la lista principal
//        for (int i = 0; i < todasLasReservas.size(); i++) {
//            if (todasLasReservas.get(i).getReserva().getIdReserva()
//                    .equals(reservaActualizada.getReserva().getIdReserva())) {
//                todasLasReservas.set(i, reservaActualizada);
//                break;
//            }
//        }
//
//        // Re-filtrar y actualizar
//        filtrarReservasPorEstado();
//        notifyDataSetChanged();
//    }

    /**
     * Obtiene el conteo de reservas por tab
     */
    public int getConteoReservas(int tabPosition) {
        if (tabPosition >= 0 && tabPosition < listasPorEstado.size()) {
            return listasPorEstado.get(tabPosition).size();
        }
        return 0;
    }

    /**
     * Carga los datos de la habitación desde Firestore
     */
    private void loadHabitacionData(ReservaConHotel reserva, ReservaItemAdapter adapter, int position) {
        if (reserva.getReserva().getIdHotel() == null) return;

        db.collection("habitaciones")
                .whereEqualTo("hotelId", reserva.getReserva().getIdHotel())
                .whereEqualTo("roomNumber", reserva.getReserva().getRoomNumber())
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Room habitacion = querySnapshot.getDocuments().get(0).toObject(Room.class);
                        if (habitacion != null) {
                            habitacion.setId(querySnapshot.getDocuments().get(0).getId());
                            reserva.setHabitacion(habitacion);
                            adapter.notifyItemChanged(position);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ReservasPager", "Error cargando habitación", e);
                });
    }


}