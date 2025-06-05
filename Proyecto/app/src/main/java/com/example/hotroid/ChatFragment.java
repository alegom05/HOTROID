package com.example.hotroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment implements ChatAdapter.OnChatClickListener {

    private RecyclerView chatRecyclerView;
    private LinearLayout emptyChatView;
    private ChatAdapter chatAdapter;
    private List<ChatItem> chatList;

    public ChatFragment() {
        // Constructor público vacío obligatorio
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout
        View view = inflater.inflate(R.layout.user_chat, container, false);

        // Inicializar vistas
        initViews(view);

        // Configurar RecyclerView
        setupRecyclerView();

        // Cargar datos de ejemplo
        loadSampleData();

        return view;
    }

    private void initViews(View view) {
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        emptyChatView = view.findViewById(R.id.emptyChatView);
    }

    private void setupRecyclerView() {
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList, this);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void loadSampleData() {
        // Datos de ejemplo - reemplaza con tus datos reales
        List<ChatItem> sampleChats = new ArrayList<>();

        sampleChats.add(new ChatItem(
                "1",
                "Hotel Barcelona Center",
                "Hola, quería confirmar los detalles de mi reserva para el fin de semana. ¿Podría ayudarme con la hora del check-in?",
                "10:35",
                true,
                R.drawable.hotel_decameron
        ));

        sampleChats.add(new ChatItem(
                "2",
                "Hotel Decameron Cartagena",
                "Gracias por su reserva. Su habitación estará lista a partir de las 3:00 PM",
                "09:20",
                false,
                R.drawable.hotel_decameron
        ));

        sampleChats.add(new ChatItem(
                "3",
                "Resort Paradise",
                "Bienvenido a Paradise Resort. ¿En qué podemos ayudarle?",
                "Ayer",
                true,
                R.drawable.hotel_decameron
        ));

        sampleChats.add(new ChatItem(
                "4",
                "Hotel Boutique Lima",
                "Su reserva ha sido confirmada para el 15 de junio",
                "Lun",
                false,
                R.drawable.hotel_decameron
        ));

        updateChatList(sampleChats);
    }

    private void updateChatList(List<ChatItem> chats) {
        if (chats.isEmpty()) {
            showEmptyView();
        } else {
            showChatList();
            chatAdapter.updateChats(chats);
        }
    }

    private void showEmptyView() {
        chatRecyclerView.setVisibility(View.GONE);
        emptyChatView.setVisibility(View.VISIBLE);
    }

    private void showChatList() {
        chatRecyclerView.setVisibility(View.VISIBLE);
        emptyChatView.setVisibility(View.GONE);
    }

    @Override
    public void onChatClick(ChatItem chat) {
        // Abrir la actividad del chat detallado
        if (getContext() != null) {
            android.content.Intent intent = new android.content.Intent(getContext(), ChatDetalladoUser.class);
            intent.putExtra("chat_id", chat.getChatId());
            intent.putExtra("hotel_name", chat.getHotelName());
            intent.putExtra("profile_image", chat.getProfileImageRes());
            intent.putExtra("last_message", chat.getLastMessage());
            startActivity(intent);
        }
    }

    // Método público para actualizar la lista desde fuera del fragmento
    public void refreshChats(List<ChatItem> newChats) {
        updateChatList(newChats);
    }

    // Método para agregar un nuevo chat
    public void addNewChat(ChatItem newChat) {
        chatList.add(0, newChat); // Agregar al inicio
        chatAdapter.notifyItemInserted(0);
        chatRecyclerView.scrollToPosition(0);
        showChatList();
    }
}