package Nebula.Android.Nebula_View.Dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Nebula.Android.Nebula_Model.Entitys.Entity_Contact;
import Nebula.Android.Nebula_ViewModel.Controllers.Controller_Contact;
import Nebula.Android.databinding.Dlg09AddContactBinding;

public class Dialog_Feed_Add_Contact extends Dialog {

    private static final String TAG = "Dialog_Feed_Add_Contact";

    private Dlg09AddContactBinding bind;
    private Activity activity;
    private List<Entity_Contact> contactsList;
    private PreviewView backCameraPreview;
    private ProcessCameraProvider cameraProvider;
    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;
    private boolean isScanning = true;
    private Camera camera;

    public Dialog_Feed_Add_Contact(@NonNull Context context) {
        super(context);

        this.activity = getActivityFromContext(context);
        this.contactsList = contactsList;

        bind = Dlg09AddContactBinding.inflate(LayoutInflater.from(context));
        setContentView(bind.getRoot());

        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        int widthDp = 300;
        int heightDp = 370;

        float density = context.getResources().getDisplayMetrics().density;
        int widthPx = (int) (widthDp * density);
        int heightPx = (int) (heightDp * density);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = widthPx;
        params.height = heightPx;
        getWindow().setAttributes(params);

        backCameraPreview = bind.previewView;

        backCameraPreview.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);

        cameraExecutor = Executors.newSingleThreadExecutor();

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();

        barcodeScanner = BarcodeScanning.getClient(options);

        bind.selfContainer.setClipToOutline(true);
        bind.selfContainer.setClipChildren(true);

        bind.previewView.setClipToOutline(true);
        bind.previewView.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(android.view.View view, android.graphics.Outline outline) {
                float radius = 6 * getContext().getResources().getDisplayMetrics().density;
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });

        init();
        startCamera();

        setOnDismissListener(dialog -> releaseCamera());
    }

    private void init() {
    }

    private void startCamera() {
        if (activity == null) {
            Log.e(TAG, "Activity é null, não é possível iniciar a câmera");
            return;
        }

        if (!(activity instanceof LifecycleOwner)) {
            Log.e(TAG, "Activity não implementa LifecycleOwner");
            return;
        }

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(getContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(backCameraPreview.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    if (!isScanning) {
                        imageProxy.close();
                        return;
                    }
                    processImageProxy(imageProxy);
                });

                cameraProvider.unbindAll();

                camera = cameraProvider.bindToLifecycle(
                        (LifecycleOwner) activity,
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

                setZoom(2.0f);

                Log.d(TAG, "Câmera e scanner iniciados com sucesso");

            } catch (Exception e) {
                Log.e(TAG, "Erro ao iniciar câmera: " + e.getMessage(), e);
            }
        }, ContextCompat.getMainExecutor(getContext()));
    }

    private void setZoom(float zoomLevel) {
        if (camera != null) {
            try {
                float maxZoom = camera.getCameraInfo().getZoomState().getValue().getMaxZoomRatio();
                float minZoom = camera.getCameraInfo().getZoomState().getValue().getMinZoomRatio();
                float zoom = Math.max(minZoom, Math.min(zoomLevel, maxZoom));
                camera.getCameraControl().setZoomRatio(zoom);
                Log.d(TAG, "Zoom aplicado: " + zoom + " (min: " + minZoom + ", max: " + maxZoom + ")");
            } catch (Exception e) {
                Log.e(TAG, "Erro ao aplicar zoom: " + e.getMessage());
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImageProxy(ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();

        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.getImageInfo().getRotationDegrees()
        );

        barcodeScanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        if (barcode.getRawValue() != null) {

                            String raw = barcode.getRawValue();

                            if (raw == null || !raw.matches("\\d+")) {
                                return;
                            }

                            int qrCodeValue = Integer.parseInt(barcode.getRawValue());

                            activity.runOnUiThread(() -> {
                                try {
                                    onQrCodeDetected(qrCodeValue);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });

                            isScanning = false;
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao processar imagem: " + e.getMessage());
                })
                .addOnCompleteListener(task -> {
                    imageProxy.close();
                });
    }


    private void onQrCodeDetected(int qrCodeValue) {
        new Controller_Contact().onQrCodeRead(qrCodeValue);
    }

    private void releaseCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
        }

        if (barcodeScanner != null) {
            barcodeScanner.close();
            barcodeScanner = null;
        }

        if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
            cameraExecutor.shutdown();
            cameraExecutor = null;
        }

        camera = null;
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

    @Override
    public void dismiss() {
        isScanning = false;
        releaseCamera();
        super.dismiss();
    }
}