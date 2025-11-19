package Nebula.Android.Nebula_View.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import Nebula.Android.R;

public class Dialog_Feed_Long_Hold_Options extends Dialog {
    public Dialog_Feed_Long_Hold_Options(@NonNull Context context) {

        super(context);

        setContentView(R.layout.dlg_06_longhold_options);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(); params.copyFrom(getWindow().getAttributes()); params.width = 200; params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        init(context);
    }
    private void init(Context context){

    }
}
