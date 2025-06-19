package com.example.hotroid.bean;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaxiAlertasBeans {
    private String nombresCliente; // Primeros nombres del cliente
    private String apellidosCliente; // Apellidos del cliente
    private String origen;        // El Hotel
    private String destino;       // El Aeropuerto
    private Date timestamp;       // Fecha y hora de creación de la notificación
    private String estadoViaje;   // Estado del viaje (ej. "Asignado", "En camino", "Llegó a destino")

    // Constructor vacío requerido por Firestore
    public TaxiAlertasBeans() {
    }

    // Constructor actualizado para recibir nombres y apellidos por separado Y el estado del viaje
    public TaxiAlertasBeans(String nombresCliente, String apellidosCliente, String origen, String destino, Date timestamp, String estadoViaje) {
        this.nombresCliente = nombresCliente;
        this.apellidosCliente = apellidosCliente;
        this.origen = origen;
        this.destino = destino;
        this.timestamp = timestamp;
        this.estadoViaje = estadoViaje; // Inicializamos el nuevo campo
    }

    // --- Getters ---
    public String getNombresCliente() {
        return nombresCliente;
    }

    public String getApellidosCliente() {
        return apellidosCliente;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return timestamp;
    }

    public String getEstadoViaje() { // Getter para el nuevo campo
        return estadoViaje;
    }

    // --- Setters ---
    public void setNombresCliente(String nombresCliente) {
        this.nombresCliente = nombresCliente;
    }

    public void setApellidosCliente(String apellidosCliente) {
        this.apellidosCliente = apellidosCliente;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setEstadoViaje(String estadoViaje) { // Setter para el nuevo campo
        this.estadoViaje = estadoViaje;
    }

    /**
     * Calcula y retorna el tiempo transcurrido desde el timestamp de la alerta
     * en un formato amigable (ej. "Hace 5 min", "Hace 1 hr y 20 min", "Más de 2 hrs").
     * El tiempo máximo considerado es 2 horas.
     */
    public String getTiempoTranscurrido() {
        if (timestamp == null) {
            return "Tiempo desconocido";
        }

        long diffMillis = System.currentTimeMillis() - timestamp.getTime();
        long minutes = diffMillis / (60 * 1000);

        if (minutes < 1) {
            return "Hace un instante";
        } else if (minutes < 60) {
            return "Hace " + minutes + " min";
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;

            if (hours < 2) {
                if (remainingMinutes > 0) {
                    return "Hace " + hours + " hr y " + remainingMinutes + " min";
                } else {
                    return "Hace " + hours + " hr";
                }
            } else {
                return "Más de 2 hrs";
            }
        }
    }
}
