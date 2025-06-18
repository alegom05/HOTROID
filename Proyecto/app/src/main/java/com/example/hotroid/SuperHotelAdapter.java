package com.example.hotroid;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hotroid.bean.Hotel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SuperHotelAdapter extends RecyclerView.Adapter<SuperHotelAdapter.HotelViewHolder> {

    private List<Hotel> hotelList;
    private List<Hotel> hotelListFull;
    private Context context;

    public SuperHotelAdapter(Context context, List<Hotel> hotelList) {
        this.context = context;
        this.hotelList = hotelList;
        this.hotelListFull = new ArrayList<>(hotelList);
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotelList.get(position);

        holder.tvHotelName.setText(hotel.getName());
        holder.tvHotelLocation.setText(hotel.getDireccion());
        holder.tvHotelRating.setText(String.format("%.1f", hotel.getRating()));
        holder.tvHotelPrice.setText(String.format(Locale.getDefault(), "S/. %.2f", hotel.getPrice()));


        // Configurar RatingBar
        holder.ratingBar.setRating(hotel.getRating());

        // Cargar imagen
        if (hotel.getImageResourceId() != 0) {
            Glide.with(context)
                    .load(hotel.getImageResourceId())
                    .placeholder(R.drawable.placeholder_hotel)
                    .into(holder.ivHotelImage);
        } else {
            holder.ivHotelImage.setImageResource(R.drawable.placeholder_hotel);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SuperDetallesActivity.class);
            intent.putExtra("hotel_id", hotel.getIdHotel());
            intent.putExtra("hotel_name", hotel.getName());
            intent.putExtra("hotel_image_resource_id", hotel.getImageResourceId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    public static class HotelViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHotelImage;
        TextView tvHotelName, tvHotelLocation, tvHotelRating, tvHotelPrice;
        RatingBar ratingBar;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHotelImage = itemView.findViewById(R.id.ivHotelImage2);
            tvHotelName = itemView.findViewById(R.id.tvHotelName2);
            tvHotelLocation = itemView.findViewById(R.id.tvHotelLocation2);
            tvHotelRating = itemView.findViewById(R.id.tvHotelRating2);
            tvHotelPrice = itemView.findViewById(R.id.tvHotelPrice2);
            ratingBar = itemView.findViewById(R.id.ratingBar2);
        }
    }

    public void setHotels(List<Hotel> hotels) {
        this.hotelList = hotels;
        this.hotelListFull = new ArrayList<>(hotels);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        hotelList.clear();
        if (text.isEmpty()) {
            hotelList.addAll(hotelListFull);
        } else {
            text = text.toLowerCase();
            for (Hotel hotel : hotelListFull) {
                if (hotel.getName().toLowerCase().contains(text) ||
                        hotel.getDireccion().toLowerCase().contains(text)) {
                    hotelList.add(hotel);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void clearFilter() {
        hotelList.clear();
        hotelList.addAll(hotelListFull);
        notifyDataSetChanged();
    }
}