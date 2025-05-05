package com.example.hotroid.bean;

public class Taxista {
    private String nombre, estado, dni, nacimiento, correo, telefono, direccion, placa;
    private int imagenResId, vehiculoImagenResId;

    public Taxista(String nombre, String estado, int imagenResId) {
        this.nombre = nombre;
        this.estado = estado;
        this.imagenResId = imagenResId;
    }

    public Taxista(String nombre, String estado, int imagenResId, String dni, String nacimiento,
                   String correo, String telefono, String direccion, String placa, int vehiculoImagenResId) {
        this.nombre = nombre;
        this.estado = estado;
        this.imagenResId = imagenResId;
        this.dni = dni;
        this.nacimiento = nacimiento;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.placa = placa;
        this.vehiculoImagenResId = vehiculoImagenResId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public int getImagenResId() {
        return imagenResId;
    }

    public void setImagenResId(int imagenResId) {
        this.imagenResId = imagenResId;
    }

    public int getVehiculoImagenResId() {
        return vehiculoImagenResId;
    }

    public void setVehiculoImagenResId(int vehiculoImagenResId) {
        this.vehiculoImagenResId = vehiculoImagenResId;
    }
}

