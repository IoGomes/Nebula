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

import Nebula.Android.Nebula_Data.Repository.Repo_Contact;
import Nebula.Android.Nebula_Data.Repository.Repo_Chat;
import Nebula.Android.Nebula_View.Activities.Activity_03_Chat;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_Choose_Contact;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_01_Chat_Adapter;
import Nebula.Android.databinding.Frg03FeedBinding;

/// @author Ítalo Oliveira Gomes
public class Fragment_Feed_01_Inbox extends Fragment {

    // ViewBinding do Fragment
    private Frg03FeedBinding bind;

    // Obtem o Adapter para utilizar no Fragment
    public RV_Feed_01_Chat_Adapter getAdapter() {
        return adapter;
    }
    private RV_Feed_01_Chat_Adapter adapter;


    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        bind = Frg03FeedBinding.inflate(inflater, container, false);

        Repo_Chat.setOnChatsChangedListener(this::updateEmptyState);
        updateEmptyState();

        adapter = new RV_Feed_01_Chat_Adapter(Repo_Chat.getChats());
        Repo_Chat.setFeedAdapter(adapter);

        bind.nullMessage.setText(Html.fromHtml("No <font color='#FFFFFF'><b>Open Chats</b></font> → Tap <font color='#FFFFFF'><b>+</b></font> to start a conversation.", Html.FROM_HTML_MODE_LEGACY));

        bind.recyclerView.setAdapter(adapter);
        bind.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bind.recyclerView.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), Activity_03_Chat.class)));

        bind.startChat.setOnClickListener(v ->
                new Dialog_Feed_Choose_Contact(requireContext(), Repo_Contact.getContacts()).show());

        return bind.getRoot();
    }

    public void updateEmptyState() {
        if (Repo_Chat.getChats().isEmpty()) {
            bind.recyclerView.setVisibility(GONE);
            bind.nullMessage.setVisibility(VISIBLE);
            bind.stars.playAnimation();
            bind.stars.setVisibility(VISIBLE);
        } else {
            bind.nullMessage.setVisibility(GONE);
            bind.recyclerView.setVisibility(VISIBLE);
            bind.stars.cancelAnimation();
            bind.stars.setVisibility(GONE);
        }
    }
}