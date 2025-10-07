package Nebula.Android.Nebula_View.Activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Choreographer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Nebula.Android.Nebula_View.Fragments.Fragment_Auth_01_Login;
import Nebula.Android.Nebula_View.Fragments.Fragment_Auth_02_Register;
import Nebula.Android.Nebula_View.Utils.NavBar_Inserts;
import Nebula.Android.R;
import Nebula.Android.databinding.Act01AuthBinding;

@SuppressWarnings("SpellCheckingInspection")
public class Activity_01_Auth extends AppCompatActivity implements Choreographer.FrameCallback {

    private Act01AuthBinding binding;
    private final Fragment fragmentAuth01Login = new Fragment_Auth_01_Login();
    private boolean isFragment01Visible = true;

    // FPS
    private long lastFrameTimeNanos = 0;
    private double fps = 0;
    private TextView fpsTextView;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(androidx.appcompat.R.style.Theme_AppCompat);
        super.onCreate(savedInstanceState);

        binding = Act01AuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fpsTextView = binding.fpsTextView;

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        Objects.requireNonNull(getSupportActionBar()).hide();

        NavBar_Inserts.adjustPaddingForNavigationBar(findViewById(R.id.motionLayout), this);

        replaceFragment(fragmentAuth01Login);

        binding.returnButton.setOnClickListener(v -> alternarFragment());
        binding.text3.setOnClickListener(v -> alternarFragment());
        binding.text3.setText(Html.fromHtml("<u><font color='#ffffff'>Sign Up</font></u>"));

        Choreographer.getInstance().postFrameCallback(this);

        simulateBackgroundLoad();
    }

    private void simulateBackgroundLoad() {
        executor.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(15);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void alternarFragment() {
        Fragment proximoFragment;

        if (isFragment01Visible) {
            binding.returnButton.setVisibility(VISIBLE);
            proximoFragment = new Fragment_Auth_02_Register();
            binding.text3.setText(Html.fromHtml("<u><font color='#ffffff'>Sign In</font></u>"));
            isFragment01Visible = false;
        } else {
            binding.returnButton.setVisibility(GONE);
            proximoFragment = new Fragment_Auth_01_Login();
            binding.text3.setText(Html.fromHtml("<u><font color='#ffffff'>Sign Up</font></u>"));
            isFragment01Visible = true;
        }

        replaceFragment(proximoFragment);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isFragment01Visible", isFragment01Visible);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isFragment01Visible = savedInstanceState.getBoolean("isFragment01Visible", true);
        binding.text3.setText(Html.fromHtml(isFragment01Visible ? "<u>SignUp!</u>" : "<u>SignIn!</u>"));
        Fragment fragmentParaMostrar = isFragment01Visible ? new Fragment_Auth_01_Login() : new Fragment_Auth_02_Register();
        replaceFragment(fragmentParaMostrar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Choreographer.getInstance().removeFrameCallback(this);
        executor.shutdownNow();
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.motionLayout, fragment)
                .commit();
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (lastFrameTimeNanos > 0) {
            long dt = frameTimeNanos - lastFrameTimeNanos;
            if (dt > 0) {
                fps = 1_000_000_000.0 / dt;
                fpsTextView.setText(String.format("FPS: %.1f", fps));
            }
        }
        lastFrameTimeNanos = frameTimeNanos;
        Choreographer.getInstance().postFrameCallback(this);
    }
}
