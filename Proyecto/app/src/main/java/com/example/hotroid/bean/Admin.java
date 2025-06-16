package com.example.hotroid.bean;

import com.google.firebase.firestore.Exclude;

public class Admin {
    private String firestoreId; // New field for the Firestore document ID
    private String nombres;
    private String apellidos;
    private String estado; // "true" or "false"
    private String tipoDocumento; // Nuevo campo para el tipo de documento (DNI, Pasaporte, Carnet de Extranjería)
    private String numeroDocumento; // Nuevo campo para el número de documento
    private String nacimiento;
    private String correo;
    private String telefono;
    private String direccion;
    private String hotelAsignado;
    private String fotoPerfilUrl; // For the image URL in Firebase Storage

    // Default constructor required by Firestore
    public Admin() {
        // Default constructor required for calls to DataSnapshot.toObject(Admin.class)
    }

    // Constructor con 9 argumentos (adaptado para los nuevos campos de documento)
    public Admin(String nombres, String apellidos, String estado, String tipoDocumento, String numeroDocumento,
                 String nacimiento, String correo, String telefono, String direccion, String hotelAsignado) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.estado = estado;
        this.tipoDocumento = tipoDocumento; // Asignar el tipo de documento
        this.numeroDocumento = numeroDocumento; // Asignar el número de documento
        this.nacimiento = nacimiento;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.hotelAsignado = hotelAsignado;
        this.fotoPerfilUrl = ""; // Initialize empty, will be populated after Storage upload
    }

    // Constructor con 10 argumentos (incluyendo fotoPerfilUrl, adaptado para los nuevos campos de documento)
    public Admin(String nombres, String apellidos, String estado, String tipoDocumento, String numeroDocumento,
                 String nacimiento, String correo, String telefono, String direccion, String hotelAsignado, String fotoPerfilUrl) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.estado = estado;
        this.tipoDocumento = tipoDocumento; // Asignar el tipo de documento
        this.numeroDocumento = numeroDocumento; // Asignar el número de documento
        this.nacimiento = nacimiento;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.hotelAsignado = hotelAsignado;
        this.fotoPerfilUrl = fotoPerfilUrl;
    }

    // Getters and Setters

    @Exclude
    public String getFirestoreId() {
        return firestoreId;
    }

    public void setFirestoreId(String firestoreId) {
        this.firestoreId = firestoreId;
    }

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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Nuevos Getters y Setters para tipoDocumento
    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    // Nuevos Getters y Setters para numeroDocumento
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

    public String getHotelAsignado() {
        return hotelAsignado;
    }

    public void setHotelAsignado(String hotelAsignado) {
        this.hotelAsignado = hotelAsignado;
    }

    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }

    public void setFotoPerfilUrl(String fotoPerfilUrl) {
        this.fotoPerfilUrl = fotoPerfilUrl;
    }
}