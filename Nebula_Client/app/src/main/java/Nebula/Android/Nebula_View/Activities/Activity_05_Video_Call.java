package Nebula.Android.Nebula_View.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import Nebula.Android.Nebula_Data.Preferences.SessionPreferences;
import Nebula.Android.databinding.Act05VideoCallBinding;

public class Activity_05_Video_Call extends AppCompatActivity {

    private static final String TAG = "Activity_05_Video_Call";

    private static final String BASE_DOMAIN = "https://nebula.app.br";
    private static final String VIDEO_ROUTE = "/video-call";

    private static final int CAMERA_REQUEST = 1001;
    private static final int PERMISSION_REQUEST_CODE = 2002;

    private ValueCallback<Uri[]> filePathCallback;
    private Uri tempPhotoUri;
    private WebView webView;
    private TextView debugConsole;

    private Act05VideoCallBinding binding;

    public String SENDER_ID;
    public String RECEIVER_ID;
    public String RECEIVER_NAME;
    public String RECEIVER_NUMBER;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(androidx.appcompat.R.style.Theme_AppCompat);

        // ✅ Infla o layout XML
        binding = Act05VideoCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBasicUI();
        intentBuilder();

        Log.e(TAG, "SENDER_USER_ID: " + SENDER_ID);

        // ✅ Debug Console
        debugConsole = new TextView(this);
        debugConsole.setTextColor(Color.GREEN);
        debugConsole.setBackgroundColor(Color.argb(220, 0, 0, 0));
        debugConsole.setPadding(20, 20, 20, 20);
        debugConsole.setMaxLines(6);
        debugConsole.setMovementMethod(new ScrollingMovementMethod());
        debugConsole.setText("--- DEBUG INICIADO ---\n");

        // Adiciona console no container do XML
        binding.debugContainer.addView(debugConsole);

        // ✅ WebView
        webView = new WebView(this);
        webView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        setupWebViewSettings();

        // Adiciona WebView no container do XML
        binding.webviewContainer.addView(webView);
        binding.contactName.setText(RECEIVER_NAME);
        binding.contactNumber.setText(RECEIVER_NUMBER);

        // ✅ Configura botões do XML
        setupButtons();

        if (checkPermissions()) {
            loadUrlWithParams();
        } else {
            logToScreen("Solicitando permissões...");
            requestPermissions();
        }
    }

    private void setupButtons() {

        binding.btnMute.setOnClickListener(v -> {
            webView.evaluateJavascript("toggleMute()", null);
            logToScreen("🔇 Mute toggle");
        });

        binding.btnCamera.setOnClickListener(v -> {
            webView.evaluateJavascript("toggleCamera()", null);
            logToScreen("📷 Camera toggle");
        });

        binding.dismissCall.setOnClickListener(v -> {
            webView.evaluateJavascript("endCall()", null);
            logToScreen("📞 Encerrando chamada");
            finish();
        });

        binding.returnButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void logToScreen(String msg) {
        Log.d(TAG, msg);
        runOnUiThread(() -> {
            if (debugConsole != null) debugConsole.append(msg + "\n");
        });
    }

    private void setupWebViewSettings() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.clearCache(true);
        webView.clearHistory();
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();

        webView.setFitsSystemWindows(true);
        webView.addJavascriptInterface(new WebBridge(), "Android");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                logToScreen("🟡 Iniciando: " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                logToScreen("🟢 Terminou em: " + url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (!request.getUrl().toString().endsWith("favicon.ico")) {
                    logToScreen("🔴 Erro: " + error.getDescription());
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        logToScreen("📷 Permissão Web: " + request.getResources().toString());
                        request.grant(request.getResources());
                    }
                });
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                logToScreen("[JS] " + consoleMessage.message());
                return true;
            }

            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {

                Activity_05_Video_Call.this.filePathCallback = filePathCallback;

                File photoFile;
                try {
                    photoFile = File.createTempFile("IMG_", ".jpg", getExternalCacheDir());
                } catch (IOException e) {
                    logToScreen("Erro ao criar arquivo temporário");
                    return false;
                }

                tempPhotoUri = FileProvider.getUriForFile(
                        Activity_05_Video_Call.this,
                        getPackageName() + ".provider",
                        photoFile
                );

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, tempPhotoUri);

                startActivityForResult(intent, CAMERA_REQUEST);
                return true;
            }
        });
    }

    private void loadUrlWithParams() {
        // Exemplo de URL real
        String realUrl = BASE_DOMAIN+VIDEO_ROUTE;

        logToScreen("🚀 Carregando URL real: " + realUrl);

        // Configurações do WebView (importante)
        webView.getSettings().setJavaScriptEnabled(true); // habilita JS se necessário
        webView.getSettings().setDomStorageEnabled(true); // habilita armazenamento local

        // Mantém a navegação dentro do WebView
        webView.setWebViewClient(new WebViewClient());

        // Carrega a URL
        webView.loadUrl(realUrl);
    }


    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadUrlWithParams();
        } else {
            logToScreen("❌ Permissão Negada!");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && filePathCallback != null) {
            if (resultCode == RESULT_OK && tempPhotoUri != null) {
                filePathCallback.onReceiveValue(new Uri[]{tempPhotoUri});
            } else {
                filePathCallback.onReceiveValue(null);
            }
            filePathCallback = null;
        }
    }

    public class WebBridge {
        @JavascriptInterface
        public void showToast(String msg) {
            Toast.makeText(Activity_05_Video_Call.this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    public void setupBasicUI() {
        setTheme(androidx.appcompat.R.style.Theme_AppCompat);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        Objects.requireNonNull(getSupportActionBar()).hide();
    }

    public void intentBuilder() {
        this.SENDER_ID = new SessionPreferences(this).getKeyId();
        this.RECEIVER_ID = getIntent().getStringExtra("RECEIVER_ID");
        this.RECEIVER_NAME = getIntent().getStringExtra("RECEIVER_NAME");
        this.RECEIVER_NUMBER = getIntent().getStringExtra("RECEIVER_NUMBER");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
        }
    }
}