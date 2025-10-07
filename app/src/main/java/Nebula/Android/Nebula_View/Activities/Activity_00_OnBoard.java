package Nebula.Android.Nebula_View.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Objects;

import Nebula.Android.Nebula_View.Utils.NavBar_Inserts;
import Nebula.Android.R;
import Nebula.Android.databinding.Act00OnboardBinding;

public class Activity_00_OnBoard extends AppCompatActivity {


    private Act00OnboardBinding binding;
    private int currentIndex = 0;

    private final int[] images = {
            R.drawable.img_onboard_01,
            R.drawable.img_onboard_03,
            R.drawable.img_onboard_02
    };

    private final String[] titles = {
            "Welcome to Nebula!",
            "Stay Connected",
            "Permission Request"
    };

    private final String[] descriptions = {
            "Chat in real time with friends, family, and colleagues.",
            "Send texts, voice notes, and files in seconds. Stay connected without hassle.",
            "Grant the required permissions to ensure the app functions properly. Tap <b>Permissions</b>."
    };

    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS
    };

    private Animation inForwardAnim;
    private Animation outForwardAnim;
    private Animation inBackwardAnim;
    private Animation outBackwardAnim;
    private View step1, step2, step3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(androidx.appcompat.R.style.Theme_AppCompat);

        binding = Act00OnboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View rootLayout = findViewById(R.id.root);
        NavBar_Inserts.adjustPaddingForNavigationBar(rootLayout, this);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        Objects.requireNonNull(getSupportActionBar()).hide();

        setupStepIndicators();
        setupAnimations();
        setupImageSwitcher();
        setupButtons();
        updateButtonStates();
        updateStepIndicators();
    }

    private void setupStepIndicators() {
        step1 = binding.step1;
        step2 = binding.step2;
        step3 = binding.step3;
    }

    private void setupAnimations() {
        // Avançar
        inForwardAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        outForwardAnim = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);

        // Voltar
        inBackwardAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        outBackwardAnim = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
    }

    private void setupImageSwitcher() {
        binding.imageSwitcher.setFactory(() -> {
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            return imageView;
        });

        binding.imageSwitcher.setInAnimation(inForwardAnim);
        binding.imageSwitcher.setOutAnimation(outForwardAnim);

        binding.imageSwitcher.setImageResource(images[currentIndex]);
    }

    private void setupButtons() {
        binding.previousButton.setOnClickListener(v -> previousImage());
        binding.nextButton.setOnClickListener(v -> nextImage());
    }

    private void nextImage() {
        if (currentIndex < images.length - 1) {
            currentIndex++;
            binding.imageSwitcher.setInAnimation(inForwardAnim);
            binding.imageSwitcher.setOutAnimation(outForwardAnim);
            binding.imageSwitcher.setImageResource(images[currentIndex]);
            updateButtonStates();
            updateStepIndicators();
        } else {
            onReachedEnd();
        }
    }

    private void previousImage() {
        if (currentIndex > 0) {
            currentIndex--;
            binding.imageSwitcher.setInAnimation(inBackwardAnim);
            binding.imageSwitcher.setOutAnimation(outBackwardAnim);
            binding.imageSwitcher.setImageResource(images[currentIndex]);
            updateButtonStates();
            updateStepIndicators();
        }
    }

    private void updateStepIndicators() {

        step1.setBackgroundResource(R.drawable.bg_step_inactive);
        step2.setBackgroundResource(R.drawable.bg_step_inactive);
        step3.setBackgroundResource(R.drawable.bg_step_inactive);

        switch (currentIndex) {
            case 0:
                step1.setBackgroundResource(R.drawable.bg_current_category);
                break;
            case 1:
                step2.setBackgroundResource(R.drawable.bg_current_category);
                break;
            case 2:
                step3.setBackgroundResource(R.drawable.bg_current_category);
                break;
        }
    }

    private void updateButtonStates() {
        binding.previousButton.setEnabled(currentIndex > 0);
        binding.nextButton.setEnabled(true);

        // Atualiza título e descrição dinamicamente
        binding.title.setText(titles[currentIndex]);
        binding.text.setText(Html.fromHtml((descriptions[currentIndex])));


        if (currentIndex == images.length - 1) {
            binding.nextButton.setText("Permissions");

        } else {
            binding.nextButton.setText("Next");
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void onReachedEnd() {
        if (allPermissionsGranted()) {
            startAuthActivity();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void startAuthActivity() {
        Intent intent = new Intent(Activity_00_OnBoard.this, Activity_01_Auth.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startAuthActivity();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (currentIndex > 0) {
            previousImage(); // animação inversa
        } else {
            super.onBackPressed(); // sai normalmente
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}