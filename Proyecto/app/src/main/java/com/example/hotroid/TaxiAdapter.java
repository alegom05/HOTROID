package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.TaxiItem;

import java.util.List;

public class TaxiAdapter extends RecyclerView.Adapter<TaxiAdapter.TaxiViewHolder> {

    private List<TaxiItem> taxiList;

    public TaxiAdapter(List<TaxiItem> taxiList) {
        this.taxiList = taxiList;
    }

    @NonNull
    @Override
    public TaxiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.taxi_item, parent, false);
        return new TaxiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxiViewHolder holder, int position) {
        TaxiItem taxiItem = taxiList.get(position);
        holder.nombreTextView.setText(taxiItem.getNombreTaxista());
        holder.estadoTextView.setText(taxiItem.getEstado());
        holder.imagenImageView.setImageResource(taxiItem.getImagen());
    }

    @Override
    public int getItemCount() {
        return taxiList.size();
    }

    public static class TaxiViewHolder extends RecyclerView.ViewHolder {

        TextView nombreTextView;
        TextView estadoTextView;
        ImageView imagenImageView;

        public TaxiViewHolder(View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.nombreTaxista);
            estadoTextView = itemView.findViewById(R.id.estadoTaxista);
            imagenImageView = itemView.findViewById(R.id.imagenTaxista);
        }
    }
}
