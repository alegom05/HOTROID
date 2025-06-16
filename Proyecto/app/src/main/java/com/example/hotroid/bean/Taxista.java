package com.example.hotroid.bean;

public class Taxista {
    // Campos principales del taxista
    private String nombres;
    private String apellidos;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nacimiento;
    private String correo;
    private String telefono;
    private String direccion;
    private String fotoPerfilUrl;

    // Campos específicos del vehículo
    private String placa;
    private String fotoVehiculoUrl;

    // Campos de estado
    private String estado; // Puede ser "activado", "desactivado", "pendiente"
    private String estadoDeViaje; // Puede ser "Asignado", "No Asignado", "En Camino", "Llego a Destino"

    // Constructor vacío requerido por Firebase Firestore para deserialización
    public Taxista() {
        // Constructor vacío
    }

    // Constructor simplificado (útil para listas o vistas previas)
    public Taxista(String nombres, String apellidos, String estado, String fotoPerfilUrl, String estadoDeViaje) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.estado = estado;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.estadoDeViaje = estadoDeViaje;
    }

    // Constructor completo para cuando tienes todos los datos
    public Taxista(String nombres, String apellidos, String tipoDocumento, String numeroDocumento,
                   String nacimiento, String correo, String telefono, String direccion,
                   String fotoPerfilUrl, String placa, String fotoVehiculoUrl,
                   String estado, String estadoDeViaje) { // Nuevo campo aquí
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.nacimiento = nacimiento;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.placa = placa;
        this.fotoVehiculoUrl = fotoVehiculoUrl;
        this.estado = estado;
        this.estadoDeViaje = estadoDeViaje; // Asignación del nuevo campo
    }

    // --- Getters y Setters ---

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getNacimiento() {
        return nacimiento;
    }

    public void setNacimiento(String nacimiento) {
        this.nacimiento = nacimiento;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }

    public void setFotoPerfilUrl(String fotoPerfilUrl) {
        this.fotoPerfilUrl = fotoPerfilUrl;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getFotoVehiculoUrl() {
        return fotoVehiculoUrl;
    }

    public void setFotoVehiculoUrl(String fotoVehiculoUrl) {
        this.fotoVehiculoUrl = fotoVehiculoUrl;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEstadoDeViaje() { // Nuevo getter
        return estadoDeViaje;
    }

    public void setEstadoDeViaje(String estadoDeViaje) { // Nuevo setter
        this.estadoDeViaje = estadoDeViaje;
    }
}