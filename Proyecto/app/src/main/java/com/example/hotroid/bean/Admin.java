package com.example.hotroid.bean;

import com.google.firebase.firestore.Exclude;

public class Admin {
    private String firestoreId; // Nuevo campo para el ID del documento en Firestore
    private String nombres;
    private String apellidos;
    private String estado; // "true" o "false"
    private String dni; // Este campo seguirá siendo el número de documento real
    private String nacimiento;
    private String correo;
    private String telefono;
    private String direccion;
    private String hotelAsignado;
    private String fotoPerfilUrl; // Para la URL de la imagen en Firebase Storage

    // Constructor sin argumentos requerido por Firestore
    public Admin() {
        // Default constructor required for calls to DataSnapshot.toObject(Admin.class)
    }

    // Constructor completo actualizado (9 argumentos de tipo String)
    public Admin(String nombres, String apellidos, String estado, String dni, String nacimiento,
                 String correo, String telefono, String direccion, String hotelAsignado) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.estado = estado;
        this.dni = dni;
        this.nacimiento = nacimiento;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.hotelAsignado = hotelAsignado;
        this.fotoPerfilUrl = ""; // Inicializar vacío, se llenará al subir a Storage
    }

    // Getters y Setters
    // Nuevo: Getter y Setter para firestoreId
    @Exclude // Esto es importante para que Firestore no intente guardar este campo dos veces si el ID del documento ya es firestoreId
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

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
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

    public void setDireccion(String direccion) { // Corrección: 'void setDireccion'
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