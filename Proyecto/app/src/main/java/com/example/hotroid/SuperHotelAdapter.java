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
        holder.tvHotelRating.setText(String.format(Locale.getDefault(), "%.1f", hotel.getRating()));
        //holder.tvHotelPrice.setText(String.format(Locale.getDefault(), "S/. %.2f", hotel.getPrice()));
        holder.ratingBar.setRating(hotel.getRating());

        if (hotel.getImageUrls() != null && !hotel.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(hotel.getImageUrls().get(0)) // Carga la primera imagen de la lista
                    .placeholder(R.drawable.placeholder_hotel)
                    .error(R.drawable.ic_user_error)
                    .into(holder.ivHotelImage);
        } else {
            holder.ivHotelImage.setImageResource(R.drawable.placeholder_hotel);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SuperDetallesActivity.class);
            intent.putExtra("hotel_id", hotel.getIdHotel());
            intent.putExtra("hotel_name", hotel.getName());
            intent.putExtra("hotel_location", hotel.getDireccion());
            intent.putExtra("hotel_detailed_address", hotel.getDireccionDetallada());
            intent.putExtra("hotel_rating", hotel.getRating());
            //intent.putExtra("hotel_price", hotel.getPrice());
            intent.putExtra("hotel_description", hotel.getDescription());
            intent.putExtra("hotel_image_urls", new ArrayList<>(hotel.getImageUrls())); // <-- PASA LA LISTA
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

    public void setHotels(List<Hotel> newHotels) {
        this.hotelList.clear();
        this.hotelList.addAll(newHotels);
        this.hotelListFull = new ArrayList<>(newHotels);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        hotelList.clear();
        if (text.isEmpty()) {
            hotelList.addAll(hotelListFull);
        } else {
            text = text.toLowerCase(Locale.getDefault());
            for (Hotel hotel : hotelListFull) {
                if (hotel.getName().toLowerCase(Locale.getDefault()).contains(text) ||
                        hotel.getDireccion().toLowerCase(Locale.getDefault()).contains(text)) {
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