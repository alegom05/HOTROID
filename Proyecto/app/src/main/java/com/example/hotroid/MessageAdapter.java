package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;
    private static final int VIEW_TYPE_TYPING = 3;

    private List<Message> messageList;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);

        if (message.isTyping()) {
            return VIEW_TYPE_TYPING;
        }

        return message.isFromUser() ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case VIEW_TYPE_USER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_user, parent, false);
                break;
            case VIEW_TYPE_TYPING:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_typing, parent, false);
                break;
            default: // VIEW_TYPE_BOT
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_hotel, parent, false);
                break;
        }

        return new MessageViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void updateMessages(List<Message> newMessages) {
        this.messageList = newMessages;
        notifyDataSetChanged();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView timestampText;
        private TextView senderNameText;
        private ProgressBar typingIndicator;
        private int viewType;

        public MessageViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;

            messageText = itemView.findViewById(R.id.messageText);
            timestampText = itemView.findViewById(R.id.timestampText);

            // Solo para mensajes del bot
            if (viewType == VIEW_TYPE_BOT) {
                senderNameText = itemView.findViewById(R.id.senderNameText);
            }

            // Solo para indicador de typing
            if (viewType == VIEW_TYPE_TYPING) {
                typingIndicator = itemView.findViewById(R.id.typingIndicator);
            }
        }

        public void bind(Message message) {
            // Para mensajes de typing
            if (viewType == VIEW_TYPE_TYPING) {
                if (typingIndicator != null) {
                    typingIndicator.setVisibility(View.VISIBLE);
                }
                if (messageText != null) {
                    messageText.setText(message.getContent());
                }
                if (timestampText != null) {
                    timestampText.setText(message.getTimestamp());
                }
                return;
            }

            // Para mensajes normales
            if (messageText != null) {
                messageText.setText(message.getContent());
            }

            if (timestampText != null) {
                timestampText.setText(message.getTimestamp());
            }

            // Solo para mensajes del bot
            if (viewType == VIEW_TYPE_BOT && senderNameText != null) {
                if (message.getSenderName() != null && !message.getSenderName().isEmpty()) {
                    senderNameText.setText(message.getSenderName());
                    senderNameText.setVisibility(View.VISIBLE);
                } else {
                    senderNameText.setVisibility(View.GONE);
                }
            }
        }
    }
}