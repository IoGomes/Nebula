package Nebula.Android.Nebula_ViewModel.Server_Services;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketManager {

    private Socket mSocket;
    private static final String TAG = "SocketManager";

    public void connect(String serverUrl, String myUserId, String myUserName) {
        try {
            IO.Options options = new IO.Options();
            options.transports = new String[]{"websocket"};
            options.query = "userId=" + myUserId + "&userName=" + myUserName;

            mSocket = IO.socket(serverUrl, options);

            mSocket.on(Socket.EVENT_CONNECT, args -> {
                Log.d(TAG, "✅ Conectado com Sucesso! ID: " + mSocket.id());
            });

            // notificação via server
            mSocket.on("offerResponse", args -> {
                JSONObject data = (JSONObject) args[0];
                processIncomingCall(data);
            });


            mSocket.on(Socket.EVENT_CONNECT_ERROR, args -> {
                Log.e(TAG, "❌ Erro de conexão: " + args[0]);
            });

            mSocket.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void processIncomingCall(JSONObject data) {
        try {

            String notificationType = data.optString("notificationType");

            if ("incoming_call".equals(notificationType)) {

                String callerName = data.optString("offererUserName", "Desconhecido");
                String callType = data.optString("callType", "video");
                String sdp = data.getString("sdp");

                Log.i(TAG, "🔔 GATILHO: Chamada recebida de " + callerName);

                //chamar função
            }

        } catch (JSONException e) {
            Log.e(TAG, "Erro ao ler JSON: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off();
        }
    }
}