package Nebula.Android.Nebula_View.Fragments;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_02_Contact_Adapter.Adapter_Mode.MODE_A;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Nebula.Android.Nebula_Data.Repository.Repo_Contact;
import Nebula.Android.Nebula_Model.Entitys.Entity_Contact;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_02_Contact_Adapter;
import Nebula.Android.Nebula_ViewModel.Controllers.Controller_Contact;
import Nebula.Android.Nebula_ViewModel.Server_Services.Service_QrCode;
import Nebula.Android.databinding.Frg04ContactListBinding;

/// @author Ítalo Oliveira Gomes
public class Fragment_Feed_02_Contacts extends Fragment {

    private Frg04ContactListBinding bind;
    private RV_Feed_02_Contact_Adapter adapter;

    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        loadContacts();
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        bind = Frg04ContactListBinding.inflate(inflater, container, false);

        bind.recyclerViewContact.setLayoutManager(new LinearLayoutManager(getContext()));
        bind.recyclerViewContact.setHasFixedSize(true);


        String nullMessage = "Empty <font color='#FFFFFF'><b>Contact list</b></font> → Tap <font " +
                "color='#FFFFFF'><b>+</b></font> to add contacts.";
        bind.nullMessage.setText(Html.fromHtml(nullMessage, Html.FROM_HTML_MODE_LEGACY));

        bind.startChat.setOnClickListener(v -> openAddContactDialog());
        bind.qrCode.setOnClickListener(v -> openQrCodeDialog());

        return bind.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void loadContacts() {
        executorService.execute(() -> {

            var contacts = Controller_Contact.loadContactFromRepo();
            boolean isEmpty = contacts.isEmpty();

            RV_Feed_02_Contact_Adapter newAdapter =
                    new RV_Feed_02_Contact_Adapter(contacts, MODE_A);

            mainHandler.post(() -> {
                if (bind != null) {
                    adapter = newAdapter;
                    Repo_Contact.setFeedAdapter(adapter);
                    bind.recyclerViewContact.setAdapter(adapter);
                    updateUI(isEmpty);
                }
            });
        });
    }

    private void openAddContactDialog() {
        new Controller_Contact().openAddContactDialog(requireContext()).show();
    }
    private void openQrCodeDialog() {
        new Controller_Contact().openQrCodeDialog(requireContext()).show();
    }

    private void refreshContacts() {
        executorService.execute(() -> {
            var contacts = Repo_Contact.getContacts();
            boolean isEmpty = contacts.isEmpty();

            mainHandler.post(() -> {
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                    updateUI(isEmpty);
                }
            });
        });
    }

    private void updateUI(boolean isEmpty) {
        if (bind == null) return;

        if (isEmpty) {
            bind.nullMessage.setVisibility(VISIBLE);
            bind.recyclerViewContact.setVisibility(GONE);
        } else {
            bind.nullMessage.setVisibility(GONE);
            bind.recyclerViewContact.setVisibility(VISIBLE);
        }
    }

    public RV_Feed_02_Contact_Adapter getAdapter() {
        return adapter;
    }
}