package com.example.hotroid.bean;

import java.io.Serializable;

public class TarjetaCredito implements Serializable {
    private boolean estadoTarjeta;
    private long numeroTarjeta;
    private String usuarioId;
    public TarjetaCredito(){}

    public TarjetaCredito(boolean estadoTarjeta, long numeroTarjeta, String usuarioId) {
        this.estadoTarjeta = estadoTarjeta;
        this.numeroTarjeta = numeroTarjeta;
        this.usuarioId = usuarioId;
    }

    public void setEstadoTarjeta(boolean estadoTarjeta) {
        this.estadoTarjeta = estadoTarjeta;
    }

    public void setNumeroTarjeta(long numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public boolean isEstadoTarjeta() {
        return estadoTarjeta;
    }

    public long getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public String getUsuarioId() {
        return usuarioId;
    }
}
