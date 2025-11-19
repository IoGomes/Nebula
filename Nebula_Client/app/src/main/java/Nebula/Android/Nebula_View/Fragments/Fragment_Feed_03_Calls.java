package Nebula.Android.Nebula_View.Fragments;

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

import Nebula.Android.Nebula_Data.Repository.Repo_Calls_History;
import Nebula.Android.Nebula_Data.Repository.Repo_Contact;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_02_Contact_Adapter;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_03_Calls_Adapter;
import Nebula.Android.databinding.Frg05CallHistoryBinding;

/// @author Ítalo Oliveira Gomes
public class Fragment_Feed_03_Calls extends Fragment {

    // ViewBinding do Fragment
    private Frg05CallHistoryBinding bind;

    public RV_Feed_03_Calls_Adapter getAdapter() {
        return adapter;
    }
    private RV_Feed_03_Calls_Adapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        bind = Frg05CallHistoryBinding.inflate(inflater, container, false);

        adapter = new RV_Feed_03_Calls_Adapter(Repo_Calls_History.getCalls());
        Repo_Calls_History.setFeedAdapter(adapter);

        bind.callRecyclerView.setAdapter(adapter);
        bind.callRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Criar e configurar o adapter
        bind.callRecyclerView.setAdapter(adapter);

        String message = "Empty <b><font color='#FFFFFF'>Call History</font></b> → Make or receive a call to view.";
        bind.nullMessage.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));

        return bind.getRoot();
    }
}
