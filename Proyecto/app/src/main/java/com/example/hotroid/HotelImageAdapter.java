package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hotroid.R;

import java.util.List;

public class HotelImageAdapter extends RecyclerView.Adapter<HotelImageAdapter.ImageViewHolder> {

    private List<String> imageResources;

    public HotelImageAdapter(List<String> imageResources) {
        this.imageResources = imageResources;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hotel_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageResources.get(position);
//        holder.imageView.setImageResource(imageResources.get(position));
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.placeholder_hotel)
                .error(R.drawable.placeholder_hotel)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageResources != null ? imageResources.size() : 0;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.hotelImageView);
        }
    }
}