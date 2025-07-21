package com.example.hotroid;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImagenHabitacionAdapterUser extends RecyclerView.Adapter<ImagenHabitacionAdapterUser.ImagenViewHolder> {
    private final List<String> listaImagenes;
    public ImagenHabitacionAdapterUser(List<String> imagenes){
        this.listaImagenes=imagenes;
    }
    @NonNull
    @Override
    public ImagenViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){
        View vista= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_habitacion_user, parent, false);
        return new ImagenViewHolder(vista);
    }
    @Override
    public void onBindViewHolder(@NonNull ImagenViewHolder holder, int position){
        String url = listaImagenes.get(position);
        Glide.with(holder.itemView.getContext())
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.hotel_room)
                .into(holder.imageView);
        Log.d("GlideDebug", "Cargando imagen: " + url);
    }

    @Override
    public int getItemCount(){
        return listaImagenes.size();
    }
    public static class ImagenViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        public ImagenViewHolder(@NonNull View itemView){
            super(itemView);
            imageView=itemView.findViewById(R.id.imgHabitacionSlide);
        }
    }

}
