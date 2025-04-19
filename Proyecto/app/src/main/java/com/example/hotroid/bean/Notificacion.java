package com.example.hotroid.bean;

public class Notificacion {
    private String mensajeResumen;
    private String mensajeCompleto;
    private Integer recursoImagen;
    public Notificacion(String resumen, String completo, Integer img){
        this.mensajeResumen=resumen;
        this.mensajeCompleto=completo;
        this.recursoImagen=img;
    }
    public String getMensajeCompleto(){
        return mensajeCompleto;
    }
    public String getMensajeResumen(){
        return mensajeResumen;
    }
    public Integer getRecursoImagen(){
        return recursoImagen;
    }
}
