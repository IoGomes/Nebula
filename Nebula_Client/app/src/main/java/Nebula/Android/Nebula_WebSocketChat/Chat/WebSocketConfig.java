package Nebula.Android.Nebula_WebSocketChat.Chat;

import android.util.Log;

import com.google.gson.Gson;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class WebSocketConfig {

    private static final String TAG = "StompChatService";

    private StompClient stompClient;
    private CompositeDisposable compositeDisposable;
    private Gson gson;
    private boolean isConnected = false;

    // Callback interfaces
    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }

    public interface MessageListener {
        void onMessageReceived(ChatMessage message);
    }

    // Model de mensagem
    public static class ChatMessage {
        private String sender;
        private String content;
        private String type;
        private long timestamp;

        public ChatMessage() {}

        public ChatMessage(String sender, String content, String type) {
            this.sender = sender;
            this.content = content;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }

        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public WebSocketConfig() {
        compositeDisposable = new CompositeDisposable();
        gson = new Gson();
    }

    /**
     * Conecta ao servidor WebSocket/STOMP
     * @param serverUrl URL do servidor (ex: "ws://192.168.1.100:8080/ws")
     * @param listener Callback de conexão
     */
    public void connect(String serverUrl, ConnectionListener listener) {
        if (isConnected) {
            Log.w(TAG, "Já está conectado");
            return;
        }

        // Cria o cliente STOMP
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, serverUrl);

        // Listener do ciclo de vida da conexão
        Disposable lifecycleDisposable = stompClient.lifecycle()
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.d(TAG, "STOMP connection opened");
                            isConnected = true;
                            if (listener != null) {
                                listener.onConnected();
                            }
                            break;

                        case CLOSED:
                            Log.d(TAG, "STOMP connection closed");
                            isConnected = false;
                            if (listener != null) {
                                listener.onDisconnected();
                            }
                            break;

                        case ERROR:
                            Log.e(TAG, "STOMP connection error", lifecycleEvent.getException());
                            isConnected = false;
                            if (listener != null) {
                                String errorMsg = lifecycleEvent.getException() != null ?
                                        lifecycleEvent.getException().getMessage() : "Erro desconhecido";
                                listener.onError(errorMsg);
                            }
                            break;

                        case FAILED_SERVER_HEARTBEAT:
                            Log.w(TAG, "Failed server heartbeat");
                            break;
                    }
                });

        compositeDisposable.add(lifecycleDisposable);

        // Conecta ao servidor
        stompClient.connect();
    }

    /**
     * Inscreve-se em um tópico para receber mensagens
     * @param topic Tópico STOMP (ex: "/topic/public")
     * @param listener Callback para mensagens recebidas
     */
    public void subscribe(String topic, MessageListener listener) {
        if (!isConnected || stompClient == null) {
            Log.e(TAG, "Não conectado. Conecte-se primeiro.");
            return;
        }

        Disposable topicDisposable = stompClient.topic(topic)
                .subscribe(topicMessage -> {
                    try {
                        Log.d(TAG, "Mensagem recebida: " + topicMessage.getPayload());

                        ChatMessage message = gson.fromJson(
                                topicMessage.getPayload(),
                                ChatMessage.class
                        );

                        if (listener != null) {
                            listener.onMessageReceived(message);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar mensagem", e);
                    }
                }, throwable -> {
                    Log.e(TAG, "Erro na inscrição do tópico: " + topic, throwable);
                });

        compositeDisposable.add(topicDisposable);
        Log.d(TAG, "Inscrito no tópico: " + topic);
    }

    /**
     * Envia uma mensagem para o servidor
     * @param destination Destino STOMP (ex: "/app/chat.send")
     * @param message Mensagem a ser enviada
     */
    public void sendMessage(String destination, ChatMessage message) {
        if (!isConnected || stompClient == null) {
            Log.e(TAG, "Não conectado. Não é possível enviar mensagem.");
            return;
        }

        try {
            String jsonMessage = gson.toJson(message);
            stompClient.send(destination, jsonMessage).subscribe(
                    () -> Log.d(TAG, "Mensagem enviada com sucesso"),
                    throwable -> Log.e(TAG, "Erro ao enviar mensagem", throwable)
            );
        } catch (Exception e) {
            Log.e(TAG, "Erro ao serializar mensagem", e);
        }
    }

    /**
     * Envia mensagem de entrada no chat
     * @param destination Destino (ex: "/app/chat.addUser")
     * @param username Nome do usuário
     */
    public void joinChat(String destination, String username) {
        ChatMessage joinMessage = new ChatMessage(username, "", "JOIN");
        sendMessage(destination, joinMessage);
    }

    /**
     * Envia mensagem de saída do chat
     * @param destination Destino (ex: "/app/chat.removeUser")
     * @param username Nome do usuário
     */
    public void leaveChat(String destination, String username) {
        ChatMessage leaveMessage = new ChatMessage(username, "", "LEAVE");
        sendMessage(destination, leaveMessage);
    }

    /**
     * Desconecta do servidor
     */
    public void disconnect() {
        if (stompClient != null) {
            stompClient.disconnect();
            stompClient = null;
        }

        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }

        isConnected = false;
        Log.d(TAG, "Desconectado do servidor");
    }

    /**
     * Verifica se está conectado
     */
    public boolean isConnected() {
        return isConnected && stompClient != null;
    }

    /**
     * Reconecta ao servidor
     */
    public void reconnect(String serverUrl, ConnectionListener listener) {
        disconnect();
        connect(serverUrl, listener);
    }
}