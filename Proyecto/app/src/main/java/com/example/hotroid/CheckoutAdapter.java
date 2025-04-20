package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder>{
    private ArrayList<Checkout> checkoutList;
    private OnItemClickListener listener;

    public CheckoutAdapter(ArrayList<Checkout> checkoutList) {
        this.checkoutList = checkoutList;
    }
    @Override
    public CheckoutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout, parent, false);
        return new CheckoutViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CheckoutViewHolder holder, int position) {
        Checkout current = checkoutList.get(position);
        holder.tvRoomNumber.setText(current.getRoomNumber());
        holder.tvClientName.setText(current.getClientName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return checkoutList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomNumber, tvClientName;
        ImageView arrowIcon;

        public CheckoutViewHolder(View itemView) {
            super(itemView);
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            arrowIcon = itemView.findViewById(R.id.arrowIcon);
        }
    }
}
