package com.example.hotroid.util;

import java.util.Date;

/**
 * Clase temporal para almacenar los datos de reserva mientras
 * el usuario navega por los distintos fragmentos del proceso
 */
public class ReservacionTempData {
    // Datos del cliente
    public static String idPersona;
    public static String nombresCliente;
    public static String apellidosCliente;

    // Datos del hotel
    public static String idHotel;
    public static String nombreHotel;

    // Datos de la reserva
    public static int habitaciones;
    public static int adultos;
    public static int ninos;
    public static Date fechaInicio;
    public static Date fechaFin;
    public static double precioBase = 0.0;
    public static double cobrosAdicionales = 0.0;
    public static double precioTotal = 0.0;
    public static String roomNumber;

    // Servicios adicionales
    public static boolean gimnasio = false;
    public static boolean desayuno = false;
    public static boolean piscina = false;
    public static boolean parqueo = false;

    /**
     * Limpia todos los datos temporales
     */
    public static void reset() {
        idPersona = null;
        nombresCliente = null;
        apellidosCliente = null;
        idHotel = null;
        nombreHotel = null;
        habitaciones = 1;
        adultos = 1;
        ninos = 0;
        fechaInicio = null;
        fechaFin = null;
        precioBase = 0.0;
        cobrosAdicionales = 0.0;
        precioTotal = 0.0;
        roomNumber = null;
        gimnasio = false;
        desayuno = false;
        piscina = false;
        parqueo = false;
    }
}