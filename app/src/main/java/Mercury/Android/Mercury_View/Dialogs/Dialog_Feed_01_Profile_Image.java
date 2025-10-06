package Mercury.Android.Mercury_View.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.util.Objects;

import Mercury.Android.Mercury_ViewModel.Controllers.Controller_Video_Call;
import Mercury.Android.Mercury_ViewModel.Controllers.Controller_Voice_Call;
import Mercury.Android.databinding.Dialog05ProfileImageBinding;

public class Dialog_Feed_01_Profile_Image extends Dialog {

    Dialog05ProfileImageBinding bind;
    private Activity activity;

    public Dialog_Feed_01_Profile_Image(@NonNull Context context) {
        super(context);

        this.activity = getActivityFromContext(context);

        bind = Dialog05ProfileImageBinding.inflate(LayoutInflater.from(context));
        setContentView(bind.getRoot());

        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

        bind.videoCall.setOnClickListener(v -> {
            new Controller_Video_Call(activity).performVideoCall(activity);
            dismiss();
        });

        bind.voiceCall.setOnClickListener(v -> {
            new Controller_Voice_Call(activity).performVoiceCall(activity);
            dismiss();
        });

        bind.textMessage.setOnClickListener(v -> {
            new Controller_Voice_Call(activity).performVoiceCall(activity);
            dismiss();
        });

        init(context);

    }

    private void init(Context context) {

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