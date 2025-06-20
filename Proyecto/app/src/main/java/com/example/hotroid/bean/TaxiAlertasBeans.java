package com.example.hotroid.bean;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaxiAlertasBeans {
    private String documentId;
    private String nombresCliente;
    private String apellidosCliente;
    private String origen;
    private String destino;
    private Date timestamp;
    private String estadoViaje;
    private String region;

    public TaxiAlertasBeans() {
    }

    public TaxiAlertasBeans(String nombresCliente, String apellidosCliente, String origen, String destino, Date timestamp, String estadoViaje, String region) {
        this.nombresCliente = nombresCliente;
        this.apellidosCliente = apellidosCliente;
        this.origen = origen;
        this.destino = destino;
        this.timestamp = timestamp;
        this.estadoViaje = estadoViaje;
        this.region = region;
    }

    public TaxiAlertasBeans(String documentId, String nombresCliente, String apellidosCliente, String origen, String destino, Date timestamp, String estadoViaje, String region) {
        this.documentId = documentId;
        this.nombresCliente = nombresCliente;
        this.apellidosCliente = apellidosCliente;
        this.origen = origen;
        this.destino = destino;
        this.timestamp = timestamp;
        this.estadoViaje = estadoViaje;
        this.region = region;
    }

    public String getDocumentId() {
        return documentId;
    }

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

    public String getEstadoViaje() {
        return estadoViaje;
    }

    public String getRegion() {
        return region;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

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

    public void setEstadoViaje(String estadoViaje) {
        this.estadoViaje = estadoViaje;
    }

    public void setRegion(String region) {
        this.region = region;
    }

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