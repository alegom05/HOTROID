package com.example.hotroid;

import android.os.Bundle;
import android.view.View;
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
    private ImageView attachButton;

    // Adaptador y datos
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    // Datos del chat
    private String chatId;
    private String hotelName;
    private int profileImageRes;
    private boolean isChatbot;

    // Chatbot
    private ChatbotManager chatbotManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_chat_detallado);

        // Configurar window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener datos del intent
        getIntentData();

        // Inicializar vistas
        initViews();

        // Configurar listeners
        setupListeners();

        // Configurar RecyclerView de mensajes
        setupMessagesRecyclerView();

        // Configurar datos del toolbar
        setupToolbarData();

        // Cargar mensajes de ejemplo
        loadSampleMessages();
    }

    private void getIntentData() {
        chatId = getIntent().getStringExtra("chat_id");
        hotelName = getIntent().getStringExtra("hotel_name");
        profileImageRes = getIntent().getIntExtra("profile_image", R.drawable.hotel_decameron);
        isChatbot = getIntent().getBooleanExtra("is_chatbot", false);

        // Valores por defecto si no se reciben datos
        if (hotelName == null) {
            hotelName = "Hotel";
        }
        if (chatId == null) {
            chatId = "default_chat";
        }

        // Inicializar chatbot si es necesario
        if (isChatbot) {
            chatbotManager = ChatbotManager.getInstance();
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
        attachButton = findViewById(R.id.attachButton);
    }

    private void setupListeners() {
        // Botón de retroceso
        backButton.setOnClickListener(v -> finish());

        // Botón de enviar mensaje
        sendButton.setOnClickListener(v -> sendMessage());

        // Botón de adjuntar (placeholder)
        attachButton.setOnClickListener(v -> {
            Toast.makeText(this, "Función de adjuntar próximamente", Toast.LENGTH_SHORT).show();
        });

        // Enter en el EditText para enviar mensaje
        messageEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void setupMessagesRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Mostrar mensajes desde abajo

        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void setupToolbarData() {
        chatNameToolbar.setText(hotelName);
        chatStatusToolbar.setText(isChatbot ? "Asistente Virtual - En línea" : "En línea");
        chatAvatarToolbar.setImageResource(profileImageRes);
    }

    private void loadSampleMessages() {
        // Mensajes de ejemplo - reemplaza con datos reales
        List<Message> sampleMessages = new ArrayList<>();

        if (isChatbot) {
            // Mensaje de bienvenida del chatbot
            sampleMessages.add(new Message(
                    "welcome",
                    chatbotManager.getWelcomeMessage(),
                    getCurrentTimestamp(),
                    false, // es del chatbot
                    Message.MessageType.CHATBOT_OPTIONS
            ));
        } else {
            // Mensajes normales de ejemplo
            sampleMessages.add(new Message(
                    "1",
                    "Hola, espero que esté bien. Quería confirmar los detalles de mi reserva.",
                    getCurrentTimestamp(),
                    true, // es del usuario
                    Message.MessageType.TEXT
            ));

            sampleMessages.add(new Message(
                    "2",
                    "¡Hola! Claro, estaré encantado de ayudarle con su reserva. ¿Podría proporcionarme su número de confirmación?",
                    getCurrentTimestamp(),
                    false, // es del hotel
                    Message.MessageType.TEXT
            ));

            sampleMessages.add(new Message(
                    "3",
                    "Sí, claro. El número de confirmación es HTL-2024-001234",
                    getCurrentTimestamp(),
                    true,
                    Message.MessageType.TEXT
            ));

            sampleMessages.add(new Message(
                    "4",
                    "Perfecto, encontré su reserva. Está confirmada para el 15-17 de junio, habitación doble con vista al mar. ¿Hay algo específico que necesite saber?",
                    getCurrentTimestamp(),
                    false,
                    Message.MessageType.TEXT
            ));
        }

        updateMessagesList(sampleMessages);
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();

        if (!messageText.isEmpty()) {
            // Crear nuevo mensaje
            Message newMessage = new Message(
                    String.valueOf(System.currentTimeMillis()),
                    messageText,
                    getCurrentTimestamp(),
                    true, // es del usuario
                    Message.MessageType.TEXT
            );

            // Agregar a la lista
            messageList.add(newMessage);
            messageAdapter.notifyItemInserted(messageList.size() - 1);

            // Scroll al último mensaje
            messagesRecyclerView.scrollToPosition(messageList.size() - 1);

            // Limpiar el EditText
            messageEditText.setText("");

            // Procesar respuesta según el tipo de chat
            if (isChatbot) {
                processChatbotResponse(messageText);
            } else {
                // Simular respuesta del hotel después de 2 segundos
                simulateHotelResponse();
            }
        }
    }

    private void simulateHotelResponse() {
        // Simular respuesta automática del hotel
        messagesRecyclerView.postDelayed(() -> {
            String[] responses = {
                    "Gracias por su mensaje. Un representante le responderá pronto.",
                    "Entendido. ¿Hay algo más en lo que pueda ayudarle?",
                    "Perfecto. Hemos tomado nota de su solicitud.",
                    "Muchas gracias. Esperamos verle pronto en nuestro hotel."
            };

            String randomResponse = responses[(int) (Math.random() * responses.length)];

            Message hotelResponse = new Message(
                    String.valueOf(System.currentTimeMillis()),
                    randomResponse,
                    getCurrentTimestamp(),
                    false, // es del hotel
                    Message.MessageType.TEXT
            );

            messageList.add(hotelResponse);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            messagesRecyclerView.scrollToPosition(messageList.size() - 1);

        }, 2000); // 2 segundos de delay
    }

    private void processChatbotResponse(String userInput) {
        // Procesar la entrada del usuario con el chatbot
        if (chatbotManager != null) {
            // Simular un hotelId y userId - en una implementación real estos vendrían del contexto de la aplicación
            String hotelId = chatId.replace("chatbot_", ""); // Extraer hotelId del chatId
            String userId = "current_user_id"; // Esto debería venir del usuario autenticado

            chatbotManager.processUserInput(userInput, hotelId, userId, new ChatbotManager.ChatbotCallback() {
                @Override
                public void onResponse(String response) {
                    // Mostrar respuesta del chatbot
                    runOnUiThread(() -> {
                        Message botResponse = new Message(
                                String.valueOf(System.currentTimeMillis()),
                                response,
                                getCurrentTimestamp(),
                                false, // es del chatbot
                                Message.MessageType.CHATBOT_RESPONSE
                        );

                        messageList.add(botResponse);
                        messageAdapter.notifyItemInserted(messageList.size() - 1);
                        messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                    });
                }

                @Override
                public void onError(String error) {
                    // Mostrar mensaje de error
                    runOnUiThread(() -> {
                        Message errorResponse = new Message(
                                String.valueOf(System.currentTimeMillis()),
                                "❌ " + error,
                                getCurrentTimestamp(),
                                false, // es del chatbot
                                Message.MessageType.CHATBOT_RESPONSE
                        );

                        messageList.add(errorResponse);
                        messageAdapter.notifyItemInserted(messageList.size() - 1);
                        messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                    });
                }

                @Override
                public void onShowOptions(List<ChatbotOption> options) {
                    // Mostrar opciones del menú principal
                    runOnUiThread(() -> {
                        Message optionsResponse = new Message(
                                String.valueOf(System.currentTimeMillis()),
                                chatbotManager.getWelcomeMessage(),
                                getCurrentTimestamp(),
                                false, // es del chatbot
                                Message.MessageType.CHATBOT_OPTIONS
                        );

                        messageList.add(optionsResponse);
                        messageAdapter.notifyItemInserted(messageList.size() - 1);
                        messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                    });
                }
            });
        }
    }

    private void updateMessagesList(List<Message> messages) {
        messageList.clear();
        messageList.addAll(messages);
        messageAdapter.notifyDataSetChanged();

        // Scroll al último mensaje
        if (!messageList.isEmpty()) {
            messagesRecyclerView.scrollToPosition(messageList.size() - 1);
        }
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
}