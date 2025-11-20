package Nebula.Android.Nebula_View.Fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_02_Contact_Adapter.Adapter_Mode.MODE_A;

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
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_Add_Contact;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_02_Contact_Adapter;
import Nebula.Android.databinding.Frg04ContactListBinding;

/// @author Ítalo Oliveira Gomes
public class Fragment_Feed_02_Contacts extends Fragment {

    private Frg04ContactListBinding bind;

    public RV_Feed_02_Contact_Adapter getAdapter() {
        return adapter;
    }
    private RV_Feed_02_Contact_Adapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        bind = Frg04ContactListBinding.inflate(inflater, container, false);

        adapter = new RV_Feed_02_Contact_Adapter(Repo_Contact.getContacts(), MODE_A);
        Repo_Contact.setFeedAdapter(adapter);

        bind.recyclerViewContact.setAdapter(adapter);
        bind.recyclerViewContact.setLayoutManager(new LinearLayoutManager(getContext()));

        String nullMessage = "Empty <font color='#FFFFFF'><b>Contact list</b></font> → Tap <font " +
                "color='#FFFFFF'><b>+</b></font> to add contacts.";

        bind.nullMessage.setText(Html.fromHtml(nullMessage, Html.FROM_HTML_MODE_LEGACY));

        updateUI(Repo_Contact.getContacts().isEmpty());

        bind.startChat.setOnClickListener(v -> {

            new Dialog_Feed_Add_Contact(requireContext(), Repo_Contact.getContacts()).show();
            adapter.notifyDataSetChanged();

        });

        return bind.getRoot();
    }

    private void updateUI(boolean isEmpty) {
        if (isEmpty) {
            bind.nullMessage.setVisibility(VISIBLE);
            bind.recyclerViewContact.setVisibility(GONE);
        } else {
            bind.nullMessage.setVisibility(GONE);
            bind.recyclerViewContact.setVisibility(VISIBLE);
        }
    }
}
