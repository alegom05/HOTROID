package com.example.hotroid;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.hotroid.bean.Reserva;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ReservaService {
    private static final String TAG = "ReservaService";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public ReservaService() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    /**
     * Crea una nueva reserva en Firestore
     * @param reserva La reserva a crear
     * @param onSuccess Callback para éxito
     * @param onFailure Callback para error
     */
    public void crearReserva(Reserva reserva, Runnable onSuccess, Consumer<Exception> onFailure) {

            // Generar un ID único
            String id = db.collection("reservas").document().getId();
            reserva.setIdReserva(id);

        // Guardar en Firestore
        db.collection("reservas")
                .document(id)
                .set(reserva)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Reserva creada con éxito");
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al crear reserva", e);
                    onFailure.accept(e);
                });
    }

    /**
     * Obtiene las reservas del usuario actual
     * @param onSuccess Callback con la lista de reservas
     * @param onFailure Callback para error
     */
    public void obtenerReservasUsuario(Consumer<List<Reserva>> onSuccess, Consumer<Exception> onFailure) {
        if (auth.getCurrentUser() == null) {
            onFailure.accept(new Exception("Usuario no autenticado"));
            return;
        }

        String idUsuario = auth.getCurrentUser().getUid();

        db.collection("reservas")
                .whereEqualTo("idPersona", idUsuario)
                .orderBy("fechaInicio", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Reserva> reservas = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Reserva reserva = document.toObject(Reserva.class);
                            // ¡AQUÍ ASIGNAS el ID del documento!
                            reserva.setIdReserva(document.getId());
                            reservas.add(reserva);
                        }
                        onSuccess.accept(reservas);
                    } else {
                        onFailure.accept(task.getException());
                    }
                });
    }

    /**
     * Cancela una reserva existente
     * @param idReserva ID de la reserva a cancelar
     * @param onSuccess Callback para éxito
     * @param onFailure Callback para error
     */
    public void cancelarReserva(String idReserva, Runnable onSuccess, Consumer<Exception> onFailure) {
        DocumentReference reservaRef = db.collection("reservas").document(idReserva);

        // Actualizar solo los campos necesarios para cancelación
        reservaRef.update(
                        "estado", "cancelado",
                        "cancelada", true,
                        "fechaCancelacion", new java.util.Date()
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Reserva cancelada con éxito");
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al cancelar reserva", e);
                    onFailure.accept(e);
                });
    }

    /**
     * Registra una valoración para una reserva
     * @param idReserva ID de la reserva
     * @param idValoracion ID de la valoración generada
     * @param onSuccess Callback para éxito
     * @param onFailure Callback para error
     */
    public void registrarValoracion(String idReserva, String idValoracion,
                                    Runnable onSuccess, Consumer<Exception> onFailure) {
        DocumentReference reservaRef = db.collection("reservas").document(idReserva);

        reservaRef.update(
                        "idValoracion", idValoracion,
                        "tieneValoracion", true
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Valoración registrada con éxito");
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al registrar valoración", e);
                    onFailure.accept(e);
                });
    }
}