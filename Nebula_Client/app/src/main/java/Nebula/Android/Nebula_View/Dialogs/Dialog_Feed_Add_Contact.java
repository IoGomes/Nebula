package Nebula.Android.Nebula_View.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Objects;

import Nebula.Android.Nebula_Model.Entitys.Entity_Contact;
import Nebula.Android.databinding.Dlg09AddContactBinding;

public class Dialog_Feed_Add_Contact extends Dialog {

    private Dlg09AddContactBinding bind;
    private Activity activity;
    private List<Entity_Contact> contactsList;

    public Dialog_Feed_Add_Contact(@NonNull Context context, List<Entity_Contact> contactsList) {
        super(context);

        this.activity = getActivityFromContext(context);
        this.contactsList = contactsList;

        bind = Dlg09AddContactBinding.inflate(LayoutInflater.from(context));
        setContentView(bind.getRoot());

        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

        init();
    }

    private void init() {
        bind.btnSave.setOnClickListener(v -> {
            String name = bind.editText.getText().toString().trim();
            String phoneNumber = bind.phoneNumber.getText().toString().trim();

            if (!name.isEmpty() && !phoneNumber.isEmpty()) {
                Entity_Contact newContact = new Entity_Contact(name, phoneNumber);
                contactsList.add(newContact);
                dismiss();
            }
        });
    }

    private Activity getActivityFromContext(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
