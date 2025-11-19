package Nebula.Android.Nebula_View.Fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import Nebula.Android.Nebula_Model.Entitys.Entity_Pv_Chat;
import Nebula.Android.Nebula_Data.Repository.Repo_Archived_Chats;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_04_Archived_Adapter;
import Nebula.Android.databinding.Frg06ArchivedBinding;


public class Fragment_Feed_04_Archived extends Fragment {

    private Frg06ArchivedBinding bind;
    private RV_Feed_04_Archived_Adapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        bind = Frg06ArchivedBinding.inflate(inflater, container, false);

        List<Entity_Pv_Chat> archivedChats = Repo_Archived_Chats.getArchivedChats();

        String message = "No <b><font color='#FFFFFF'>Archived Chats</font></b>.<br>" +
                "<b><font color='#FFFFFF'>Long-press</font></b> inbox chat → Archive chat to view.";
        bind.nullMessage.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));

        if (archivedChats.isEmpty()) {
            bind.nullMessage.setVisibility(View.VISIBLE);
            bind.recyclerView.setVisibility(View.GONE);
        } else {
            bind.nullMessage.setVisibility(View.GONE);
            bind.recyclerView.setVisibility(View.VISIBLE);
        }

        bind.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new RV_Feed_04_Archived_Adapter(archivedChats);
        bind.recyclerView.setAdapter(adapter);

        Repo_Archived_Chats.setArchivedAdapter(adapter);

        return bind.getRoot();
    }
}
