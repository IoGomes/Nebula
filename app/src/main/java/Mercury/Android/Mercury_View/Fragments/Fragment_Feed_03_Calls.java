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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import Mercury.Android.Mercury_Model.Entitys.Entity_05_call;
import Mercury.Android.Mercury_View.RV_Adapters.RV_Feed_03_Calls_Adapter;
import Mercury.Android.databinding.Fragment05CallHistoryBinding;

@SuppressWarnings("SpellCheckingInspection")

public class Fragment_Feed_03_Calls extends Fragment {

    Fragment05CallHistoryBinding bind;
    private List<Entity_05_call> callSessions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){

        bind = Fragment05CallHistoryBinding.inflate(inflater, container, false);

        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.SEPTEMBER, 25, 10, 30, 0);
        Date dataFicticia = cal.getTime();

        callSessions = new ArrayList<>();

        callSessions.add(new Entity_05_call(
                "Nome de Contato",
                dataFicticia,
                true
        ));
        callSessions.add(new Entity_05_call(
                "Nome de Contato",
                dataFicticia,
                true
        ));callSessions.add(new Entity_05_call(
                "Nome de Contato",
                dataFicticia,
                true
        ));callSessions.add(new Entity_05_call(
                "Nome de Contato",
                dataFicticia,
                true
        ));callSessions.add(new Entity_05_call(
                "Nome de Contato",
                dataFicticia,
                true
        ));

        bind.callRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bind.callRecyclerView.setAdapter(new RV_Feed_03_Calls_Adapter(callSessions));

        return bind.getRoot();
    }
}
