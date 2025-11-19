package Nebula.Android.Nebula_View.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import Nebula.Android.Nebula_View.Utils.NavBar_Inserts;
import Nebula.Android.R;

public class Activity_06_Web extends AppCompatActivity {

    private static final String TAG = "Activity_06_Web";

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

        // Configurações do WebView
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.setFitsSystemWindows(true);
        webView.setHapticFeedbackEnabled(false);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        webView.addJavascriptInterface(new WebBridge(), "Android");

        webView.loadUrl("file:///android_asset/WebView/GitHistory.html");
    }

    public class WebBridge {
        @JavascriptInterface
        public void showToast(String msg) {
            Toast.makeText(Activity_06_Web.this, msg, Toast.LENGTH_SHORT).show();
        }
    }
}