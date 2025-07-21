package com.example.hotroid.bean;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.PropertyName; // Importa esta anotación

import java.util.Date;
import java.util.List;

public class Reserva {
    private String idReserva;
    private String idPersona;
    private String nombresCliente;
    private String apellidosCliente;
    private String idHotel;
    private String nombreHotel;
    private int habitaciones;
    private int adultos;
    private int ninos;
    private Date fechaInicio;
    private Date fechaFin;
    private String estado; // "activo", "pasado", "cancelado"
    private double precioTotal;
    private boolean checkInRealizado;
    private boolean checkOutRealizado;
    private double cobrosAdicionales; // Nombre del campo en Java
    private boolean cancelada;
    private Date fechaCancelacion;
    private String idValoracion;
    private List<Integer> roomNumber;
    private boolean tieneValoracion;

    // Servicios adicionales
    private boolean gimnasio;
    private boolean desayuno;
    private boolean piscina;
    private boolean parqueo;

    @ServerTimestamp
    private Date fechaCreacion;

    // Constructor vacío requerido para Firestore
    public Reserva() {
    }

    public Reserva(String idReserva, String idPersona, String nombresCliente, String apellidosCliente,
                   String idHotel, String nombreHotel, int habitaciones, int adultos, int ninos,
                   Date fechaInicio, Date fechaFin, String estado, double precioTotal,
                   boolean checkInRealizado, boolean checkOutRealizado,
                   double cobrosAdicionales, // Asegúrate de que este constructor también lo reciba
                   boolean cancelada, Date fechaCancelacion, String idValoracion, List<Integer> roomNumber,
                   boolean tieneValoracion) {
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
        this.cobrosAdicionales = cobrosAdicionales; // Asignación de la propiedad
        this.cancelada = cancelada;
        this.fechaCancelacion = fechaCancelacion;
        this.idValoracion = idValoracion;
        this.roomNumber = roomNumber;
        this.tieneValoracion = tieneValoracion;
    }

    // Getters y setters
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

    // *** CAMBIO CLAVE AQUÍ: Anotación @PropertyName ***
    @PropertyName("cobros_adicionales") // Mapea la propiedad Java 'cobrosAdicionales' al campo 'cobros_adicionales' en Firestore
    public double getCobrosAdicionales() {
        return cobrosAdicionales;
    }

    // *** CAMBIO CLAVE AQUÍ: Anotación @PropertyName ***
    @PropertyName("cobros_adicionales") // Mapea la propiedad Java 'cobrosAdicionales' al campo 'cobros_adicionales' en Firestore
    public void setCobrosAdicionales(double cobrosAdicionales) {
        this.cobrosAdicionales = cobrosAdicionales;
    }

    public boolean isCancelada() {
        return cancelada;
    }

    public void setCancelada(boolean cancelada) {
        this.cancelada = cancelada;
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

    public List<Integer> getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(List<Integer> roomNumber) {
        this.roomNumber = roomNumber;
    }

    public boolean isTieneValoracion() {
        return tieneValoracion;
    }

    public void setTieneValoracion(boolean tieneValoracion) {
        this.tieneValoracion = tieneValoracion;
    }

    public boolean isGimnasio() {
        return gimnasio;
    }

    public void setGimnasio(boolean gimnasio) {
        this.gimnasio = gimnasio;
    }

    public boolean isDesayuno() {
        return desayuno;
    }

    public void setDesayuno(boolean desayuno) {
        this.desayuno = desayuno;
    }

    public boolean isPiscina() {
        return piscina;
    }

    public void setPiscina(boolean piscina) {
        this.piscina = piscina;
    }

    public boolean isParqueo() {
        return parqueo;
    }

    public void setParqueo(boolean parqueo) {
        this.parqueo = parqueo;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "idReserva='" + idReserva + '\'' +
                ", idPersona='" + idPersona + '\'' +
                ", nombresCliente='" + nombresCliente + '\'' +
                ", idHotel='" + idHotel + '\'' +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", estado='" + estado + '\'' +
                '}';
    }

    @Exclude
    public String getNombreCompleto() {
        return nombresCliente + " " + apellidosCliente;
    }
}