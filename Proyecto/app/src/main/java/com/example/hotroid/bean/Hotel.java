package com.example.hotroid.bean;

public class Hotel {
    private String idHotel;
    private String name;
    private float rating;
    private String price;
    private String direccion;
    private int imageResourceId;  // ID del recurso drawable para la imagen

    public Hotel(String idHotel, String name, float rating, String price, String direccion, int imageResourceId) {
        this.idHotel = idHotel;
        this.name = name;
        this.rating = rating;
        this.price = price;
        this.direccion = direccion;
        this.imageResourceId = imageResourceId;
    }

    public String getName() {
        return name;
    }

    public float getRating() {
        return rating;
    }

    public String getPrice() {
        return price;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
    public String getDireccion() {
        return direccion;
    }

    public String getIdHotel() {
        return idHotel;
    }
}