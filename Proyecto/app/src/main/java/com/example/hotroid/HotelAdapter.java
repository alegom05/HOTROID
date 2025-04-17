package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.R;
import com.example.hotroid.Hotel;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {

    private List<Hotel> hotels;

    public HotelAdapter(List<Hotel> hotels) {
        this.hotels = hotels;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotels.get(position);

        holder.hotelName.setText(hotel.getName());
        holder.hotelRating.setRating(hotel.getRating());
        holder.ratingText.setText(String.valueOf(hotel.getRating()));
        holder.hotelPrice.setText(hotel.getPrice());
        holder.hotelImage.setImageResource(hotel.getImageResourceId());

        holder.viewDetailButton.setOnClickListener(v -> {
            // Por ahora, solo mostramos un Toast con el nombre del hotel
            Toast.makeText(v.getContext(),
                    "Ver detalles de " + hotel.getName(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return hotels.size();
    }

    public static class HotelViewHolder extends RecyclerView.ViewHolder {
        ImageView hotelImage;
        TextView hotelName;
        RatingBar hotelRating;
        TextView ratingText;
        TextView hotelPrice;
        MaterialButton viewDetailButton;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            hotelImage = itemView.findViewById(R.id.hotelImage);
            hotelName = itemView.findViewById(R.id.hotelName);
            hotelRating = itemView.findViewById(R.id.hotelRating);
            ratingText = itemView.findViewById(R.id.ratingText);
            hotelPrice = itemView.findViewById(R.id.hotelPrice);
            viewDetailButton = itemView.findViewById(R.id.viewDetailButton);
        }
    }
}
