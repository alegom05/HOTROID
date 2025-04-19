package com.example.hotroid.bean;

public class Notificacion {
    private String mensajeResumen;
    private String mensajeCompleto;
    private Integer recursoImagen;
    private String codigoQr;
    public Notificacion(String resumen, String completo, Integer img, String codigoQr){
        this.mensajeResumen=resumen;
        this.mensajeCompleto=completo;
        this.recursoImagen=img;
        this.codigoQr = codigoQr;
    }
    public Notificacion(String resumen, String completo, Integer img) {
        this(resumen, completo, img, null);
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
    public String getCodigoQr(){
        return codigoQr;
    }
    public boolean tieneCodigoQr() {
        return codigoQr != null && !codigoQr.isEmpty();
    }
}
