package Nebula.Android.Nebula_View.Activities;

import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import Nebula.Android.Nebula_View.Utils.NavBar_Inserts;
import Nebula.Android.R;
import Nebula.Android.databinding.Act04VoiceCallBinding;

public class Activity_04_Voice_Call extends AppCompatActivity {

    private Act04VoiceCallBinding bind;
    private Vibrator vibrator;

    private ToneGenerator toneGenerator;
    private Handler toneHandler;
    private Runnable toneRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(androidx.appcompat.R.style.Theme_AppCompat);
        super.onCreate(savedInstanceState);

        View rootLayout = findViewById(R.id.root);
        NavBar_Inserts.adjustPaddingForNavigationBar(rootLayout, this);

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        Objects.requireNonNull(getSupportActionBar()).hide();

        bind = Act04VoiceCallBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        bind.cronometro.start();

        // Inicializa vibrator e som
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        startCallTone();

        bind.returnButton.setOnClickListener(v -> {
            bind.cronometro.stop();
            stopPulseVibration();
            stopCallTone();
            finish();
        });
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

    private void stopPulseVibration() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private void startCallTone() {
        if (toneGenerator != null) {
            toneGenerator.release();
        }

        // Tom mais grave e agradável
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 60); // 60% volume

        final int pulseDuration = 1400; // duração do "pulso" (soma de todos os pulsos da animação)
        final long loopDuration = 3000; // duração total do loop da animação

        toneHandler = new Handler();
        toneRunnable = new Runnable() {
            @Override
            public void run() {
                toneGenerator.startTone(ToneGenerator.TONE_SUP_RINGTONE, pulseDuration);

                // Reagendar o próximo pulso após o loop da animação
                toneHandler.postDelayed(this, loopDuration);
            }
        };

        toneHandler.post(toneRunnable);
    }

    private void stopCallTone() {
        if (toneHandler != null && toneRunnable != null) {
            toneHandler.removeCallbacks(toneRunnable);
        }
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPulseVibration();
        stopCallTone();
    }
}
