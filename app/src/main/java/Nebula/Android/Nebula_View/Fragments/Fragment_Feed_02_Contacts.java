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

import Nebula.Android.Nebula_Model.Entitys.Entity_06_Contact;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_05_Add_Contact;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_02_Contact_Adapter;
import Nebula.Android.databinding.Frg04ContactListBinding;

public class Fragment_Feed_02_Contacts extends Fragment {

    private Frg04ContactListBinding bind;
    private RV_Feed_02_Contact_Adapter adapter;
    private List<Entity_06_Contact> contactsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        bind = Frg04ContactListBinding.inflate(inflater, container, false);

        contactsList = new ArrayList<>();

        bind.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RV_Feed_02_Contact_Adapter(contactsList);
        bind.recyclerView.setAdapter(adapter);

        bind.nullMessage.setText(Html.fromHtml(
                "Empty <font color='#FFFFFF'><b>Contact list</b></font> → Tap <font color='#FFFFFF'><b>+</b></font> to add contacts.", Html.FROM_HTML_MODE_LEGACY));

        updateUI(contactsList.isEmpty());

        bind.startChat.setOnClickListener(v -> {
            Dialog_Feed_05_Add_Contact dialog = new Dialog_Feed_05_Add_Contact(requireContext(), contactsList);
            dialog.setOnDismissListener(d -> {
                adapter.notifyDataSetChanged();
                updateUI(contactsList.isEmpty());
            });
            dialog.show();
        });

        return bind.getRoot();
    }

    private void updateUI(boolean isEmpty) {
        if (isEmpty) {
            bind.nullMessage.setVisibility(VISIBLE);
            bind.recyclerView.setVisibility(GONE);
        } else {
            bind.nullMessage.setVisibility(GONE);
            bind.recyclerView.setVisibility(VISIBLE);
        }
    }
}
