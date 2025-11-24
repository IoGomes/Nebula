package Nebula.Android.Nebula_View.Activities;

import static android.view.View.VISIBLE;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Nebula.Android.Nebula_Data.Preferences.SessionPreferences;
import Nebula.Android.Nebula_Data.Repository.Repo_Chat;
import Nebula.Android.Nebula_Data.Repository.Repo_Chat_Storage;
import Nebula.Android.Nebula_Model.Entitys.Entity_Chat;
import Nebula.Android.Nebula_Model.Entitys.Entity_Message;
import Nebula.Android.Nebula_Model.Services.StompChatService;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_Profile_Image;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Chat_01_Msg_Adapter;
import Nebula.Android.Nebula_View.Utils.NavBar_Inserts;
import Nebula.Android.Nebula_ViewModel.Controllers.Controller_Video_Call;
import Nebula.Android.Nebula_ViewModel.Controllers.Controller_Voice_Call;
import Nebula.Android.R;
import Nebula.Android.databinding.Act03ChatBinding;

public class Activity_03_Chat extends AppCompatActivity {

    private static final String TAG = "Activity_03_Chat";

    private RV_Chat_01_Msg_Adapter adapter;
    private List<Entity_Message> messageList;
    private Act03ChatBinding bind;

    private ExecutorService executorService;
    private Handler mainHandler;

    private StompChatService chatService;
    private Repo_Chat_Storage chatStorage;

    private static final String SERVER_URL = "wss://malinda-poetless-manipulatively.ngrok-free.dev/ws/websocket";

    private static final String PRIVATE_TOPIC_PATTERN = "/user/%s/queue/messages";
    private static final String SEND_DESTINATION = "/app/chat.sendPrivate";
    private static final String JOIN_DESTINATION = "/app/chat.joinPrivate";
    private static final String LEAVE_DESTINATION = "/app/chat.leavePrivate";

    private String CHAT_ID;
    private int CHAT_POS;
    private String SENDER_ID;
    private String RECEIVER_ID;
    private String RECEIVER_NAME;
    private String RECEIVER_NUMBER;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        setTheme(androidx.appcompat.R.style.Theme_AppCompat);

        bind = Act03ChatBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        CHAT_ID = getIntent().getStringExtra("CHAT_ID");
        CHAT_POS = getIntent().getIntExtra("CHAT_POS", 0);
        SENDER_ID = new SessionPreferences(this).getKeyId();
        RECEIVER_ID = getIntent().getStringExtra("RECEIVER_ID");
        RECEIVER_NAME = getIntent().getStringExtra("RECEIVER_NAME");
        RECEIVER_NUMBER = getIntent().getStringExtra("RECEIVER_NUMBER");

        setupUI();

        chatStorage = new Repo_Chat_Storage(this);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        chatService = new StompChatService();

        loadSavedMessages();

        bind.getRoot().post(() -> {
            initializeHeavyComponents();
            connectToWebSocket();
        });

        bind.returnButton.setOnClickListener(v -> {
            saveMessagesBeforeExit();
            finish();
        });
    }

    private String generateRoomId(String userId1, String userId2) {
        List<String> ids = Arrays.asList(userId1, userId2);
        Collections.sort(ids);
        return String.join("_", ids);
    }

    private void setupUI() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        Objects.requireNonNull(getSupportActionBar()).hide();

        bind.nomeContato.setText(RECEIVER_NAME);
        bind.contactNumber.setText(RECEIVER_NUMBER);

        messageList = new ArrayList<>();
        adapter = new RV_Chat_01_Msg_Adapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        bind.rvMessage.setLayoutManager(layoutManager);
        bind.rvMessage.setAdapter(adapter);
        bind.rvMessage.setItemAnimator(null);
        bind.rvMessage.setHasFixedSize(true);

        bind.profilePhoto.setOnClickListener(v ->
                new Dialog_Feed_Profile_Image(
                        v.getContext(),
                        SENDER_ID, RECEIVER_ID, RECEIVER_NAME, RECEIVER_NUMBER
                ).show()
        );
    }

    // ✅ ADICIONAR: Carregar mensagens salvas
    private void loadSavedMessages() {
        if (CHAT_ID != null && chatStorage.hasSavedMessages(CHAT_ID)) {
            List<Entity_Message> savedMessages = chatStorage.loadMessages(CHAT_ID);
            messageList.addAll(savedMessages);
            adapter.notifyDataSetChanged();

            if (!messageList.isEmpty()) {
                bind.rvMessage.scrollToPosition(messageList.size() - 1);
            }

            Log.d(TAG, "📂 Carregadas " + savedMessages.size() + " mensagens do storage");
        }
    }

    private void initializeHeavyComponents() {
        executorService.execute(() -> {
            mainHandler.post(() -> setTheme(androidx.appcompat.R.style.Theme_AppCompat));
        });

        View rootLayout = findViewById(R.id.root);
        NavBar_Inserts.adjustPaddingForNavigationBar(rootLayout, this);

        setupClickListeners();
        setupTextWatcher();
        setupKeyboardListener();
    }

    private void connectToWebSocket() {
        chatService.connect(SERVER_URL, new StompChatService.ConnectionListener() {
            @Override
            public void onConnected() {
                runOnUiThread(() -> {
                    Log.d(TAG, "✅ Conectado ao servidor");
                    bind.status.setVisibility(VISIBLE);

                    // ✅ Gera o roomId normalizado
                    String roomId = generateRoomId(SENDER_ID, RECEIVER_ID);
                    String roomTopic = "/topic/chat/room/" + roomId;

                    Log.d(TAG, "🔑 Room ID: " + roomId);
                    Log.d(TAG, "📡 Inscrevendo em: " + roomTopic);

                    // ✅ Inscreve-se no tópico da room
                    chatService.subscribe(roomTopic, new StompChatService.MessageListener() {
                        @Override
                        public void onMessageReceived(StompChatService.ChatMessage message) {
                            runOnUiThread(() -> {
                                Log.d(TAG, "📨 Mensagem recebida na room");
                                Log.d(TAG, "   De: " + message.getSenderId());
                                Log.d(TAG, "   Conteúdo: " + message.getContent());

                                // Ignora próprias mensagens
                                if (!SENDER_ID.equals(message.getSenderId())) {
                                    receiveMessage(message);
                                } else {
                                    Log.d(TAG, "⏭️ Ignorando própria mensagem");
                                }
                            });
                        }
                    });

                    // Join na room
                    chatService.joinPrivateChat(JOIN_DESTINATION, SENDER_ID, RECEIVER_ID, RECEIVER_NAME);
                });
            }

            @Override
            public void onDisconnected() {
                runOnUiThread(() -> {
                    Log.d(TAG, "🔌 Desconectado do servidor");
                    bind.status.setVisibility(View.GONE);
                    saveMessagesBeforeExit();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "❌ Erro de conexão: " + error);
                    bind.status.setVisibility(View.GONE);

                    // Tentar reconectar após 2 segundos
                    mainHandler.postDelayed(() -> {
                        if (!chatService.isConnected()) {
                            Log.d(TAG, "🔄 Tentando reconectar...");
                            connectToWebSocket();
                        }
                    }, 2000);
                });
            }
        });
    }

    // ✅ RENOMEADO: subscribeToPrivateChat
    private void subscribeToPrivateChat(String privateTopic) {
        chatService.subscribe(privateTopic, new StompChatService.MessageListener() {
            @Override
            public void onMessageReceived(StompChatService.ChatMessage message) {
                runOnUiThread(() -> {
                    Log.d(TAG, "📨 Mensagem privada recebida de: " + message.getSenderId());

                    // ✅ CORRIGIDO: Verificar se é mensagem do outro usuário
                    if (!SENDER_ID.equals(message.getSenderId())) {
                        receiveMessage(message);
                    } else {
                        Log.d(TAG, "⏭️ Ignorando própria mensagem");
                    }
                });
            }
        });
    }

    private void sendMessage(String text) {
        Entity_Message newMessage = new Entity_Message();
        newMessage.setMessage(text);
        newMessage.setDateTimeMessage(new Date());
        newMessage.setWasVisualized(false);
        newMessage.setIsSentByMe(true);
        newMessage.setSenderName(RECEIVER_NAME); // Nome do destinatário

        messageList.add(newMessage);
        adapter.notifyItemInserted(messageList.size() - 1);
        bind.rvMessage.scrollToPosition(messageList.size() - 1);

        // Salvar no storage
        if (CHAT_ID != null) {
            chatStorage.addMessage(CHAT_ID, newMessage);
            Log.d(TAG, "💾 Mensagem salva no storage");
        }

        // Atualizar última mensagem no repositório
        if (Repo_Chat.getChats() != null && CHAT_POS < Repo_Chat.getChats().size()) {
            Repo_Chat.getChats().get(CHAT_POS).setLastMessageSend(text);

            if (Repo_Chat.getFeedAdapter() != null) {
                int recyclerViewPosition = CHAT_POS + 2;
                Repo_Chat.getFeedAdapter().notifyItemChanged(recyclerViewPosition);
                Log.d(TAG, "✅ Feed atualizado");
            }
        }

        // Enviar via WebSocket
        if (chatService.isConnected()) {

            String roomId = CHAT_ID != null ? CHAT_ID : SENDER_ID + "_" + RECEIVER_ID;

            StompChatService.ChatMessage stompMessage =
                    new StompChatService.ChatMessage(
                            SENDER_ID,
                            text,
                            "CHAT",
                            roomId
                    );
            stompMessage.setSenderId(SENDER_ID);
            stompMessage.setReceiverId(RECEIVER_ID);
            stompMessage.setContent(text);
            stompMessage.setType("CHAT");
            stompMessage.setTimestamp(System.currentTimeMillis());

            String json = new Gson().toJson(stompMessage);
            Log.d(TAG, "📤 Enviando mensagem privada: " + json);

            chatService.sendMessage(SEND_DESTINATION, stompMessage);
        } else {
            Log.w(TAG, "⚠️ Não conectado! Tentando reconectar...");
            connectToWebSocket();
        }
    }

    private void receiveMessage(StompChatService.ChatMessage stompMessage) {
        Entity_Message newMessage = new Entity_Message();
        newMessage.setMessage(stompMessage.getContent());
        newMessage.setDateTimeMessage(new Date(stompMessage.getTimestamp()));
        newMessage.setWasVisualized(false);
        newMessage.setSenderName(RECEIVER_NAME);
        newMessage.setIsSentByMe(false);

        messageList.add(newMessage);
        adapter.notifyItemInserted(messageList.size() - 1);
        bind.rvMessage.scrollToPosition(messageList.size() - 1);

        // Salvar no storage
        if (CHAT_ID != null) {
            chatStorage.addMessage(CHAT_ID, newMessage);
            Log.d(TAG, "💾 Mensagem recebida salva no storage");
        }

        createChatIfNotExists(stompMessage);

        String lastMessageText = stompMessage.getContent();
        if (Repo_Chat.getChats() != null && CHAT_POS < Repo_Chat.getChats().size()) {
            Repo_Chat.getChats().get(CHAT_POS).setLastMessageSend(lastMessageText);
            Repo_Chat.getChats().get(CHAT_POS).setRead(true);

            if (Repo_Chat.getFeedAdapter() != null) {
                int recyclerViewPosition = CHAT_POS + 2;
                Repo_Chat.getFeedAdapter().notifyItemChanged(recyclerViewPosition);
                Log.d(TAG, "✅ Feed atualizado");
            }
        }
    }

    private void createChatIfNotExists(StompChatService.ChatMessage stompMessage) {
        // Verifica se o chat já existe
        boolean chatExists = false;
        if (Repo_Chat.getChats() != null) {
            for (Entity_Chat chat : Repo_Chat.getChats()) {
                if (chat.getChatId() != null && chat.getChatId().equals(CHAT_ID)) {
                    chatExists = true;
                    break;
                }
            }
        }

        // Se não existir, cria um novo
        if (!chatExists) {
            Entity_Chat newChat = new Entity_Chat();
            newChat.setChatId(CHAT_ID);
            newChat.setReceiverName(stompMessage.getSender());
            newChat.setReceiverNumber(stompMessage.getSenderId());
            newChat.setLastMessageSend(stompMessage.getContent());
            newChat.setRead(true); // Marca como não lido
            newChat.setFavorite(false);

            // Adiciona usando o método que já existe
            Repo_Chat.addChat(newChat);

            Log.d("NebulaChat", "✅ Novo chat criado no feed: " + CHAT_ID);
        }
    }


    private void saveMessagesBeforeExit() {
        if (CHAT_ID != null && !messageList.isEmpty()) {
            chatStorage.saveMessages(CHAT_ID, messageList);
            Log.d(TAG, "💾 Todas as mensagens salvas: " + messageList.size());
        }
    }

    private void setupClickListeners() {
        bind.videoCall.setOnClickListener(v -> {
            new Controller_Video_Call(this).performVideoCall(
                    this, SENDER_ID, RECEIVER_ID, RECEIVER_NAME, RECEIVER_NUMBER
            );
        });

        bind.voiceCall.setOnClickListener(v -> {
            new Controller_Voice_Call(this).performVoiceCall(
                    this, SENDER_ID, RECEIVER_ID, RECEIVER_NAME, RECEIVER_NUMBER
            );
        });

        bind.send.setOnClickListener(v -> {
            String text = bind.messageTextfield.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
                bind.messageTextfield.setText("");
            }
        });
    }

    private void setupTextWatcher() {
        bind.messageTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void setupKeyboardListener() {
        View rootView = findViewById(android.R.id.content);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int lastKeypadHeight = 0;

            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);

                int screenHeight = rootView.getHeight();
                int keypadHeight = screenHeight - rect.bottom;

                if (Math.abs(keypadHeight - lastKeypadHeight) < 10) {
                    return;
                }
                lastKeypadHeight = keypadHeight;

                ConstraintLayout.LayoutParams layoutParams =
                        (ConstraintLayout.LayoutParams) bind.bottomBar.getLayoutParams();

                layoutParams.bottomMargin = keypadHeight > screenHeight * 0.15
                        ? keypadHeight
                        : 0;

                bind.bottomBar.setLayoutParams(layoutParams);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "🗑️ Destruindo activity");

        saveMessagesBeforeExit();

        if (chatService != null) {
            chatService.leavePrivateChat(
                    LEAVE_DESTINATION,
                    SENDER_ID,
                    RECEIVER_ID,
                    RECEIVER_NAME
            );
            chatService.disconnect();
        }

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "⏸️ Activity pausada");

        saveMessagesBeforeExit();

        if (CHAT_ID != null) {
            chatStorage.markAllAsRead(CHAT_ID);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "▶️ Activity resumida");

        if (CHAT_ID != null) {
            chatStorage.markAllAsRead(CHAT_ID);

            if (Repo_Chat.getChats() != null && CHAT_POS < Repo_Chat.getChats().size()) {
                Repo_Chat.getChats().get(CHAT_POS).setRead(false);

                if (Repo_Chat.getFeedAdapter() != null) {
                    int recyclerViewPosition = CHAT_POS + 2;
                    Repo_Chat.getFeedAdapter().notifyItemChanged(recyclerViewPosition);
                }
            }
        }
    }
}