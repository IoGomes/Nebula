package Nebula.Android.Nebula_ViewModel.Controllers;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.widget.SwitchCompat;

import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONException;
import org.json.JSONObject;

import Nebula.Android.Nebula_Data.Preferences.SessionPreferences;
import Nebula.Android.Nebula_Model.Services.Svc_Network_Checker;
import Nebula.Android.Nebula_Model.Services.Svc_Permission;
import Nebula.Android.Nebula_Model.UseCases.UseCase_02_Register;
import Nebula.Android.Nebula_View.Activities.Activity_02_Feed;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Auth_Login_Credentials;
import Nebula.Android.Nebula_View.Utils.ToastWarning;
import Nebula.Android.Nebula_ViewModel.Server_Services.Service_Online;
import Nebula.Android.Nebula_ViewModel.Server_Services.Service_SignIn;
import Nebula.Android.Nebula_ViewModel.Server_Services.Service_SignUp;
import Nebula.Android.R;

public class Controller_Auth {

    private static final Controller_Auth instance = new Controller_Auth();

    private static final String SERVER_URL = "https://youlanda-undependable-compressingly.ngrok-free.dev/";

    private Service_Online connection;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void submitUserDataToSignUp(String userName, String userPassword, String email, String phoneNumber, SubmitUserDataToSignUpCallback callback) {
        new Thread(() -> {
            try {
                String response = Service_SignUp.sendUserData(userName, userPassword, email, phoneNumber);

                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    callback.onResult(response);
                });

            } catch (Exception e) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    callback.onError(e);
                });
            }
        }).start();
    }

    public interface SubmitUserDataToSignUpCallback {
        void onResult(String response);

        void onError(Exception e);
    }


    public void submitUserDataToSignIn(String userName, String userPassword, SubmitUserDataToSignInCallback callback) {
        new Thread(() -> {
            try {
                String response = Service_SignIn.sendUserData(userName, userPassword);

                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    callback.onResult(response);
                });

            } catch (Exception e) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    callback.onError(e);
                });
            }
        }).start();
    }

    public interface SubmitUserDataToSignInCallback {
        void onResult(String response);

        void onError(Exception e);
    }


    public Controller_Auth() {
    }

    public static Controller_Auth getInstance() {
        return instance;
    }

    public void responseTreatment(String response, Context context) {
        switch (response) {
            case "201":
                new ToastWarning(context)
                        .showInfo("201");
                break;
            case "409":
                new ToastWarning(context)
                        .showInfo("409");
                break;
            case "500":
                new ToastWarning(context)
                        .showInfo("500");
                break;
            default:
                new ToastWarning(context)
                        .showInfo("Não gerou nenhum resultado");
                break;
        }
    }

    public void handleSignIn(
            Activity activity,
            Button loginButton,
            EditText emailField,
            EditText passwordField,
            View loadAnimation
    ) {
        loginButton.setClickable(false);

        final String email = emailField.getText().toString();
        final String password = passwordField.getText().toString();

        submitUserDataToSignIn(email, password, new SubmitUserDataToSignInCallback() {
            @Override
            public void onResult(String response) {

                try {
                    JSONObject json = new JSONObject(response);

                    String userId = json.getString("userId");
                    String userName = json.getString("userName");
                    initializeConnection(userId, userName);

                    new SessionPreferences(activity).setKeyId(userId, activity);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                new ToastWarning(activity).showInfo("201");

                loginButton.setText(null);
                loadAnimation.setVisibility(View.VISIBLE);

                SessionPreferences prefsData = new SessionPreferences(activity);
                prefsData.setIsLoggedIn(true);

                if (loadAnimation instanceof LottieAnimationView) {
                    ((LottieAnimationView) loadAnimation).playAnimation();
                }

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    activity.startActivity(new Intent(activity, Activity_02_Feed.class));
                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    loginButton.setClickable(true);
                }, 1500);
            }

            @Override
            public void onError(Exception e) {
                new ToastWarning(activity).showInfo("409");
                loginButton.setClickable(true);
            }
        });
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
                        });
                    }

                    @Override
                    public void onDisconnected() {
                        mainHandler.post(() -> {
                            Log.d(TAG, "Desconectado do servidor");


                        });
                    }

                    @Override
                    public void onConnectionError(String error) {
                        mainHandler.post(() -> {
                            Log.e(TAG, "Erro de conexão: " + error);


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

                                    } else {

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
    }


    public void handleRegister(
            Activity activity,
            Button signupButton,
            EditText userNameField,
            EditText userEmailField,
            EditText userPhoneField,
            EditText userPasswordField,
            EditText confirmPasswordField,
            SwitchCompat termosCondicoes,
            SwitchCompat termosPrivacidade,
            View loadAnimation
    ) {
        signupButton.setClickable(false);

        UseCase_02_Register register = new UseCase_02_Register(
                userNameField.getText().toString(),
                userEmailField.getText().toString(),
                userPasswordField.getText().toString(),
                confirmPasswordField.getText().toString(),
                termosCondicoes.isChecked(),
                termosPrivacidade.isChecked(),
                new Svc_Network_Checker(activity),
                new Svc_Permission(activity)
        );

        if (register.isEnabled()) {
            final String userName = userNameField.getText().toString();
            final String userEmail = userEmailField.getText().toString();
            final String userPhoneNumber = userPhoneField.getText().toString();
            final String userPassword = userPasswordField.getText().toString();

            submitUserDataToSignUp(userName, userPassword, userEmail, userPhoneNumber, new SubmitUserDataToSignUpCallback() {
                @Override
                public void onResult(String response) {
                    try {
                        JSONObject json = new JSONObject(response);

                        String userId = json.getString("userId");
                        String userName = json.getString("userName");
                        initializeConnection(userId, userName);

                        new SessionPreferences(activity).setKeyId(userId, activity);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Exception e) {
                    new ToastWarning(activity)
                            .showInfo("409");
                }
            });

            signupButton.setText("");
            loadAnimation.setVisibility(View.VISIBLE);
            SessionPreferences prefsData = new SessionPreferences(activity);
            prefsData.setIsLoggedIn(true);
            if (loadAnimation instanceof LottieAnimationView) {
                ((LottieAnimationView) loadAnimation).playAnimation();
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                activity.startActivity(new Intent(activity, Activity_02_Feed.class));
                signupButton.setClickable(true);
            }, 1500);

        } else {
            new Dialog_Auth_Login_Credentials(activity).show();
            signupButton.setClickable(true);
        }
    }
}

