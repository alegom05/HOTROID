package com.example.hotroid.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

public class RoomGroupOption implements Parcelable {
    private String roomType;
    private int habitacionesNecesarias;
    private int disponibles;
    private int totalAdults;
    private int totalChildren;
    private double precioPorHabitacion;
    private List<Room> habitacionesSeleccionadas;

    public RoomGroupOption() {}

    protected RoomGroupOption(Parcel in) {
        roomType = in.readString();
        habitacionesNecesarias = in.readInt();
        disponibles = in.readInt();
        totalAdults = in.readInt();
        totalChildren = in.readInt();
        precioPorHabitacion = in.readDouble();
        habitacionesSeleccionadas = in.createTypedArrayList(Room.CREATOR);
    }

    public static final Creator<RoomGroupOption> CREATOR = new Creator<RoomGroupOption>() {
        @Override
        public RoomGroupOption createFromParcel(Parcel in) {
            return new RoomGroupOption(in);
        }

        @Override
        public RoomGroupOption[] newArray(int size) {
            return new RoomGroupOption[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(roomType);
        dest.writeInt(habitacionesNecesarias);
        dest.writeInt(disponibles);
        dest.writeInt(totalAdults);
        dest.writeInt(totalChildren);
        dest.writeDouble(precioPorHabitacion);
        dest.writeTypedList(habitacionesSeleccionadas);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public List<Room> getHabitacionesSeleccionadas() {
        return habitacionesSeleccionadas;
    }

    public void setHabitacionesSeleccionadas(List<Room> habitacionesSeleccionadas) {
        this.habitacionesSeleccionadas = habitacionesSeleccionadas;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public int getHabitacionesNecesarias() {
        return habitacionesNecesarias;
    }

    public void setHabitacionesNecesarias(int habitacionesNecesarias) {
        this.habitacionesNecesarias = habitacionesNecesarias;
    }

    public int getDisponibles() {
        return disponibles;
    }

    public void setDisponibles(int disponibles) {
        this.disponibles = disponibles;
    }

    public int getTotalAdults() {
        return totalAdults;
    }

    public void setTotalAdults(int totalAdults) {
        this.totalAdults = totalAdults;
    }

    public int getTotalChildren() {
        return totalChildren;
    }

    public void setTotalChildren(int totalChildren) {
        this.totalChildren = totalChildren;
    }

    public double getPrecioPorHabitacion() {
        return precioPorHabitacion;
    }

    public void setPrecioPorHabitacion(double precioPorHabitacion) {
        this.precioPorHabitacion = precioPorHabitacion;
    }

}
