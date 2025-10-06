package Mercury.Android.Mercury_View.Fragments;

import android.content.Intent;
import android.os.Bundle;
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

import Mercury.Android.Mercury_Model.Entitys.Entity_02_Chat_Session;
import Mercury.Android.Mercury_View.Activities.Activity_03_Chat;
import Mercury.Android.Mercury_View.Dialogs.Dialog_Feed_Choose_Contact;
import Mercury.Android.Mercury_View.RecyclerView.RV_Feed_01_Chat_Adapter;
import Mercury.Android.databinding.Fragment01FeedInboxBinding;

public class Fragment_Feed_01_Inbox extends Fragment {

    Fragment01FeedInboxBinding bind;
    private List<Entity_02_Chat_Session> chatSessions;
    private RV_Feed_01_Chat_Adapter adapter;

    @Nullable
    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        bind = Fragment01FeedInboxBinding.inflate(inflater, container, false);

        chatSessions = new ArrayList<>();

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

        bind.recyclerView.setAdapter(new RV_Feed_01_Chat_Adapter(chatSessions));
        bind.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bind.recyclerView.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), Activity_03_Chat.class)));

        bind.startChat.setOnClickListener(v ->
                new Dialog_Feed_Choose_Contact(requireContext()).show());

        return bind.getRoot();

    }
}


