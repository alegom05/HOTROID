package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatItem> chatList;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(ChatItem chat);
    }

    public ChatAdapter(List<ChatItem> chatList, OnChatClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatItem chat = chatList.get(position);
        holder.bind(chat);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void updateChats(List<ChatItem> newChats) {
        this.chatList = newChats;
        notifyDataSetChanged();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        private ImageView profileImage;
        private TextView chatName;
        private TextView timestamp;
        private TextView lastMessage;
        private View unreadIndicator;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            chatName = itemView.findViewById(R.id.chatName);
            timestamp = itemView.findViewById(R.id.timestamp);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onChatClick(chatList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(ChatItem chat) {
            chatName.setText(chat.getHotelName());
            timestamp.setText(chat.getTimestamp());
            lastMessage.setText(chat.getLastMessage());

            // Mostrar/ocultar indicador de no leído
            unreadIndicator.setVisibility(chat.isUnread() ? View.VISIBLE : View.GONE);

            // Configurar imagen del hotel (puedes personalizar según tus necesidades)
            if (chat.getProfileImageRes() != 0) {
                profileImage.setImageResource(chat.getProfileImageRes());
            } else {
                profileImage.setImageResource(R.drawable.hotel_decameron); // imagen por defecto
            }
        }
    }
}