package Nebula.Android.Nebula_View.Fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import Nebula.Android.Nebula_Model.Entitys.Entity_02_Chat_Session;
import Nebula.Android.Nebula_Model.Entitys.Entity_06_Contact;
import Nebula.Android.Nebula_View.Activities.Activity_03_Chat;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_06_Choose_Contact;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_01_Chat_Adapter;
import Nebula.Android.databinding.Frg03FeedBinding;

public class Fragment_Feed_01_Inbox extends Fragment {

    Frg03FeedBinding bind;
    private List<Entity_02_Chat_Session> chatSessions;
    private RV_Feed_01_Chat_Adapter adapter;
    private List<Entity_06_Contact> contacts;

    @Nullable
    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        bind = Frg03FeedBinding.inflate(inflater, container, false);

        chatSessions = new ArrayList<>();
        contacts = new ArrayList<>();

        List<String> users2 = Arrays.asList("userC", "userD");
        List<Date> date = Arrays.asList(new Date());

        chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));
        chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));chatSessions.add(new Entity_02_Chat_Session(
                "chat",
                users2,
                date,
                "Versão 1.0 -> 03/10/2025"
        ));

        bind.nullMessage.setText(Html.fromHtml("No <font color='#FFFFFF'><b>Open Chats</b></font> → Tap <font color='#FFFFFF'><b>+</b></font> to start a conversation.", Html.FROM_HTML_MODE_LEGACY));

        if(chatSessions.isEmpty()){
            bind.nullMessage.setVisibility(VISIBLE);
            bind.recyclerView.setVisibility(GONE);
        }

        // MUDANÇA AQUI: Atribuir ao campo adapter antes de setar no RecyclerView
        adapter = new RV_Feed_01_Chat_Adapter(chatSessions);
        bind.recyclerView.setAdapter(adapter);
        bind.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bind.recyclerView.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), Activity_03_Chat.class)));

        bind.startChat.setOnClickListener(v ->
                new Dialog_Feed_06_Choose_Contact(requireContext(), contacts).show());

        return bind.getRoot();

    }

    public RV_Feed_01_Chat_Adapter getAdapter() {
        return adapter;
    }
}