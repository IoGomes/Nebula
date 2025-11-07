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
import Nebula.Android.Nebula_Model.Repository.Chat_Repository;
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

        contacts = new ArrayList<>();

        if (Chat_Repository.getChats().isEmpty()) {
            List<String> users2 = Arrays.asList("userC", "userD");
            List<Date> date = Arrays.asList(new Date());

            Chat_Repository.getChats().add(new Entity_02_Chat_Session(
                    "chat",
                    users2,
                    date,
                    "Versão 1.3 -> 06/11/2025",
                    "TDE 2025.2"
            ));

            Chat_Repository.getChats().add(new Entity_02_Chat_Session(
                    "chat2",
                    users2,
                    date,
                    "\uD83D\uDC4B Olá! Bem-vindo(a) ao chat do Nebula",
                    "Nebula Dev Team"
            ));
        }

        bind.nullMessage.setText(Html.fromHtml("No <font color='#FFFFFF'><b>Open Chats</b></font> → Tap <font color='#FFFFFF'><b>+</b></font> to start a conversation.", Html.FROM_HTML_MODE_LEGACY));

        if (Chat_Repository.getChats() == null || Chat_Repository.getChats().isEmpty()) {
            bind.nullMessage.setVisibility(View.VISIBLE);
            bind.recyclerView.setVisibility(View.GONE);
        } else {
            bind.nullMessage.setVisibility(View.GONE);
            bind.recyclerView.setVisibility(View.VISIBLE);
        }

        adapter = new RV_Feed_01_Chat_Adapter(Chat_Repository.getChats());
        Chat_Repository.setFeedAdapter(adapter);
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