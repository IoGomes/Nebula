package Nebula.Android.Nebula_View.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.util.Objects;

import Nebula.Android.Nebula_Data.Preferences.SessionPreferences;
import Nebula.Android.databinding.DlgQrCodeBinding;

public class Dialog_Feed_QrCode extends Dialog {

    private DlgQrCodeBinding bind;

    private Activity activity;

    public Dialog_Feed_QrCode(@NonNull Context context) {
        super(context);
        this.activity = getActivityFromContext(context);

        bind = DlgQrCodeBinding.inflate(LayoutInflater.from(context));
        setContentView(bind.getRoot());

        SessionPreferences session = new SessionPreferences(context);
        String id = session.getKeyId();

        Bitmap qr = session.generateQrCode(context, id);

        bind.qrCode.setImageBitmap(qr);

        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams params = getWindow().getAttributes();

        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

        getWindow().setAttributes(params);

        init();
    }

    private void init() {
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
