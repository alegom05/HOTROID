package com.example.hotroid.bean;

public class Reserva {
    private String idReserva; //id de la reserva
    private String idPersona; //id de la persona que reserva
    private String idHotel; //id del hotel al que se reservó

    private int habitaciones; //cantidad de habitaciones reservadas
    private int adultos; //integer usar valor por defecto "null", mientras que int usar "0"
    private int ninos;
    private String fechaInicio;
    private String fechaFin;
    private String estado; // "activo", "pasado", "cancelado"
//    private Integer imagenResId; // para drawable local
    private int precioTotal;


    public Reserva(String idReserva, String idHotel, String idPersona, int habitaciones, int adultos, int ninos,String fechaInicio, String fechaFin, String estado, int precioTotal) {
        this.idReserva=idReserva;
        this.idHotel = idHotel;
        this.idPersona = idPersona;
        this.habitaciones = habitaciones;
        this.adultos = adultos;
        this.ninos = ninos;
        this.fechaInicio=fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        //this.imagenResId = imagenResId;
        this.precioTotal = precioTotal;
    }

    public String getIdReserva() {
        return idReserva;
    }

    public String getIdPersona() {
        return idPersona;
    }

    public String getIdHotel() {
        return idHotel;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public int getPrecioTotal() {
        return precioTotal;
    }

    public int getHabitaciones() {
        return habitaciones;
    }

    public int getAdultos() {
        return adultos;
    }

    public int getNinos() {
        return ninos;
    }

    public String getEstado() {
        return estado;
    }

//    public Integer getImagenResId() {
//        return imagenResId;
//    }

}
