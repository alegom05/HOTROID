package com.example.hotroid.bean;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class Hotel {
    @DocumentId
    private String idHotel;
    private String name;
    private float rating;
    private double price; // <--- CAMBIADO A DOUBLE
    private String direccion;
    private String direccionDetallada;
    private String description;

    private String imageName;
    private transient int imageResourceId;
    private List<String> imageUrls;
    private String idUbicacion;
    private String idServicio;
    private String idFoto;
    private String idHabitacion;
    private String idReserva;

    private transient boolean isFavorite = false;

    public Hotel() {
        // Constructor vacío requerido por Firestore
    }

    // Actualiza el constructor si lo usas
    public Hotel(String idHotel, String name, float rating, double price, String direccion, // <--- CAMBIADO A DOUBLE
                 String direccionDetallada, String description, String imageName,
                 String idUbicacion, String idServicio, String idFoto,
                 String idHabitacion, String idReserva) {
        this.idHotel = idHotel;
        this.name = name;
        this.rating = rating;
        this.price = price; // <--- Asignación directa
        this.direccion = direccion;
        this.direccionDetallada = direccionDetallada;
        this.description = description;
        this.imageName = imageName;
        this.idUbicacion = idUbicacion;
        this.idServicio = idServicio;
        this.idFoto = idFoto;
        this.idHabitacion = idHabitacion;
        this.idReserva = idReserva;
    }

    // --- Getters ---
    public String getIdHotel() { return idHotel; }
    public String getName() { return name; }
    public float getRating() { return rating; }
    public double getPrice() { return price; } // <--- CAMBIADO A DOUBLE
    public String getDireccion() { return direccion; }
    public String getDireccionDetallada() { return direccionDetallada; }
    public String getDescription() { return description; }
    public String getImageName() { return imageName; }
    public int getImageResourceId() { return imageResourceId; }
    public List<String> getImageUrls() { return imageUrls; }
    public String getIdUbicacion() { return idUbicacion; }
    public String getIdServicio() { return idServicio; }
    public String getIdFoto() { return idFoto; }
    public String getIdHabitacion() { return idHabitacion; }
    public String getIdReserva() { return idReserva; }
    public boolean isFavorite() { return isFavorite; }

    // --- Setters ---
    public void setIdHotel(String idHotel) { this.idHotel = idHotel; }
    public void setName(String name) { this.name = name; }
    public void setRating(float rating) { this.rating = rating; }
    public void setPrice(double price) { this.price = price; } // <--- CAMBIADO A DOUBLE
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setDireccionDetallada(String direccionDetallada) { this.direccionDetallada = direccionDetallada; }
    public void setDescription(String description) { this.description = description; }
    public void setImageName(String imageName) { this.imageName = imageName; }
    public void setImageResourceId(int imageResourceId) { this.imageResourceId = imageResourceId; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public void setIdUbicacion(String idUbicacion) { this.idUbicacion = idUbicacion; }
    public void setIdServicio(String idServicio) { this.idServicio = idServicio; }
    public void setIdFoto(String idFoto) { this.idFoto = idFoto; }
    public void setIdHabitacion(String idHabitacion) { this.idHabitacion = idHabitacion; }
    public void setIdReserva(String idReserva) { this.idReserva = idReserva; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}