package com.example.hotroid.bean;
import com.google.firebase.firestore.Exclude; // Import for Firestore exclusion

public class Cliente {
    private String firestoreId; // Para almacenar el ID del documento en Firestore
    private String nombres;
    private String apellidos;
    private String estado; // "true" para activado, "false" para desactivado
    private String dni;
    private String nacimiento;
    private String correo;
    private String telefono;
    private String direccion;
    private String fotoPerfilUrl; // ¡AÑADIDO PARA LA FOTO DE PERFIL!

    public Cliente() {
        // Constructor vacío requerido para Firebase Firestore
    }

    // Constructor con todos los campos (excepto firestoreId, que se asigna después de guardar)
    public Cliente(String nombres, String apellidos, String estado, String dni,
                   String nacimiento, String correo, String telefono, String direccion, String fotoPerfilUrl) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.estado = estado;
        this.dni = dni;
        this.nacimiento = nacimiento;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.fotoPerfilUrl = fotoPerfilUrl; // Inicializa la URL de la foto
    }

    // Getters
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getEstado() { return estado; }
    public String getDni() { return dni; }
    public String getNacimiento() { return nacimiento; }
    public String getCorreo() { return correo; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getFotoPerfilUrl() { return fotoPerfilUrl; } // Getter para fotoPerfilUrl

    // Setters
    public void setNombres(String nombres) { this.nombres = nombres; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setDni(String dni) { this.dni = dni; }
    public void setNacimiento(String nacimiento) { this.nacimiento = nacimiento; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setFotoPerfilUrl(String fotoPerfilUrl) { this.fotoPerfilUrl = fotoPerfilUrl; } // Setter para fotoPerfilUrl

    // Getter y Setter para firestoreId (se usa para manejar el ID del documento en la app,
    // pero no se guarda directamente en Firestore con @Exclude)
    @Exclude
    public String getFirestoreId() {
        return firestoreId;
    }

    public void setFirestoreId(String firestoreId) {
        this.firestoreId = firestoreId;
    }
}