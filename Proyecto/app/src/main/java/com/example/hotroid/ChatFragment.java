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

import com.example.hotroid.bean.ChatHotelItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView chatRecyclerView;
    private LinearLayout emptyChatView;
    private ChatHotelAdapter chatAdapter;
    private List<ChatHotelItem> chatList;

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
        // Pasar el contexto como primer parámetro
        chatAdapter = new ChatHotelAdapter(getContext(), chatList);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void loadSampleData() {
        // Datos de ejemplo - reemplaza con tus datos reales
        List<ChatHotelItem> sampleChats = new ArrayList<>();

        // Crear objetos ChatHotelItem con el constructor correcto
        ChatHotelItem chat1 = new ChatHotelItem();
        chat1.setHotelId("1");
        chat1.setHotelName("Hotel Barcelona Center");
        chat1.setLastMessage("Hola, quería confirmar los detalles de mi reserva para el fin de semana. ¿Podría ayudarme con la hora del check-in?");
        chat1.setLastMessageTime(new Date());
        chat1.setHasUnreadMessages(true);
        chat1.setUnreadCount(2);
        chat1.setProfileImageRes(R.drawable.hotel_decameron);
        sampleChats.add(chat1);

        ChatHotelItem chat2 = new ChatHotelItem();
        chat2.setHotelId("2");
        chat2.setHotelName("Hotel Decameron Cartagena");
        chat2.setLastMessage("Gracias por su reserva. Su habitación estará lista a partir de las 3:00 PM");
        chat2.setLastMessageTime(new Date(System.currentTimeMillis() - 3600000)); // 1 hora atrás
        chat2.setHasUnreadMessages(false);
        chat2.setUnreadCount(0);
        chat2.setProfileImageRes(R.drawable.hotel_decameron);
        sampleChats.add(chat2);

        ChatHotelItem chat3 = new ChatHotelItem();
        chat3.setHotelId("3");
        chat3.setHotelName("Resort Paradise");
        chat3.setLastMessage("Bienvenido a Paradise Resort. ¿En qué podemos ayudarle?");
        chat3.setLastMessageTime(new Date(System.currentTimeMillis() - 86400000)); // 1 día atrás
        chat3.setHasUnreadMessages(true);
        chat3.setUnreadCount(1);
        chat3.setProfileImageRes(R.drawable.hotel_decameron);
        sampleChats.add(chat3);

        ChatHotelItem chat4 = new ChatHotelItem();
        chat4.setHotelId("4");
        chat4.setHotelName("Hotel Boutique Lima");
        chat4.setLastMessage("Su reserva ha sido confirmada para el 15 de junio");
        chat4.setLastMessageTime(new Date(System.currentTimeMillis() - 172800000)); // 2 días atrás
        chat4.setHasUnreadMessages(false);
        chat4.setUnreadCount(0);
        chat4.setProfileImageRes(R.drawable.hotel_decameron);
        sampleChats.add(chat4);

        updateChatList(sampleChats);
    }

    private void updateChatList(List<ChatHotelItem> chats) {
        if (chats.isEmpty()) {
            showEmptyView();
        } else {
            showChatList();
            chatAdapter.updateChatList(chats);
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
}