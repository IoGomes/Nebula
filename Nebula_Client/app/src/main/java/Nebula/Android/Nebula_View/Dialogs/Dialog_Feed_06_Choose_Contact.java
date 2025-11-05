package Nebula.Android.Nebula_View.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Nebula.Android.Nebula_Model.Entitys.Entity_06_Contact;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_02_Contact_Adapter;
import Nebula.Android.R;

public class Dialog_Feed_06_Choose_Contact extends Dialog {

    private RecyclerView recyclerContacts;
    private RV_Feed_02_Contact_Adapter adapter;

    public Dialog_Feed_06_Choose_Contact(@NonNull Context context, List<Entity_06_Contact> contacts) {
        super(context);

        setContentView(R.layout.dlg_04_new_chat);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        recyclerContacts = findViewById(R.id.recyclerView);
        recyclerContacts.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new RV_Feed_02_Contact_Adapter(contacts);

        recyclerContacts.setAdapter(adapter);

    }
}
