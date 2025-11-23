package Nebula.Android.Nebula_ViewModel.Controllers;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;

import Nebula.Android.Nebula_Data.Repository.Repo_Contact;
import Nebula.Android.Nebula_Model.Entitys.Entity_Contact;
import Nebula.Android.Nebula_View.Activities.Activity_02_Feed;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_Add_Contact;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_QrCode;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_02_Contact_Adapter;
import Nebula.Android.Nebula_ViewModel.Server_Services.Service_QrCode;

public class Controller_Contact {

    public interface onContactsChanged {
    }

    private WeakReference<Activity> activityRef;

    private ExecutorService executorService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    RV_Feed_02_Contact_Adapter adapter;

    public void setAdapter(RV_Feed_02_Contact_Adapter adapter) {
        this.adapter = adapter;
    }
    public RV_Feed_02_Contact_Adapter getAdapter() {
        return adapter;
    }

    public Repo_Contact initializeContactRepo(Context context) {
        return Repo_Contact.initialize(context);
    }
    public static List<Entity_Contact> loadContactFromRepo() {
        return Repo_Contact.getContacts();
    }

    public static void addContactOnRepo(Entity_Contact contact) {
        Repo_Contact.addContact(contact);
    }
    public static void removeContactFromRepo(Entity_Contact contact, int position, Context context) {
        Repo_Contact.removeContact(contact, position);
        removeContactActivityNotification(context);
    }

    public void onQrCodeRead(int qrCode) {

        submitQrCodeToServer(qrCode, new SubmitQrCodeToServerCallback() {
            @Override
            public void onResult(String response) {

                    try {
                        JSONObject json = new JSONObject(response);

                        if (!json.has("userData")) {
                            Log.e("QR", "response sem userData");
                            return;
                        }

                        JSONObject userData = json.optJSONObject("userData");
                        if (userData == null) {
                            Log.e("QR", "userData não é um objeto");
                            return;
                        }

                        if (!userData.has("user")) {
                            Log.e("QR", "userData sem user");
                            return;
                        }

                        JSONObject user = userData.optJSONObject("user");
                        if (user == null) {
                            Log.e("QR", "user não é um objeto");
                            return;
                        }

                        if (!user.has("user_id") || !user.has("username") || !user.has("phone_number")) {
                            Log.e("QR", "Campos obrigatórios faltando");
                            return;
                        }

                        String userId = user.optString("user_id", "");
                        String userNumber = user.optString("phone_number", "");
                        String userName = user.optString("username", "");

                        if (userId.isEmpty() || userName.isEmpty()) {
                            Log.e("QR", "Valores inválidos no JSON");
                            return;
                        }
                        addContactOnRepo(new Entity_Contact(userId, userName, userNumber));

                        mainHandler.post(() -> {

                            Activity activity = getActivity();
                            addedContactActivityNotification();
                            if (activity != null) {

                            }
                        });

                    } catch (Exception e) {
                        Log.e("QR", "Erro ao tratar QR: " + e.getMessage());
                    }
                }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }
    public void submitQrCodeToServer(int userId, SubmitQrCodeToServerCallback callback) {
        new Thread(() -> {
            try {
                String response = Service_QrCode.sendUserData(userId);

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
    public interface SubmitQrCodeToServerCallback {
        void onResult(String response);

        void onError(Exception e);
    }

    public Dialog_Feed_Add_Contact openAddContactDialog(Context context) {
        return new Dialog_Feed_Add_Contact(context);
    }
    public Dialog_Feed_QrCode openQrCodeDialog(Context context) {
        return new Dialog_Feed_QrCode(context);
    }

    private Activity getActivity() {
        return activityRef != null ? activityRef.get() : null;
    }

    public void addedContactActivityNotification(){
        Activity activity = getActivity();
        if (activity instanceof Activity_02_Feed) {
            Activity_02_Feed feed = (Activity_02_Feed) activity;
            feed.addFromFragment02(); }
    }
    public static void removeContactActivityNotification(Context context){
        if (context instanceof Activity_02_Feed) {
            Activity_02_Feed feed = (Activity_02_Feed) context;
            feed.hideOptionsBar();
        }
    }
}
