package Nebula.Android.Nebula_View.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Activity_LandingPage extends AppCompatActivity {

    // SEU DOMÍNIO NGROK
    private static final String BASE_DOMAIN = "https://youlanda-undependable-compressingly.ngrok-free.dev";
    private static final String VIDEO_ROUTE = "/video-call";

    // TAG para filtrar no Logcat
    private static final String TAG = "NebulaDebug";

    private WebView webView;
    private static final int PERMISSION_REQUEST_CODE = 999;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(androidx.appcompat.R.style.Theme_AppCompat);
        if (getWindow() != null) getWindow().setStatusBarColor(Color.BLACK);

        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setOrientation(LinearLayout.VERTICAL);

        webView = new WebView(this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setupWebView();

        layout.addView(webView);
        setContentView(layout);

        if (checkPermissions()) {
            loadPage();
        } else {
            requestPermissions();
        }
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Limpeza de Cache
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.clearCache(true);
        webView.clearHistory();
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();

        // 1. CLIENT DE NAVEGAÇÃO (Monitora URLs)
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // AQUI: Mostra qual URL o Android COMEÇOU a carregar
                Log.d(TAG, ">>> 🟢 INICIANDO CARREGAMENTO: " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // AQUI: Mostra em qual URL ele TERMINOU (se houve redirecionamento, vai aparecer aqui)
                Log.d(TAG, ">>> 🏁 PÁGINA FINAL CARREGADA: " + url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e(TAG, ">>> 🔴 ERRO DE CARREGAMENTO: " + error.getDescription());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });

        // 2. CLIENT DO CHROME (Câmera + Console do JS)
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Log.d(TAG, ">>> 📷 Solicitando Permissão Web: " + request.getResources().toString());
                        request.grant(request.getResources());
                    }
                });
            }

            // --- O SEGREDO: TRAZ O CONSOLE.LOG DO JAVASCRIPT PARA O ANDROID ---
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(TAG + "-JS", consoleMessage.message() + " -- Linha "
                        + consoleMessage.lineNumber() + " de "
                        + consoleMessage.sourceId());
                return true;
            }
        });
    }

    private void loadPage() {
        String myUserId = "13";
        String myUserName = "AndroidMobile";
        String targetUserId = "15";

        String fullUrl = BASE_DOMAIN + VIDEO_ROUTE +
                "?userId=" + myUserId +
                "&userName=" + myUserName +
                "&targetId=" + targetUserId +
                "&t=" + System.currentTimeMillis();

        Toast.makeText(this, "Conectando...", Toast.LENGTH_SHORT).show();

        // Log da URL que estamos PEDINDO para carregar
        Log.d(TAG, ">>> 🚀 SOLICITANDO URL: " + fullUrl);

        webView.loadUrl(fullUrl);
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadPage();
        }
    }
}