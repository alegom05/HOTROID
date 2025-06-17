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
    private int precioTotal;

    // Atributos adicionales basados en la imagen/requerimientos
    private boolean checkInRealizado;
    private boolean checkOutRealizado;
    private int cobros_adicionales;
    private boolean estaCancelado;
    private String fechaCancelacion; // Puede ser null
    private String idValoracion; // Puede ser null
    private String roomNumber;
    private boolean tieneValoracion;


    // Constructor completo con todos los 18 parámetros
    public Reserva(String idReserva, String idHotel, String idPersona, int habitaciones, int adultos, int ninos,
                   String fechaInicio, String fechaFin, String estado, int precioTotal,
                   boolean checkInRealizado, boolean checkOutRealizado, int cobros_adicionales,
                   boolean estaCancelado, String fechaCancelacion, String idValoracion, String roomNumber, boolean tieneValoracion) {
        this.idReserva = idReserva;
        this.idHotel = idHotel;
        this.idPersona = idPersona;
        this.habitaciones = habitaciones;
        this.adultos = adultos;
        this.ninos = ninos;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.precioTotal = precioTotal;
        this.checkInRealizado = checkInRealizado;
        this.checkOutRealizado = checkOutRealizado;
        this.cobros_adicionales = cobros_adicionales;
        this.estaCancelado = estaCancelado;
        this.fechaCancelacion = fechaCancelacion;
        this.idValoracion = idValoracion;
        this.roomNumber = roomNumber;
        this.tieneValoracion = tieneValoracion;
    }

    // Si también necesitas un constructor más corto (como la Opción 2), agrégalo aquí:
    /*
    public Reserva(String idReserva, String idHotel, String idPersona, int habitaciones, int adultos, int ninos,
                   String fechaInicio, String fechaFin, String estado, int precioTotal) {
        this(idReserva, idHotel, idPersona, habitaciones, adultos, ninos, fechaInicio, fechaFin, estado, precioTotal,
             false, false, 0, false, null, null, null, false);
    }
    */


    // Getters para todos los atributos
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

    public boolean isCheckInRealizado() {
        return checkInRealizado;
    }

    public boolean isCheckOutRealizado() {
        return checkOutRealizado;
    }

    public int getCobros_adicionales() {
        return cobros_adicionales;
    }

    public boolean isEstaCancelado() {
        return estaCancelado;
    }

    public String getFechaCancelacion() {
        return fechaCancelacion;
    }

    public String getIdValoracion() {
        return idValoracion;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public boolean isTieneValoracion() {
        return tieneValoracion;
    }

    // Setters (agrega si necesitas modificar los valores después de la creación)
    // public void setIdReserva(String idReserva) { this.idReserva = idReserva; }
    // ... y así para los demás si es necesario
}