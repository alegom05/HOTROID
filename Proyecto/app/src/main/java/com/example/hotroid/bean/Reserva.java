package com.example.hotroid.bean;

import com.google.firebase.firestore.DocumentId; // ¡Importante!
import java.util.Date; // ¡Importante para fechas!

public class Reserva {
    @DocumentId // Esto mapea el ID del documento de Firestore a esta propiedad
    private String idReserva;

    private String idPersona; // id de la persona que reserva
    private String idHotel; // id del hotel al que se reservó

    private int habitaciones; // cantidad de habitaciones reservadas
    private int adultos;
    private int ninos;
    private Date fechaInicio; // Cambiado a Date
    private Date fechaFin;   // Cambiado a Date
    private String estado;   // "activo", "pasado", "cancelado"
    private double precioTotal; // Cambiado a double

    // Atributos adicionales basados en la imagen/requerimientos
    private boolean checkInRealizado;
    private boolean checkOutRealizado;
    private double cobros_adicionales; // Cambiado a double
    private boolean estaCancelado;
    private Date fechaCancelacion; // Cambiado a Date (puede ser null)
    private String idValoracion;   // Puede ser null
    private String roomNumber;     // Asumo que es String, si es un objeto en Firebase, necesitará un bean aparte
    private boolean tieneValoracion;

    // CONSTRUCTOR VACÍO - ¡ESTE ES CRÍTICO PARA FIRESTORE!
    public Reserva() {
        // Constructor vacío requerido por Firebase Firestore para la deserialización
    }

    // Constructor completo con todos los parámetros
    // Puedes tener múltiples constructores si necesitas formas diferentes de crear objetos
    public Reserva(String idReserva, String idPersona, String idHotel, int habitaciones, int adultos, int ninos,
                   Date fechaInicio, Date fechaFin, String estado, double precioTotal,
                   boolean checkInRealizado, boolean checkOutRealizado, double cobros_adicionales,
                   boolean estaCancelado, Date fechaCancelacion, String idValoracion, String roomNumber, boolean tieneValoracion) {
        this.idReserva = idReserva;
        this.idPersona = idPersona;
        this.idHotel = idHotel;
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

    // --- Getters --- (Todos necesarios para Firestore)
    public String getIdReserva() {
        return idReserva;
    }

    public String getIdPersona() {
        return idPersona;
    }

    public String getIdHotel() {
        return idHotel;
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

    public Date getFechaInicio() { // Cambiado a Date
        return fechaInicio;
    }

    public Date getFechaFin() { // Cambiado a Date
        return fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public double getPrecioTotal() { // Cambiado a double
        return precioTotal;
    }

    public boolean isCheckInRealizado() {
        return checkInRealizado;
    }

    public boolean isCheckOutRealizado() {
        return checkOutRealizado;
    }

    public double getCobros_adicionales() { // Cambiado a double
        return cobros_adicionales;
    }

    public boolean isEstaCancelado() {
        return estaCancelado;
    }

    public Date getFechaCancelacion() { // Cambiado a Date
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

    // --- Setters --- (Todos son NECESARIOS para que Firestore pueda "llenar" tu objeto)
    // Firestore usa los setters para establecer los valores de las propiedades al deserializar.
    public void setIdReserva(String idReserva) {
        this.idReserva = idReserva;
    }

    public void setIdPersona(String idPersona) {
        this.idPersona = idPersona;
    }

    public void setIdHotel(String idHotel) {
        this.idHotel = idHotel;
    }

    public void setHabitaciones(int habitaciones) {
        this.habitaciones = habitaciones;
    }

    public void setAdultos(int adultos) {
        this.adultos = adultos;
    }

    public void setNinos(int ninos) {
        this.ninos = ninos;
    }

    public void setFechaInicio(Date fechaInicio) { // Cambiado a Date
        this.fechaInicio = fechaInicio;
    }

    public void setFechaFin(Date fechaFin) { // Cambiado a Date
        this.fechaFin = fechaFin;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setPrecioTotal(double precioTotal) { // Cambiado a double
        this.precioTotal = precioTotal;
    }

    public void setCheckInRealizado(boolean checkInRealizado) {
        this.checkInRealizado = checkInRealizado;
    }

    public void setCheckOutRealizado(boolean checkOutRealizado) {
        this.checkOutRealizado = checkOutRealizado;
    }

    public void setCobros_adicionales(double cobros_adicionales) { // Cambiado a double
        this.cobros_adicionales = cobros_adicionales;
    }

    public void setEstaCancelado(boolean estaCancelado) {
        this.estaCancelado = estaCancelado;
    }

    public void setFechaCancelacion(Date fechaCancelacion) { // Cambiado a Date
        this.fechaCancelacion = fechaCancelacion;
    }

    public void setIdValoracion(String idValoracion) {
        this.idValoracion = idValoracion;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setTieneValoracion(boolean tieneValoracion) {
        this.tieneValoracion = tieneValoracion;
    }
}