package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaxiCuentaAdapter extends RecyclerView.Adapter<TaxiCuentaAdapter.ViewHolder> {

    private final List<String[]> datos; // <-- Aquí debe ser List<String[]>


    public TaxiCuentaAdapter(List<String[]> datos) {
        this.datos = datos;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.taxi_cuenta_item, parent, false);
        return new ViewHolder(vista);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvClave, tvValor;

        public ViewHolder(View itemView) {
            super(itemView);
            tvClave = itemView.findViewById(R.id.tvClave);
            tvValor = itemView.findViewById(R.id.tvValor);
        }
    }





    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String[] par = datos.get(position);
        holder.tvClave.setText(par[0]);
        holder.tvValor.setText(par[1]);
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }
}
