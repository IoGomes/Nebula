package Nebula.Android.Nebula_View.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import Nebula.Android.Nebula_View.Utils.NavBar_Inserts;
import Nebula.Android.R;
import Nebula.Android.databinding.Act05VideoCallBinding;

public class Activity_05_Video_Call extends AppCompatActivity {
        private Act05VideoCallBinding bind;
        private PreviewView frontCameraPreview;
        private ProcessCameraProvider cameraProvider;

        @Override
        protected void onCreate(Bundle savedInstanceState) {

            setTheme(androidx.appcompat.R.style.Theme_AppCompat);
            super.onCreate(savedInstanceState);

            bind = Act05VideoCallBinding.inflate(getLayoutInflater());

            setContentView(bind.getRoot());

            frontCameraPreview = (PreviewView) findViewById(R.id.previewView);

            bind.returnButton.setOnClickListener(v -> finish());

            bind.dismissCall.setOnClickListener(v-> finish());

            Window window = getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);

            View rootLayout = findViewById(R.id.extern_image);
            NavBar_Inserts.adjustPaddingForNavigationBar(rootLayout, this);

            Objects.requireNonNull(getSupportActionBar()).hide();

            startCamera();
        }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(frontCameraPreview.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview);

            } catch (ExecutionException | InterruptedException e) {

            }
        }, ContextCompat.getMainExecutor(this));
    }
    }
