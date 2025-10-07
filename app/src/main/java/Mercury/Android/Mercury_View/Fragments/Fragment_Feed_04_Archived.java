package Mercury.Android.Mercury_View.Fragments;

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
import Mercury.Android.Mercury_View.RV_Adapters.RV_Feed_01_Chat_Adapter;
import Mercury.Android.databinding.Fragment06ArchivedBinding;

public class Fragment_Feed_04_Archived extends Fragment {

    Fragment06ArchivedBinding bind;
    private List<Entity_02_Chat_Session> chatSessions;

    @Nullable
    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        bind = Fragment06ArchivedBinding.inflate(inflater, container, false);

        chatSessions = new ArrayList<>();

        chatSessions.add(new Entity_02_Chat_Session(
                "chat2",
                Arrays.asList("userC", "userD"),
                List.of(new Date()),
                "Reunião marcada para amanhã."
        ));

        chatSessions.add(new Entity_02_Chat_Session(
                "chat2",
                Arrays.asList("userC", "userD"),
                List.of(new Date()),
                "Reunião marcada para amanhã."
        ));

        chatSessions.add(new Entity_02_Chat_Session(
                "chat2",
                Arrays.asList("userC", "userD"),
                List.of(new Date()),
                "Reunião marcada para amanhã."
        ));

        chatSessions.add(new Entity_02_Chat_Session(
                "chat2",
                Arrays.asList("userC", "userD"),
                List.of(new Date()),
                "Reunião marcada para amanhã."
        ));

        bind.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bind.recyclerView.setAdapter(new RV_Feed_01_Chat_Adapter(chatSessions));

        return bind.getRoot();
    }
}