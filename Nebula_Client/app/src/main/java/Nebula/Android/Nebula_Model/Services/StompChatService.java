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
        private String senderId;
        private String receiverId;

        public ChatMessage(String sender, String content, String type, String roomId) {
            this.sender = sender;
            this.content = content;
            this.type = type;
            this.roomId = roomId;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public String getSender() { return sender; }
        public String getContent() { return content; }
        public String getType() { return type; }
        public String getRoomId() { return roomId; }
        public long getTimestamp() { return timestamp; }
        public String getSenderId() { return senderId; }
        public String getReceiverId() { return receiverId; }

        // Setters
        public void setSender(String sender) { this.sender = sender; }
        public void setContent(String content) { this.content = content; }
        public void setType(String type) { this.type = type; }
        public void setRoomId(String roomId) { this.roomId = roomId; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public void setSenderId(String senderId) { this.senderId = senderId; }
        public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    }

    /**
     * Conecta ao servidor WebSocket
     */
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

    /**
     * Verifica se está conectado
     */
    public boolean isConnected() {
        return connected && stompClient != null && stompClient.isConnected();
    }

    /**
     * Desconecta do servidor
     */
    public void disconnect() {
        if (stompClient != null) {
            stompClient.disconnect();
            connected = false;
            Log.d(TAG, "🔌 Desconectado do servidor");
        }
    }

    /**
     * Entra em uma room privada
     * @param destination Endpoint do servidor (ex: /app/chat.joinPrivate)
     * @param senderId ID do usuário que está entrando
     * @param receiverId ID do outro usuário da conversa
     * @param username Nome de exibição do usuário
     */
    public void joinPrivateChat(String destination, String senderId, String receiverId, String username) {
        if (!isConnected()) {
            Log.w(TAG, "⚠️ Tentativa de join sem conexão ativa");
            return;
        }

        ChatMessage message = new ChatMessage(username, "", "JOIN", "");
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        sendMessage(destination, message);

        Log.d(TAG, "👤 Join private chat - Sender: " + senderId + ", Receiver: " + receiverId);
    }

    /**
     * Sai de uma room privada
     * @param destination Endpoint do servidor (ex: /app/chat.leavePrivate)
     * @param senderId ID do usuário que está saindo
     * @param receiverId ID do outro usuário da conversa
     * @param username Nome de exibição do usuário
     */
    public void leavePrivateChat(String destination, String senderId, String receiverId, String username) {
        if (!isConnected()) {
            Log.w(TAG, "⚠️ Tentativa de leave sem conexão ativa");
            return;
        }

        ChatMessage message = new ChatMessage(username, "", "LEAVE", "");
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        sendMessage(destination, message);

        Log.d(TAG, "👋 Leave private chat - Sender: " + senderId + ", Receiver: " + receiverId);
    }

    /**
     * Envia mensagem para a room privada
     * @param destination Endpoint do servidor (ex: /app/chat.sendPrivate)
     * @param message Mensagem a ser enviada
     */
    public void sendMessage(String destination, ChatMessage message) {
        if (isConnected()) {
            String payload = gson.toJson(message);
            Log.d(TAG, "📤 Enviando: " + payload);
            stompClient.send(destination, payload).subscribe(
                    () -> Log.d(TAG, "✅ Mensagem enviada com sucesso"),
                    error -> Log.e(TAG, "❌ Erro ao enviar mensagem", error)
            );
        } else {
            Log.w(TAG, "⚠️ Tentativa de envio sem conexão ativa");
        }
    }

    /**
     * Inscreve-se em uma room privada específica
     * @param topic Tópico da room (ex: /topic/chat/room/user123_user456)
     * @param listener Callback para receber mensagens
     */
    public void subscribe(String topic, MessageListener listener) {
        if (stompClient == null) {
            Log.e(TAG, "❌ STOMP client não inicializado");
            return;
        }

        Log.d(TAG, "📡 Inscrevendo-se no tópico: " + topic);

        stompClient.topic(topic)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        msg -> {
                            try {
                                Log.d(TAG, "📨 Mensagem recebida: " + msg.getPayload());
                                ChatMessage message = gson.fromJson(msg.getPayload(), ChatMessage.class);
                                listener.onMessageReceived(message);
                            } catch (Exception e) {
                                Log.e(TAG, "❌ Erro ao processar mensagem JSON: " + msg.getPayload(), e);
                            }
                        },
                        error -> Log.e(TAG, "❌ Erro na inscrição do tópico", error)
                );
    }

    /**
     * Envia indicador de digitação
     * @param destination Endpoint do servidor (ex: /app/chat.typing)
     * @param senderId ID do usuário que está digitando
     * @param receiverId ID do usuário que receberá a notificação
     */
    public void sendTypingIndicator(String destination, String senderId, String receiverId) {
        if (!isConnected()) {
            return;
        }

        ChatMessage typingMsg = new ChatMessage("", "", "TYPING", "");
        typingMsg.setSenderId(senderId);
        typingMsg.setReceiverId(receiverId);
        sendMessage(destination, typingMsg);

        Log.d(TAG, "⌨️ Typing indicator - Sender: " + senderId + ", Receiver: " + receiverId);
    }
}