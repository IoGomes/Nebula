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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Nebula.Android.Nebula_Model.Entitys.Entity_02_Chat_Session;
import Nebula.Android.Nebula_Model.Entitys.Entity_03_Message;
import Nebula.Android.Nebula_Model.Repository.Repo_Chat;
import Nebula.Android.Nebula_Model.Repository.Repo_Chat_Storage;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_01_Profile_Image;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Chat_01_Msg_Adapter;
import Nebula.Android.Nebula_View.Utils.NavBar_Inserts;
import Nebula.Android.Nebula_ViewModel.Controllers.Controller_Video_Call;
import Nebula.Android.Nebula_ViewModel.Controllers.Controller_Voice_Call;
import Nebula.Android.Nebula_WebSocketChat.StompChatService;
import Nebula.Android.R;
import Nebula.Android.databinding.Act03ChatBinding;

/// @author Ítalo Oliveira Gomes

public class Activity_03_Chat extends AppCompatActivity {

    private RV_Chat_01_Msg_Adapter adapter;
    private List<Entity_03_Message> messageList;
    private Act03ChatBinding bind;

    private ExecutorService executorService;
    private Handler mainHandler;

    private StompChatService chatService;
    private String username = "User_" + System.currentTimeMillis();
    private String chatRoomId = "public";

    private List<Entity_02_Chat_Session> chatSessions;

    // ✅ ADICIONAR - Gerenciador de armazenamento
    private Repo_Chat_Storage chatStorage;

    private static final String SERVER_URL = "wss://malinda-poetless-manipulatively.ngrok-free.dev/ws/websocket";
    private static final String TOPIC_PATTERN = "/topic/chat/%s";
    private static final String SEND_DESTINATION = "/app/chat.send";
    private static final String JOIN_DESTINATION = "/app/chat.join";
    private static final String LEAVE_DESTINATION = "/app/chat.leave";

    private int currentChatPosition = 0;
    private String currentChatId;

    private String currentChatWith;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {

        setTheme(androidx.appcompat.R.style.Theme_AppCompat);

        super.onCreate(savedInstanceBundle);

        bind = Act03ChatBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        currentChatPosition = getIntent().getIntExtra("CHAT_POSITION", 0);
        currentChatId = getIntent().getStringExtra("CHAT_ID");

        currentChatWith = getIntent().getStringExtra("ChatWith");
        bind.nomeContato.setText(currentChatWith);

        chatStorage = new Repo_Chat_Storage(this);

        Log.d("NebulaChat", "==================");
        Log.d("NebulaChat", "currentChatId: " + currentChatId);
        Log.d("NebulaChat", "currentChatPosition: " + currentChatPosition);
        Log.d("NebulaChat", "==================");

        setupBasicUI();

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        chatService = new StompChatService();

        bind.getRoot().post(() -> {
            initializeHeavyComponents();
            connectToWebSocket();
        });

        bind.returnButton.setOnClickListener(v -> {
            saveMessagesBeforeExit();
            finish();
        });
    }

    private void setupBasicUI() {

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        messageList = new ArrayList<>();


        if (currentChatId != null && chatStorage.hasSavedMessages(currentChatId)) {
            List<Entity_03_Message> savedMessages = chatStorage.loadMessages(currentChatId);
            messageList.addAll(savedMessages);
            Log.d("NebulaChat", "📂 Carregadas " + savedMessages.size() + " mensagens");
        } else {
            Log.d("NebulaChat", "❌ Nenhuma mensagem salva ou currentChatId é null");
            Log.d("NebulaChat", "currentChatId: " + currentChatId);
        }

        adapter = new RV_Chat_01_Msg_Adapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        bind.rvMessage.setLayoutManager(layoutManager);
        bind.rvMessage.setAdapter(adapter);
        bind.rvMessage.setItemAnimator(null);
        bind.rvMessage.setHasFixedSize(true);

        // ✅ ADICIONAR - Rolar para a última mensagem se houver
        if (!messageList.isEmpty()) {
            bind.rvMessage.scrollToPosition(messageList.size() - 1);
        }

        bind.profilePhoto.setOnClickListener(v ->
                new Dialog_Feed_01_Profile_Image(
                        v.getContext(),
                        currentChatWith // passa o identificador do perfil
                ).show()
        );
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

                    bind.status.setVisibility(VISIBLE);

                    String topic = String.format(TOPIC_PATTERN, chatRoomId);
                    subscribeToChat(topic);

                    chatService.joinChat(JOIN_DESTINATION, username, chatRoomId);
                });
            }

            @Override
            public void onDisconnected() {
                runOnUiThread(() -> {
                    // ✅ ADICIONAR - Salvar mensagens quando desconectar
                    saveMessagesBeforeExit();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showConnectionStatus("Erro: " + error);
                    mainHandler.postDelayed(() -> {
                        if (!chatService.isConnected()) {
                            connectToWebSocket();
                        }
                    }, 1000);
                });
            }
        });
    }

    private void subscribeToChat(String topic) {
        chatService.subscribe(topic, new StompChatService.MessageListener() {
            @Override
            public void onMessageReceived(StompChatService.ChatMessage message) {
                runOnUiThread(() -> {

                    if (!message.getSender().equals(username)) {
                        receiveMessage(message);
                    }
                });
            }
        });
    }

    private void sendMessage(String text) {
        Entity_03_Message newMessage = new Entity_03_Message();
        newMessage.setMessage(text);
        newMessage.setDateTimeMessage(new Date());
        newMessage.setWasVisualized(false);
        newMessage.setIsSentByMe(true);
        newMessage.setSenderName(username); // ✅ ADICIONAR

        messageList.add(newMessage);
        adapter.notifyItemInserted(messageList.size() - 1);
        bind.rvMessage.scrollToPosition(messageList.size() - 1);

        // ✅ ADICIONAR - Salvar mensagem no storage
        if (currentChatId != null) {
            chatStorage.addMessage(currentChatId, newMessage);
            Log.d("NebulaChat", "💾 Mensagem salva no storage");
        }

        if (Repo_Chat.getChats() != null && currentChatPosition < Repo_Chat.getChats().size()) {
            Repo_Chat.getChats().get(currentChatPosition).setLastMessage(text);
            Log.d("NebulaChat", "✅ LastMessage atualizado na posição: " + currentChatPosition);

            if (Repo_Chat.getFeedAdapter() != null) {
                int recyclerViewPosition = currentChatPosition + 2;
                Repo_Chat.getFeedAdapter().notifyItemChanged(recyclerViewPosition);
                Log.d("NebulaChat", "✅ Adapter notificado na posição: " + recyclerViewPosition);
            }
        }

        if (chatService.isConnected()) {
            StompChatService.ChatMessage stompMessage = new StompChatService.ChatMessage(
                    username,
                    text,
                    "CHAT",
                    chatRoomId
            );

            String json = new Gson().toJson(stompMessage);
            Log.d("NebulaChat", "📤 Enviando mensagem: " + json);

            chatService.sendMessage(SEND_DESTINATION, stompMessage);
        } else {
            Log.w("NebulaChat", "⚠️ Não conectado! Tentando reconectar...");
            Toast.makeText(this, "Não conectado. Tentando reconectar...", Toast.LENGTH_SHORT).show();
            connectToWebSocket();
        }
    }

    private void receiveMessage(StompChatService.ChatMessage stompMessage) {
        Entity_03_Message newMessage = new Entity_03_Message();
        newMessage.setMessage(stompMessage.getContent());
        newMessage.setDateTimeMessage(new Date(stompMessage.getTimestamp()));
        newMessage.setWasVisualized(false);
        newMessage.setSenderName(stompMessage.getSender());
        newMessage.setIsSentByMe(false);

        messageList.add(newMessage);
        adapter.notifyItemInserted(messageList.size() - 1);
        bind.rvMessage.scrollToPosition(messageList.size() - 1);

        // ✅ ADICIONAR - Salvar mensagem recebida no storage
        if (currentChatId != null) {
            chatStorage.addMessage(currentChatId, newMessage);
            Log.d("NebulaChat", "💾 Mensagem recebida salva no storage");
        }

        String lastMessageText = stompMessage.getContent();
        if (Repo_Chat.getChats() != null && currentChatPosition < Repo_Chat.getChats().size()) {
            Repo_Chat.getChats().get(currentChatPosition).setLastMessage(lastMessageText);
            Repo_Chat.getChats().get(currentChatPosition).setHasUnread(true);

            Log.d("NebulaChat", "📥 Mensagem recebida na posição: " + currentChatPosition);

            if (Repo_Chat.getFeedAdapter() != null) {
                int recyclerViewPosition = currentChatPosition + 2;
                Repo_Chat.getFeedAdapter().notifyItemChanged(recyclerViewPosition);
                Log.d("NebulaChat", "✅ Feed atualizado na posição: " + recyclerViewPosition);
            }
        }
    }

    // ✅ ADICIONAR - Método para salvar mensagens antes de sair
    private void saveMessagesBeforeExit() {
        if (currentChatId != null && !messageList.isEmpty()) {
            chatStorage.saveMessages(currentChatId, messageList);
            Log.d("NebulaChat", "💾 Todas as mensagens salvas: " + messageList.size());
        }
    }

    // ✅ ADICIONAR - Método para limpar o histórico do chat (opcional)
    private void clearChatHistory() {
        if (currentChatId != null) {
            messageList.clear();
            chatStorage.clearMessages(currentChatId);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Histórico limpo", Toast.LENGTH_SHORT).show();
            Log.d("NebulaChat", "🗑️ Histórico do chat limpo");
        }
    }

    private void showConnectionStatus(String status) {
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
    }

    private void setupClickListeners() {
        bind.videoCall.setOnClickListener(v ->
                new Controller_Video_Call(this).performVideoCall(this));

        bind.voiceCall.setOnClickListener(v ->
                new Controller_Voice_Call(this).performVoiceCall(this));

        bind.send.setOnClickListener(v -> {
            String text = bind.messageTextfield.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
                bind.messageTextfield.setText("");
            }
        });

        // ✅ OPCIONAL - Adicionar botão para limpar histórico (se tiver no XML)
        // bind.clearHistoryButton.setOnClickListener(v -> clearChatHistory());
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

        // ✅ ADICIONAR - Salvar mensagens ao destruir
        saveMessagesBeforeExit();

        if (chatService != null) {
            chatService.leaveChat(LEAVE_DESTINATION, username, chatRoomId);
            chatService.disconnect();
        }

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // ✅ ADICIONAR - Salvar mensagens ao pausar
        saveMessagesBeforeExit();

        // ✅ OPCIONAL - Marcar mensagens como visualizadas
        if (currentChatId != null) {
            chatStorage.markAllAsRead(currentChatId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ✅ ADICIONAR - Marcar mensagens como lidas quando voltar ao chat
        if (currentChatId != null) {
            chatStorage.markAllAsRead(currentChatId);

            // Atualizar o status de não lidas no repositório
            if (Repo_Chat.getChats() != null && currentChatPosition < Repo_Chat.getChats().size()) {
                Repo_Chat.getChats().get(currentChatPosition).setHasUnread(false);

                if (Repo_Chat.getFeedAdapter() != null) {
                    int recyclerViewPosition = currentChatPosition + 2;
                    Repo_Chat.getFeedAdapter().notifyItemChanged(recyclerViewPosition);
                }
            }
        }
    }
}