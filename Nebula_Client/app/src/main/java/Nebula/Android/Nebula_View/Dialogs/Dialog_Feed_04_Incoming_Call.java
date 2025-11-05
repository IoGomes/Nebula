package Nebula.Android.Nebula_View.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;

import java.util.Objects;

import Nebula.Android.Nebula_ViewModel.Controllers.Controller_Voice_Call;
import Nebula.Android.R;
import Nebula.Android.databinding.Dlg08CallReceivedBinding;

public class Dialog_Feed_04_Incoming_Call extends Dialog {

    private Activity activity;
    Dlg08CallReceivedBinding bind;

    public Dialog_Feed_04_Incoming_Call(@NonNull Context context) {
        super(context);

        this.activity = getActivityFromContext(context);

        bind = Dlg08CallReceivedBinding.inflate(LayoutInflater.from(context));
        setContentView(bind.getRoot());

        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams params = getWindow().getAttributes();

        params.width = context.getResources().getDisplayMetrics().widthPixels - dpToPx(context, 20);
        params.height = dpToPx(context, 80);
        params.gravity = Gravity.TOP;

        bind.imageButton2.setOnClickListener(v -> {
            new Controller_Voice_Call(activity).performVoiceCall(activity);
            dismiss();
        });

        bind.dismissCall.setOnClickListener(v -> dismiss());

        getWindow().setAttributes(params);

        init(context);
    }

    private Vibrator vibrator;

    @Override
    public void show() {
        super.show();

        startPulseVibration();

        MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.mp3_msg_ring);
        mp.start();

        new android.os.Handler().postDelayed(() -> {
            dismiss();
        }, 1500);

        Animation slideDown = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
        bind.getRoot().startAnimation(slideDown);
    }

    @Override
    public void dismiss() {
        stopVibration();
        super.dismiss();
    }

    private void startPulseVibration() {
        if (vibrator == null) return;

        final long pulseDuration = 200;
        final long delayBetweenPulses = 600;
        final long endLoopDelay = 1400;

        long[] pattern = new long[] {
                0,
                pulseDuration,
                delayBetweenPulses,
                pulseDuration,
                delayBetweenPulses,
                pulseDuration,
                endLoopDelay
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect effect = VibrationEffect.createWaveform(pattern, 0);
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(pattern, 0);
        }
    }

    private void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private void init(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
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

    private int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}