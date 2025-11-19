package Nebula.Android.Nebula_Model.Services;

import android.util.Log;

import com.google.gson.Gson;

import io.reactivex.android.schedulers.AndroidSchedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

@SuppressWarnings("SpellCheckingInspection")
public class StompChatService {

    private static final String TAG = "StompChatService";
    private StompClient stompClient;
    private Gson gson = new Gson();

    private boolean connected = false;

    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }

    public interface MessageListener {
        void onMessageReceived(ChatMessage message);
    }

    public static class ChatMessage {
        private String sender;
        private String content;
        private String type;
        private String roomId;
        private long timestamp;

        public ChatMessage(String sender, String content, String type, String roomId) {
            this.sender = sender;
            this.content = content;
            this.type = type;
            this.roomId = roomId;
            this.timestamp = System.currentTimeMillis();
        }

        public String getSender() { return sender; }
        public String getContent() { return content; }
        public String getType() { return type; }
        public String getRoomId() { return roomId; }
        public long getTimestamp() { return timestamp; }
    }

    public void connect(String serverUrl, ConnectionListener listener) {
        try {
            if (stompClient != null && connected) {
                listener.onConnected();
                return;
            }

            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, serverUrl);
            stompClient.connect();

            stompClient.lifecycle()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(event -> {
                        switch (event.getType()) {
                            case OPENED:
                                connected = true;
                                Log.d(TAG, "✅ Conectado ao servidor STOMP");
                                listener.onConnected();
                                break;
                            case ERROR:
                                connected = false;
                                Log.e(TAG, "❌ Erro na conexão", event.getException());
                                listener.onError(event.getException() != null
                                        ? event.getException().getMessage()
                                        : "Erro desconhecido");
                                break;
                            case CLOSED:
                                connected = false;
                                Log.d(TAG, "🔒 Conexão encerrada");
                                listener.onDisconnected();
                                break;
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Erro ao conectar", e);
            listener.onError(e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected && stompClient != null && stompClient.isConnected();
    }

    public void disconnect() {
        if (stompClient != null) {
            stompClient.disconnect();
            connected = false;
        }
    }

    public void sendMessage(String destination, ChatMessage message) {
        if (isConnected()) {
            String payload = gson.toJson(message);
            stompClient.send(destination, payload).subscribe();
        } else {
            Log.w(TAG, "⚠️ Tentativa de envio sem conexão ativa");
        }
    }

    public void subscribe(String topic, MessageListener listener) {
        if (stompClient == null) {
            Log.e(TAG, "❌ STOMP client não inicializado");
            return;
        }

        stompClient.topic(topic)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msg -> {
                    try {
                        ChatMessage message = gson.fromJson(msg.getPayload(), ChatMessage.class);
                        listener.onMessageReceived(message);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar mensagem JSON: " + msg.getPayload(), e);
                    }
                });
    }

    public void joinChat(String destination, String username, String roomId) {
        if (isConnected()) {
            ChatMessage joinMsg = new ChatMessage(username, username + " entrou no chat", "JOIN", roomId);
            sendMessage(destination, joinMsg);
        }
    }

    public void leaveChat(String destination, String username, String roomId) {
        if (isConnected()) {
            ChatMessage leaveMsg = new ChatMessage(username, username + " saiu do chat", "LEAVE", roomId);
            sendMessage(destination, leaveMsg);
        }
    }
}

