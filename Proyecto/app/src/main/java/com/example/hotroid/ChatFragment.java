package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
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

public class ChatFragment extends Fragment implements FirestoreChatManager.ChatListListener, ChatHotelAdapter.OnChatDeleteListener {

    private static final String TAG = "ChatFragment";
    private RecyclerView recyclerViewChats;
    private LinearLayout textViewEmptyState;
    private ChatHotelAdapter chatAdapter;
    private List<ChatHotelItem> chatItems;
    private FirestoreChatManager firestoreChatManager;
    private String currentUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");

        firestoreChatManager = FirestoreChatManager.getInstance();

        // Obtener ID del usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            Log.d(TAG, "Usuario actual: " + currentUserId);
        } else {
            Log.w(TAG, "No hay usuario autenticado");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called");
        View view = inflater.inflate(R.layout.user_chat, container, false);

        initializeViews(view);
        setupRecyclerView();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called");
        loadUserChats();
    }

    private void initializeViews(View view) {
        recyclerViewChats = view.findViewById(R.id.recyclerViewChats);
        textViewEmptyState = view.findViewById(R.id.textViewEmptyState);

        chatItems = new ArrayList<>();

        Log.d(TAG, "Views initialized");
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatHotelAdapter(getContext(), chatItems);
        chatAdapter.setOnChatDeleteListener(this); // Configurar listener de eliminación
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewChats.setAdapter(chatAdapter);

        Log.d(TAG, "RecyclerView configured");
    }

    private void loadUserChats() {
        Log.d(TAG, "loadUserChats() called");

        if (currentUserId != null) {
            Log.d(TAG, "Cargando chats para usuario: " + currentUserId);
            showEmptyState(false); // Ocultar estado vacío mientras carga
            firestoreChatManager.getUserChats(currentUserId, this);
        } else {
            Log.w(TAG, "No se puede cargar chats: usuario no autenticado");
            showEmptyState(true);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Debes iniciar sesión para ver tus chats", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showEmptyState(boolean show) {
        Log.d(TAG, "showEmptyState: " + show);
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
        Log.d(TAG, "onChatsLoaded called with " + chats.size() + " chats");

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                chatItems.clear();

                for (ChatSession chat : chats) {
                    Log.d(TAG, "Procesando chat: " + chat.getHotelName());

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

                Log.d(TAG, "Total items agregados: " + chatItems.size());
                chatAdapter.notifyDataSetChanged();
                showEmptyState(chatItems.isEmpty());

                if (chatItems.isEmpty()) {
                    Toast.makeText(getContext(), "No tienes chats activos", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onChatAdded(ChatSession chat) {
        Log.d(TAG, "onChatAdded: " + chat.getHotelName());
    }

    @Override
    public void onChatUpdated(ChatSession chat) {
        Log.d(TAG, "onChatUpdated: " + chat.getHotelName());
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "Error en ChatFragment: " + error);

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                showEmptyState(true);
                Toast.makeText(getContext(), "Error al cargar chats: " + error, Toast.LENGTH_LONG).show();
            });
        }
    }

    // Implementación de OnChatDeleteListener
    @Override
    public void onChatDelete(String chatId, int position) {
        Log.d(TAG, "onChatDelete: chatId=" + chatId + ", position=" + position);

        // Eliminar del adapter inmediatamente para mejorar UX
        chatAdapter.removeChat(position);

        // Eliminar de Firestore
        firestoreChatManager.deleteChat(chatId, new FirestoreChatManager.ChatDeletionListener() {
            @Override
            public void onChatDeleted() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Chat eliminado correctamente", Toast.LENGTH_SHORT).show();

                        // Verificar si la lista está vacía después de eliminar
                        if (chatItems.isEmpty()) {
                            showEmptyState(true);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al eliminar chat: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error al eliminar chat: " + error, Toast.LENGTH_LONG).show();
                        // Recargar chats en caso de error
                        loadUserChats();
                    });
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() - reloading chats");
        loadUserChats(); // Recargar chats cuando volvemos al fragment
    }
}