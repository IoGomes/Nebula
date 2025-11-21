package Nebula.Android.Nebula_ViewModel.Services;

import android.util.Log;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Service_Online {

    private static final String TAG = "ConectionToOnlineArray";
    private static Socket socket;
    private String userName;
    private ConnectionListener listener;
    private boolean isConnected = false;

    // Interface para callbacks de conexão
    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
        void onConnectionError(String error);
        void onDataReceived(JSONObject data);
    }

    // URL do servidor - COLOQUE SUA URL DO NGROK AQUI
    private static final String SERVER_URL = "https://youlanda-undependable-compressingly.ngrok-free.dev/";

    // Construtor
    public Service_Online(String userName, ConnectionListener listener) {
        this.userName = userName;
        this.listener = listener;
        initializeSocket();
    }

    // Construtor alternativo com URL customizada
    public Service_Online(String userName, String serverUrl, ConnectionListener listener) {
        this.userName = userName;
        this.listener = listener;
        initializeSocket(serverUrl);
    }

    // Construtor com userId e userName
    public Service_Online(String userId, String userName, String serverUrl, ConnectionListener listener) {
        this.userName = userName;
        this.listener = listener;
        initializeSocket(serverUrl, userId, userName);
    }

    // Inicializa o socket com URL padrão
    private void initializeSocket() {
        initializeSocket(SERVER_URL, null, userName);
    }

    // Inicializa o socket com URL customizada
    private void initializeSocket(String serverUrl) {
        initializeSocket(serverUrl, null, userName);
    }

    // Inicializa o socket com URL customizada e userId/userName
    private void initializeSocket(String serverUrl, String userId, String userName) {
        try {
            IO.Options opts = new IO.Options();

            // Monta a query string com userId e userName
            StringBuilder query = new StringBuilder();

            if (userId != null && !userId.isEmpty()) {
                query.append("userId=").append(userId);
            }

            if (userName != null && !userName.isEmpty()) {
                if (query.length() > 0) {
                    query.append("&");
                }
                query.append("userName=").append(userName);
            }

            opts.query = query.toString();
            opts.reconnection = true;
            opts.reconnectionAttempts = 5;
            opts.reconnectionDelay = 1000;
            opts.timeout = 10000;

            socket = IO.socket(serverUrl, opts);
            setupSocketListeners();

        } catch (URISyntaxException e) {
            Log.e(TAG, "Erro ao criar socket: " + e.getMessage());
            if (listener != null) {
                listener.onConnectionError("URI inválida: " + e.getMessage());
            }
        }
    }

    // Configura os listeners do socket
    private void setupSocketListeners() {
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                isConnected = true;
                Log.d(TAG, "Conectado ao servidor");
                if (listener != null) {
                    listener.onConnected();
                }
            }
        });

        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                isConnected = false;
                Log.d(TAG, "Desconectado do servidor");
                if (listener != null) {
                    listener.onDisconnected();
                }
            }
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                isConnected = false;
                String error = args.length > 0 ? args[0].toString() : "Erro desconhecido";
                Log.e(TAG, "Erro de conexão: " + error);
                if (listener != null) {
                    listener.onConnectionError(error);
                }
            }
        });

        // Listener para receber dados do servidor
        socket.on("data", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0 && args[0] instanceof JSONObject) {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "Dados recebidos: " + data.toString());
                    if (listener != null) {
                        listener.onDataReceived(data);
                    }
                }
            }
        });
    }

    // Conecta ao servidor
    public void connect() {
        if (socket != null && !isConnected) {
            socket.connect();
            Log.d(TAG, "Tentando conectar...");
        } else if (isConnected) {
            Log.d(TAG, "Já está conectado");
        }
    }

    // Desconecta do servidor
    public void disconnect() {
        if (socket != null && isConnected) {
            socket.disconnect();
            Log.d(TAG, "Desconectando...");
        }
    }

    // Envia dados para o servidor
    public void emit(String event, JSONObject data) {
        if (socket != null && isConnected) {
            socket.emit(event, data);
            Log.d(TAG, "Enviando evento: " + event);
        } else {
            Log.w(TAG, "Socket não conectado. Não foi possível enviar o evento: " + event);
        }
    }

    // Verifica se está conectado
    public boolean isConnected() {
        return isConnected;
    }

    // Limpa recursos
    public void destroy() {
        if (socket != null) {
            socket.off();
            socket.disconnect();
            socket = null;
        }
        listener = null;
        Log.d(TAG, "Recursos liberados");
    }

    // Getter para o socket
    public static Socket getSocket() {
        return socket;
    }
}