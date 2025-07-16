package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.hotroid.bean.ChatHotelItem;
import java.util.ArrayList;
import java.util.List;

import com.example.hotroid.bean.ChatSession;

public class ChatFragment extends Fragment implements FirestoreChatManager.ChatListListener {

    private RecyclerView recyclerViewChats; // Usar el ID correcto del XML
    private LinearLayout textViewEmptyState; // Cambiar a LinearLayout
    private ChatHotelAdapter chatAdapter;
    private List<ChatHotelItem> chatItems;
    private FirestoreChatManager firestoreChatManager;
    private String currentUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestoreChatManager = FirestoreChatManager.getInstance();

        // Obtener ID del usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_chat, container, false);

        initializeViews(view);
        setupRecyclerView();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadUserChats();
    }

    private void initializeViews(View view) {
        recyclerViewChats = view.findViewById(R.id.recyclerViewChats);
        textViewEmptyState = view.findViewById(R.id.textViewEmptyState);

        chatItems = new ArrayList<>();
    }

    private void setupRecyclerView() {
        // Usar el constructor correcto del adaptador existente
        chatAdapter = new ChatHotelAdapter(getContext(), chatItems);
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChats.setAdapter(chatAdapter);
    }

    private void loadUserChats() {
        if (currentUserId != null) {
            firestoreChatManager.getUserChats(currentUserId, this);
        } else {
            showEmptyState(true);
        }
    }

    private void showEmptyState(boolean show) {
        if (show) {
            recyclerViewChats.setVisibility(View.GONE);
            textViewEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewChats.setVisibility(View.VISIBLE);
            textViewEmptyState.setVisibility(View.GONE);
        }
    }

    // Implementación de ChatListListener
    @Override
    public void onChatsLoaded(List<ChatSession> chats) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                chatItems.clear();

                for (ChatSession chat : chats) {
                    ChatHotelItem item = new ChatHotelItem();
                    item.setChatId(chat.getChatId());
                    item.setHotelId(chat.getHotelId());
                    item.setHotelName(chat.getHotelName());
                    item.setLastMessage(chat.getLastMessage());
                    item.setLastMessageTime(chat.getLastMessageTime());
                    item.setProfileImageRes(R.drawable.hotel_decameron);
                    item.setHasUnreadMessages(chat.getUnreadCount() > 0);
                    item.setUnreadCount(chat.getUnreadCount());
                    chatItems.add(item);
                }

                chatAdapter.notifyDataSetChanged();
                showEmptyState(chatItems.isEmpty());
            });
        }
    }

    @Override
    public void onChatAdded(ChatSession chat) {
        // Manejado por onChatsLoaded
    }

    @Override
    public void onChatUpdated(ChatSession chat) {
        // Manejado por onChatsLoaded
    }

    @Override
    public void onError(String error) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                showEmptyState(true);
            });
        }
    }
}