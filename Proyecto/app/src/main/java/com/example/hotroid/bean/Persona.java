package com.example.hotroid.bean;

import java.io.Serializable;
import java.util.Date;

public class Persona implements Serializable {

    private String nombre;
    private String apellido;
    private String correo;
    private String direccion;
    private String dni;
    private String idRol;
    private Date nacimiento;
    private String telefono;
    private TarjetaCredito numeroTarjetaCredito; // subobjeto

    public Persona() {

    }

    public Persona(String correo, String nombre) {
        this.nombre = correo;
        this.correo = correo;
    }
    public Persona(String correo, String nombre,String dni, String idRol, Date nacimiento, String telefono){
        this.correo=correo;
        this.nombre = nombre;
        this.dni= dni;
        this.idRol = idRol;
        this.nacimiento = nacimiento;
        this.telefono= telefono;
    }

    public Persona(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    public String getCorreo() {
        return correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getDni() {
        return dni;
    }

    public String getIdRol() {
        return idRol;
    }

    public Date getNacimiento() {
        return nacimiento;
    }

    public String getTelefono() {
        return telefono;
    }
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }
    public void setIdRol(String idRol) {
        this.idRol = idRol;
    }

    public void setNacimiento(Date nacimiento) {
        this.nacimiento = nacimiento;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    public TarjetaCredito getNumeroTarjetaCredito() {
        return numeroTarjetaCredito;
    }

    public void setNumeroTarjetaCredito(TarjetaCredito numeroTarjetaCredito) {
        this.numeroTarjetaCredito = numeroTarjetaCredito;
    }
}
