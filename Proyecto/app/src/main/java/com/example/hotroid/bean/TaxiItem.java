package com.example.hotroid.bean;

public class TaxiItem {
    private String nombreTaxista;
    private String estado;
    private int imagen;

    public TaxiItem(String nombreTaxista, String estado, int imagen) {
        this.nombreTaxista = nombreTaxista;
        this.estado = estado;
        this.imagen = imagen;
    }

    public String getNombreTaxista() {
        return nombreTaxista;
    }

    public String getEstado() {
        return estado;
    }

    public int getImagen() {
        return imagen;
    }
}
