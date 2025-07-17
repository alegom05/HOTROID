package com.example.hotroid.bean;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;

public class Valoracion {
    @DocumentId
    private String idValoracion;
    private String idHotel;
    private String idReserva;
    private String idCliente;
    private Double estrellas;
    private String comentario;
    private Date fecha;

    public Valoracion(String idValoracion, String idHotel, String idReserva, String idCliente, Double estrellas, String comentario, Date fecha) {
        this.idValoracion = idValoracion;
        this.idHotel = idHotel;
        this.idReserva = idReserva;
        this.idCliente = idCliente;
        this.estrellas = estrellas;
        this.comentario = comentario;
        this.fecha = fecha;
    }
    public String getIdValoracion() {
        return idValoracion;
    }

    public void setIdValoracion(String idValoracion) {
        this.idValoracion = idValoracion;
    }

    public String getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(String idHotel) {
        this.idHotel = idHotel;
    }

    public String getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(String idReserva) {
        this.idReserva = idReserva;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public Double getEstrellas() {
        return estrellas;
    }

    public void setEstrellas(Double estrellas) {
        this.estrellas = estrellas;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Valoracion() {
        // Constructor vacío requerido por Firestore
    }

}
