package Nebula.Android.Nebula_View.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import Nebula.Android.Nebula_View.Utils.NavBar_Inserts;
import Nebula.Android.R;

public class Activity_06_Git_WebHook extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1001;

    private ValueCallback<Uri[]> filePathCallback;
    private Uri tempPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(androidx.appcompat.R.style.Theme_AppCompat);
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        View rootLayout = findViewById(R.id.root);
        NavBar_Inserts.adjustPaddingForNavigationBar(rootLayout, this);

        Objects.requireNonNull(getSupportActionBar()).hide();

        WebView webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.setFitsSystemWindows(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        webView.addJavascriptInterface(new WebBridge(), "Android");

        // Habilita input de arquivo + câmera
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {

                Activity_06_Git_WebHook.this.filePathCallback = filePathCallback;

                // Cria arquivo temporário para a foto
                File photoFile;
                try {
                    photoFile = File.createTempFile(
                            "IMG_", ".jpg",
                            getExternalCacheDir()
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }

                tempPhotoUri = FileProvider.getUriForFile(
                        Activity_06_Git_WebHook.this,
                        getPackageName() + ".provider",
                        photoFile
                );

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, tempPhotoUri);

                startActivityForResult(intent, CAMERA_REQUEST);
                return true;
            }
        });

        webView.loadUrl("file:///android_asset/WebView/GitHistory.html");
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
            Toast.makeText(Activity_06_Git_WebHook.this, msg, Toast.LENGTH_SHORT).show();
        }
    }
}
