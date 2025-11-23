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
import android.widget.LinearLayout;
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


public class Activity_05_Video_Call extends AppCompatActivity {

    private static final String TAG = "NEBULA_LOG";

    private static final String BASE_DOMAIN = "https://youlanda-undependable-compressingly.ngrok-free.dev";
    private static final String VIDEO_ROUTE = "/video-call";

    private static final int CAMERA_REQUEST = 1001;
    private static final int PERMISSION_REQUEST_CODE = 2002;

    private ValueCallback<Uri[]> filePathCallback;
    private Uri tempPhotoUri;
    private WebView webView;
    private TextView debugConsole;

    /// Strings contendo todos os dados necessários para a chamada de vídeo solicitada por Ezdraz.
    /// Ao clicar no botão de chamada, todas as variáveis abaixo já estarão preenchidas.
    /// Basta verificar onde cada valor é utilizado e realizar as substituições adequadas.
    ///
    /// OBS:válido apenas para Testes nos botões de chamada da Lista de Contatos e com contas novas.
    /// os mocks não contém IDs corretos.
    ///
    /// @author Ítalo Gomes

    public String SENDER_USER_ID;

    public String RECEIVER_USER_ID;
    public String RECEIVER_USER_NAME;
    public String RECEIVER_USER_PHONE_NUMBER;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupBasicUI();

        intentBuilder();

        Log.e(TAG, "SENDER_USER_ID: " + SENDER_USER_ID);

        // Layout Principal (LinearLayout para caber Console + WebView)
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setOrientation(LinearLayout.VERTICAL);

        // 1. CONSOLE VISUAL (Debugger na tela)
        debugConsole = new TextView(this);
        debugConsole.setTextColor(Color.GREEN);
        debugConsole.setBackgroundColor(Color.argb(220, 0, 0, 0));
        debugConsole.setPadding(20, 20, 20, 20);
        debugConsole.setMaxLines(6);
        debugConsole.setMovementMethod(new ScrollingMovementMethod());
        debugConsole.setText("--- DEBUG INICIADO ---\n");

        // Adiciona console (ocupa pouco espaço no topo)
        layout.addView(debugConsole, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 250));

        // 2. WEBVIEW
        webView = new WebView(this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setupWebViewSettings();

        layout.addView(webView);
        setContentView(layout);

        if (checkPermissions()) {
            loadUrlWithParams();
        } else {
            logToScreen("Solicitando permissões...");
            requestPermissions();
        }
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

        // --- LIMPEZA NUCLEAR DE CACHE (Para o Vite não atrapalhar) ---
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.clearCache(true);
        webView.clearHistory();
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();

        webView.setFitsSystemWindows(true);
        webView.addJavascriptInterface(new Activity_05_Video_Call.WebBridge(), "Android");

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

        // Chrome Client (Câmera + Upload + Logs JS)
        webView.setWebChromeClient(new WebChromeClient() {

            // Permissão de WebRTC
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        logToScreen("📷 Permissão Web: " + request.getResources().toString());
                        request.grant(request.getResources());
                    }
                });
            }

            // Logs do JavaScript (Console.log do site)
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                logToScreen("[JS] " + consoleMessage.message());
                return true;
            }

            // Upload de Arquivos (Mantido seu código original)
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
        String myUserId = "15";
        String myUserName = "AndroidApp";
        String targetUserId = "13";

        // URL CORRETA + Timestamp anti-cache
        String fullUrl = BASE_DOMAIN + VIDEO_ROUTE +
                "?userId=" + myUserId +
                "&userName=" + myUserName +
                "&targetId=" + targetUserId +
                "&t=" + System.currentTimeMillis();

        logToScreen("🚀 Indo para: " + fullUrl);
        webView.loadUrl(fullUrl);
    }

    // --- PERMISSÕES ---
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

    // --- UPLOAD RESULT ---
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

    public void setupBasicUI(){
        setTheme(androidx.appcompat.R.style.Theme_AppCompat);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        Objects.requireNonNull(getSupportActionBar()).hide();
    }

    public void intentBuilder(){
        this.SENDER_USER_ID = getIntent().getStringExtra("SENDER_USER_ID");

        this.RECEIVER_USER_ID = getIntent().getStringExtra("RECEIVER_USER_ID");
        this.RECEIVER_USER_NAME = getIntent().getStringExtra("RECEIVER_USER_NAME");
        this.RECEIVER_USER_PHONE_NUMBER = getIntent().getStringExtra("RECEIVER_USER_PHONE_NUMBER");
    }
}
