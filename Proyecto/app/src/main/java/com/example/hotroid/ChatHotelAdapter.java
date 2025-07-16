package com.example.hotroid;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.ChatDetalladoUser;
import com.example.hotroid.R;
import com.example.hotroid.bean.ChatHotelItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatHotelAdapter extends RecyclerView.Adapter<ChatHotelAdapter.ChatHotelViewHolder> {

    private Context context;
    private List<ChatHotelItem> chatList;

    public ChatHotelAdapter(Context context, List<ChatHotelItem> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatHotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_hotel, parent, false);
        return new ChatHotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHotelViewHolder holder, int position) {
        ChatHotelItem chatItem = chatList.get(position);

        holder.hotelName.setText(chatItem.getHotelName());
        holder.lastMessage.setText(chatItem.getLastMessage());
        holder.profileImage.setImageResource(chatItem.getProfileImageRes());

        // Formatear timestamp - CORREGIDO
        if (chatItem.getLastMessageTime() > 0) {
            Date date = new Date(chatItem.getLastMessageTime());
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            holder.timestamp.setText(sdf.format(date));
        } else {
            holder.timestamp.setText("");
        }

        // Mostrar indicador de mensajes no leídos
        if (chatItem.isHasUnreadMessages() && chatItem.getUnreadCount() > 0) {
            holder.unreadBadge.setVisibility(View.VISIBLE);
            holder.unreadCount.setText(String.valueOf(chatItem.getUnreadCount()));
            holder.unreadCount.setVisibility(View.VISIBLE);
        } else {
            holder.unreadBadge.setVisibility(View.GONE);
            holder.unreadCount.setVisibility(View.GONE);
        }

        // Click listener para abrir el chat detallado
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatDetalladoUser.class);
            intent.putExtra("chat_id", chatItem.getChatId()); // Usar getChatId()
            intent.putExtra("hotel_name", chatItem.getHotelName());
            intent.putExtra("hotel_id", chatItem.getHotelId());
            intent.putExtra("profile_image", chatItem.getProfileImageRes());

            // Marcar mensajes como leídos en Firestore
            if (chatItem.getChatId() != null) {
                FirestoreChatManager.getInstance().markAsRead(chatItem.getChatId());
            }

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void updateChatList(List<ChatHotelItem> newChatList) {
        this.chatList = newChatList;
        notifyDataSetChanged();
    }

    public void addOrUpdateChat(ChatHotelItem chatItem) {
        // Buscar si ya existe un chat con este hotel
        for (int i = 0; i < chatList.size(); i++) {
            if (chatList.get(i).getHotelId().equals(chatItem.getHotelId())) {
                // Actualizar chat existente
                chatList.set(i, chatItem);
                // Mover al principio de la lista
                chatList.add(0, chatList.remove(i));
                notifyDataSetChanged();
                return;
            }
        }

        // Si no existe, agregar al principio
        chatList.add(0, chatItem);
        notifyItemInserted(0);
    }

    static class ChatHotelViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView hotelName;
        TextView lastMessage;
        TextView timestamp;
        View unreadBadge;
        TextView unreadCount;

        public ChatHotelViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.chatProfileImage);
            hotelName = itemView.findViewById(R.id.chatHotelName);
            lastMessage = itemView.findViewById(R.id.chatLastMessage);
            timestamp = itemView.findViewById(R.id.chatTimestamp);
            unreadBadge = itemView.findViewById(R.id.unreadBadge);
            unreadCount = itemView.findViewById(R.id.unreadCount);
        }
    }
}