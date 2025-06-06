package com.example.hotroid;

import com.example.hotroid.bean.Reserva;
import com.example.hotroid.bean.ReservaConHotel;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

//public class ReservasPagerAdapterUser extends FragmentStateAdapter {
//
//    public ReservasPagerAdapterUser(@NonNull FragmentActivity fragmentActivity) {
//        super(fragmentActivity);
//    }
//
//    @NonNull
//    @Override
//    public Fragment createFragment(int position) {
//        switch (position) {
//            case 0: return new FragmentReservaActivos();
//            case 1: return new FragmentReservaPasados();
//            case 2: return new FragmentReservaCancelados();
//            default: return new FragmentReservaActivos();
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return 3;
//    }
//}
public class ReservasPagerAdapterUser extends RecyclerView.Adapter<ReservasPagerAdapterUser.ReservaViewHolder> {

    private final Context context;
    private final List<List<ReservaConHotel>> listasPorEstado; // Lista de listas: [activos, pasados, cancelados]

    //private static final int VIEW_TYPE_ACTIVO = 0;
//    private static final int VIEW_TYPE_PASADO = 1;
//    private static final int VIEW_TYPE_CANCELADO = 2;

    //constructor
    public ReservasPagerAdapterUser(Context context,  List<List<ReservaConHotel>> listasPorEstado) {
        this.context=context;
        this.listasPorEstado=listasPorEstado;
    }

    /*@NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.)
        switch (position) {
            case 0: return new FragmentReservaActivos();
            case 1: return new FragmentReservaPasados();
            case 2: return new FragmentReservaCancelados();
            default: return new FragmentReservaActivos();
        }
    }*/

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_pagina_reservas, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        holder.recyclerView.setAdapter(null);
        // Cada posición del ViewPager corresponde a una lista específica (activos, pasados, cancelados)
        List<ReservaConHotel> reservasPorEstado = listasPorEstado.get(position);
        ReservaAdapterUser adapter = new ReservaAdapterUser(reservasPorEstado, context, position);
        holder.recyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return listasPorEstado.size(); // 3 páginas: activos, pasados, cancelados
    }

    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.recycler_reservas);
            recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }
    }
}


