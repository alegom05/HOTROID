package com.example.hotroid;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Hotel;
import com.example.hotroid.bean.ChatBotResponse;
import com.example.hotroid.chatbot.ChatBotManager;
import com.example.hotroid.chatbot.HotelChatBot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatDetalladoUser extends AppCompatActivity {

    // Vistas del toolbar
    private ImageView backButton;
    private CircleImageView chatAvatarToolbar;
    private TextView chatNameToolbar;
    private TextView chatStatusToolbar;

    // Vistas del chat
    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private ImageView sendButton;

    // Adaptador y datos
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    // ChatBot
    private HotelChatBot chatBot;
    private Hotel hotel;

    // Datos del chat
    private String chatId;
    private String hotelName;
    private String hotelId;

    private String hotelRating;
    private String hotelDescription;

    private String hotelDireccion;
    private int profileImageRes;

    // Para gestión del teclado
    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*EdgeToEdge.enable(this);*/

        // Configurar la ventana para ajustarse al teclado
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setContentView(R.layout.user_chat_detallado);

        // Configurar window insets
        /*findViewById(R.id.toolbar).setOnApplyWindowInsetsListener((v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });*/

        // Obtener datos del intent
        getIntentData();

        // Inicializar vistas
        initViews();

        // Configurar listeners
        setupListeners();

        // Configurar detección del teclado
        setupKeyboardDetection();

        // Configurar RecyclerView de mensajes
        setupMessagesRecyclerView();


    }

    private void getIntentData() {
        chatId = getIntent().getStringExtra("chat_id");
        hotelName = getIntent().getStringExtra("hotel_name");
        hotelId = getIntent().getStringExtra("hotel_id");
        profileImageRes = getIntent().getIntExtra("profile_image", R.drawable.hotel_decameron);

        loadCompleteHotelData();
    }

    private void loadCompleteHotelData() {
        if (hotelId != null) {
            FirebaseFirestore.getInstance()
                    .collection("hoteles")
                    .document(hotelId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            hotel = documentSnapshot.toObject(Hotel.class);
                            if (hotel != null) {
                                hotel.setIdHotel(hotelId);

                                // Configurar datos del toolbar
                                setupToolbarData();

                                // Inicializar ChatBot con datos completos
                                initializeChatBot();

                                // Iniciar conversación
                                startChatBotConversation();
                            }
                        } else {
                            Toast.makeText(this, "Hotel no encontrado", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al cargar datos del hotel", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }

    private void initViews() {
        // Toolbar
        backButton = findViewById(R.id.backButton);
        chatAvatarToolbar = findViewById(R.id.chatAvatarToolbar);
        chatNameToolbar = findViewById(R.id.chatNameToolbar);
        chatStatusToolbar = findViewById(R.id.chatStatusToolbar);

        // Chat
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
    }

    private void setupListeners() {
        // Botón de retroceso
        backButton.setOnClickListener(v -> finish());

        // Botón de enviar mensaje
        sendButton.setOnClickListener(v -> sendMessage());


        // Click en el EditText para mostrar teclado
        messageEditText.setOnClickListener(v -> {
            showKeyboard(messageEditText);
            scrollToBottomDelayed();
        });

        // Focus listener para mostrar teclado automáticamente
        messageEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showKeyboard(messageEditText);
                scrollToBottomDelayed();
            }
        });

        // Enter en el EditText para enviar mensaje
        messageEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void setupKeyboardDetection() {
        final View rootView = findViewById(android.R.id.content);
        keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.05) {
                    // Teclado visible
                    scrollToBottomDelayed();
                } else {
                    // Teclado oculto
                }
            }
        };
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
    }

    private void setupMessagesRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Para mostrar mensajes desde abajo

        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void setupToolbarData() {
        chatNameToolbar.setText(hotelName);
        chatStatusToolbar.setText("Asistente Virtual ");
        chatAvatarToolbar.setImageResource(profileImageRes);
    }

    private void initializeChatBot() {
        chatBot = ChatBotManager.getInstance().getChatBot(hotel);
    }

    private void startChatBotConversation() {
        // Agregar mensaje de bienvenida del chatbot
        ChatBotResponse welcomeResponse = chatBot.getWelcomeMessage();
        addBotMessage(welcomeResponse.getContent());
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();

        if (messageText.isEmpty()) {
            return;
        }

        // Agregar mensaje del usuario
        addUserMessage(messageText);

        // Limpiar el EditText
        messageEditText.setText("");

        // Procesar respuesta del chatbot
        processChatBotResponse(messageText);
    }

    private void processChatBotResponse(String userInput) {
        // Mostrar indicador de "escribiendo..."
        showTypingIndicator();

        // Simular delay de respuesta del bot (más realista)
        messagesRecyclerView.postDelayed(() -> {
            hideTypingIndicator();

            chatBot.processUserInput(userInput, new HotelChatBot.ChatBotCallback() {
                @Override
                public void onResponse(ChatBotResponse response) {
                    runOnUiThread(() -> {
                        addBotMessage(response.getContent());
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        addBotMessage("❌ Lo siento, ocurrió un error al procesar tu solicitud. " +
                                "Por favor intenta nuevamente o contacta atención al cliente.");
                        Toast.makeText(ChatDetalladoUser.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }, 1500); // 1.5 segundos de delay
    }

    private void addUserMessage(String message) {
        Message userMessage = new Message();
        userMessage.setContent(message);
        userMessage.setFromCurrentUser(true);
        userMessage.setTimestamp(getCurrentTimestamp());

        messageList.add(userMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        Message botMessage = new Message();
        botMessage.setContent(message);
        botMessage.setFromCurrentUser(false);
        botMessage.setTimestamp(getCurrentTimestamp());
        botMessage.setSenderName("Asistente " + hotelName);

        messageList.add(botMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void showTypingIndicator() {
        Message typingMessage = new Message();
        typingMessage.setContent("Escribiendo...");
        typingMessage.setFromCurrentUser(false);
        typingMessage.setTimestamp(getCurrentTimestamp());
        typingMessage.setSenderName("Asistente " + hotelName);
        typingMessage.setTyping(true); // Necesitarás agregar este campo a la clase Message

        messageList.add(typingMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void hideTypingIndicator() {
        // Remover el último mensaje si es un indicador de "escribiendo"
        if (!messageList.isEmpty()) {
            Message lastMessage = messageList.get(messageList.size() - 1);
            if (lastMessage.isTyping()) {
                messageList.remove(messageList.size() - 1);
                messageAdapter.notifyItemRemoved(messageList.size());
            }
        }
    }

    private void showKeyboard(EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void scrollToBottom() {
        if (messageList.size() > 0) {
            messagesRecyclerView.smoothScrollToPosition(messageList.size() - 1);
        }
    }

    private void scrollToBottomDelayed() {
        messagesRecyclerView.postDelayed(() -> {
            if (messageList.size() > 0) {
                messagesRecyclerView.smoothScrollToPosition(messageList.size() - 1);
            }
        }, 300);
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Limpiar el listener del teclado
        if (keyboardLayoutListener != null) {
            View rootView = findViewById(android.R.id.content);
            if (rootView != null) {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
            }
        }
    }
}