package Nebula.Android.Nebula_View.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import Nebula.Android.R;

public class Dialog_Feed_Choose_Contact extends Dialog {
    public Dialog_Feed_Choose_Contact(@NonNull Context context) {
        super(context);

        setContentView(R.layout.dlg_04_new_chat);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    protected Dialog_Feed_Choose_Contact(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public Dialog_Feed_Choose_Contact(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }
}
