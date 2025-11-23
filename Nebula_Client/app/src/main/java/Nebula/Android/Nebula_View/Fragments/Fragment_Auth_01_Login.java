package Nebula.Android.Nebula_View.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Nebula.Android.Nebula_View.Activities.Activity_LandingPage;
import Nebula.Android.Nebula_View.Activities.Activity_GitAuth;
import Nebula.Android.Nebula_ViewModel.Controllers.Controller_Auth;
import Nebula.Android.Nebula_ViewModel.Server_Services.Service_Online;
import Nebula.Android.databinding.Frg01LoginBinding;

public class Fragment_Auth_01_Login extends Fragment {

    private static final String TAG = "Fragment_Auth_Login";
    private static final String SERVER_URL = "https://youlanda-undependable-compressingly.ngrok-free.dev/";

    private Frg01LoginBinding bind;
    private Service_Online connection;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        bind = Frg01LoginBinding.inflate(inflater, container, false);

        String forgot = "<u>Forgot Password?</u>";
        bind.textView2.setText(Html.fromHtml(forgot, Html.FROM_HTML_MODE_LEGACY));

        bind.login.setOnClickListener(v -> {
            new Controller_Auth().getInstance().handleSignIn(requireActivity(),
                    bind.login ,
                    bind.emailTextfield,
                    bind.userPasswordTextfield,
                    bind.loadAnimation);
        });

        bind.textView2.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Activity_LandingPage.class);
            startActivity(intent);
        });

        bind.google.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Activity_GitAuth.class);
            startActivity(intent);
        });

        return bind.getRoot();
    }

    public void initializeConnection(String userId, String userName) {

        connection = new Service_Online(
                userId,
                userName,
                SERVER_URL,
                new Service_Online.ConnectionListener() {

                    @Override
                    public void onConnected() {
                        mainHandler.post(() -> {
                            Log.d(TAG, "Conectado ao servidor");
                            Toast.makeText(getContext(), "Conectado ao servidor", Toast.LENGTH_SHORT).show();

                            if (bind != null) {
                                bind.login.setEnabled(true);
                                bind.login.setAlpha(1.0f);
                            }
                        });
                    }

                    @Override
                    public void onDisconnected() {
                        mainHandler.post(() -> {
                            Log.d(TAG, "Desconectado do servidor");
                            Toast.makeText(getContext(), "Desconectado", Toast.LENGTH_SHORT).show();

                            if (bind != null) {
                                bind.login.setEnabled(false);
                                bind.login.setAlpha(0.5f);
                            }
                        });
                    }

                    @Override
                    public void onConnectionError(String error) {
                        mainHandler.post(() -> {
                            Log.e(TAG, "Erro de conexão: " + error);
                            Toast.makeText(getContext(), "Erro ao conectar: " + error, Toast.LENGTH_LONG).show();

                            if (bind != null) {
                                bind.login.setEnabled(false);
                                bind.login.setAlpha(0.5f);
                            }
                        });
                    }

                    @Override
                    public void onDataReceived(JSONObject data) {
                        mainHandler.post(() -> {
                            try {
                                Log.d(TAG, "Dados recebidos: " + data.toString());

                                if (data.has("status")) {
                                    String status = data.getString("status");
                                    String message = data.optString("message", "");

                                    if ("success".equals(status)) {
                                        Toast.makeText(getContext(), "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Erro: " + message, Toast.LENGTH_SHORT).show();
                                    }
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "Erro ao processar dados: " + e.getMessage());
                            }
                        });
                    }
                }
        );

        connection.connect();

        bind.login.setEnabled(false);
        bind.login.setAlpha(0.5f);
    }

    private void enviarDadosLogin(String email) {
        try {
            JSONObject data = new JSONObject();
            data.put("email", email);
            data.put("timestamp", System.currentTimeMillis());
            data.put("action", "login");
            data.put("device", "android");

            connection.emit("enviar_nome", data);

            Log.d(TAG, "Dados enviados: " + data.toString());
            Toast.makeText(getContext(), "Enviando dados...", Toast.LENGTH_SHORT).show();

            showLoading(true);

            mainHandler.postDelayed(() -> {
                showLoading(false);
            }, 2000);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao enviar dados: " + e.getMessage());
            Toast.makeText(getContext(), "Erro ao enviar dados", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        if (bind != null) {
            bind.login.setEnabled(!show);
            bind.login.setText(show ? "Enviando..." : "Login");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (connection != null && !connection.isConnected()) {
            connection.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Limpa a conexão Socket.IO
        if (connection != null) {
            connection.destroy();
            connection = null;
        }

        bind = null;
        executor.shutdownNow();
    }
}