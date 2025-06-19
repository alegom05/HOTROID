package com.example.hotroid.bean;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;

public class Reserva {

    @DocumentId // Esta anotación le dice a Firestore que este campo debe usarse como el ID del documento
    private String idReserva;
    private String idPersona;
    private String nombresCliente; // Campo desnormalizado
    private String apellidosCliente; // Campo desnormalizado
    private String idHotel;
    private String nombreHotel; // Campo desnormalizado
    private int habitaciones;
    private int adultos;
    private int ninos;
    private Date fechaInicio;
    private Date fechaFin;
    private String estado;
    private double precioTotal;
    private boolean checkInRealizado;
    private boolean checkOutRealizado;
    private double cobros_adicionales;
    private boolean estaCancelado;
    private Date fechaCancelacion;
    private String idValoracion;
    private String roomNumber;
    private boolean tieneValoracion;

    // Constructor vacío requerido por Firestore
    public Reserva() {
        // Constructor vacío
    }

    // Constructor completo para cuando creas una nueva reserva en tu código
    public Reserva(String idReserva, String idPersona, String nombresCliente, String apellidosCliente,
                   String idHotel, String nombreHotel, int habitaciones, int adultos, int ninos,
                   Date fechaInicio, Date fechaFin, String estado, double precioTotal,
                   boolean checkInRealizado, boolean checkOutRealizado, double cobros_adicionales,
                   boolean estaCancelado, Date fechaCancelacion, String idValoracion,
                   String roomNumber, boolean tieneValoracion) {
        this.idReserva = idReserva;
        this.idPersona = idPersona;
        this.nombresCliente = nombresCliente;
        this.apellidosCliente = apellidosCliente;
        this.idHotel = idHotel;
        this.nombreHotel = nombreHotel;
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

    // --- Getters y Setters ---

    public String getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(String idReserva) {
        this.idReserva = idReserva;
    }

    public String getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(String idPersona) {
        this.idPersona = idPersona;
    }

    public String getNombresCliente() {
        return nombresCliente;
    }

    public void setNombresCliente(String nombresCliente) {
        this.nombresCliente = nombresCliente;
    }

    public String getApellidosCliente() {
        return apellidosCliente;
    }

    public void setApellidosCliente(String apellidosCliente) {
        this.apellidosCliente = apellidosCliente;
    }

    public String getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(String idHotel) {
        this.idHotel = idHotel;
    }

    public String getNombreHotel() {
        return nombreHotel;
    }

    public void setNombreHotel(String nombreHotel) {
        this.nombreHotel = nombreHotel;
    }

    public int getHabitaciones() {
        return habitaciones;
    }

    public void setHabitaciones(int habitaciones) {
        this.habitaciones = habitaciones;
    }

    public int getAdultos() {
        return adultos;
    }

    public void setAdultos(int adultos) {
        this.adultos = adultos;
    }

    public int getNinos() {
        return ninos;
    }

    public void setNinos(int ninos) {
        this.ninos = ninos;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(double precioTotal) {
        this.precioTotal = precioTotal;
    }

    public boolean isCheckInRealizado() {
        return checkInRealizado;
    }

    public void setCheckInRealizado(boolean checkInRealizado) {
        this.checkInRealizado = checkInRealizado;
    }

    public boolean isCheckOutRealizado() {
        return checkOutRealizado;
    }

    public void setCheckOutRealizado(boolean checkOutRealizado) {
        this.checkOutRealizado = checkOutRealizado;
    }

    public double getCobros_adicionales() {
        return cobros_adicionales;
    }

    public void setCobros_adicionales(double cobros_adicionales) {
        this.cobros_adicionales = cobros_adicionales;
    }

    public boolean isEstaCancelado() {
        return estaCancelado;
    }

    public void setEstaCancelado(boolean estaCancelado) {
        this.estaCancelado = estaCancelado;
    }

    public Date getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(Date fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public String getIdValoracion() {
        return idValoracion;
    }

    public void setIdValoracion(String idValoracion) {
        this.idValoracion = idValoracion;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public boolean isTieneValoracion() {
        return tieneValoracion;
    }

    public void setTieneValoracion(boolean tieneValoracion) {
        this.tieneValoracion = tieneValoracion;
    }
}