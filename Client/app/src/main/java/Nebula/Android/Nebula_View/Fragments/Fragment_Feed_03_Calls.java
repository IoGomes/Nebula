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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import Nebula.Android.Nebula_Model.Entitys.Entity_05_call;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_03_Calls_Adapter;
import Nebula.Android.databinding.Frg05CallHistoryBinding;

@SuppressWarnings("SpellCheckingInspection")

public class Fragment_Feed_03_Calls extends Fragment {

    Frg05CallHistoryBinding bind;
    private List<Entity_05_call> callSessions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){

        bind = Frg05CallHistoryBinding.inflate(inflater, container, false);

        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.SEPTEMBER, 25, 10, 30, 0);
        Date dataFicticia = cal.getTime();

        callSessions = new ArrayList<>();

        if(callSessions.isEmpty()){

            bind.callRecyclerView.setVisibility(GONE);
            bind.nullMessage.setVisibility(VISIBLE);

        }

        String message = "Empty <b><font color='#FFFFFF'>Call History</font></b> → Make or receive a call to view.";
        bind.nullMessage.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));

        bind.callRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bind.callRecyclerView.setAdapter(new RV_Feed_03_Calls_Adapter(callSessions));

        return bind.getRoot();
    }
}
