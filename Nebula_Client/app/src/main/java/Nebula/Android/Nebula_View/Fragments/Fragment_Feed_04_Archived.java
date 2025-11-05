package Nebula.Android.Nebula_View.Fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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
import java.util.List;

import Nebula.Android.Nebula_Model.Entitys.Entity_02_Chat_Session;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_01_Chat_Adapter;
import Nebula.Android.databinding.Frg06ArchivedBinding;

public class Fragment_Feed_04_Archived extends Fragment {

    Frg06ArchivedBinding bind;
    private List<Entity_02_Chat_Session> chatSessions;

    @Nullable
    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        bind = Frg06ArchivedBinding.inflate(inflater, container, false);

        chatSessions = new ArrayList<>();

        String message = "No <b><font color='#FFFFFF'>Archived Chats</font></b>. <b><font color='#FFFFFF'>Long-press</font></b> inbox chat → Archive chat to view.";
        bind.nullMessage.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));

        if (chatSessions.isEmpty()) {
            bind.recyclerView.setVisibility(GONE);
            bind.nullMessage.setVisibility(VISIBLE);
        }

        bind.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bind.recyclerView.setAdapter(new RV_Feed_01_Chat_Adapter(chatSessions));

        return bind.getRoot();
    }
}