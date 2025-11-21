package Nebula.Android.Nebula_View.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Activity_GitAuth extends AppCompatActivity {

    private WebView webView;
    private ProgressBar loading;

    private static final String CLIENT_ID = "SEU_CLIENT_ID";
    private static final String REDIRECT_URI = "myapp://auth/github";

    private static final String BACKEND_OAUTH_URL = "https://SEU_BACKEND.com/auth/github/callback";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        setTheme(androidx.appcompat.R.style.Theme_AppCompat);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        super.onCreate(savedInstanceState);

        try { Objects.requireNonNull(getSupportActionBar()).hide(); }
        catch (Exception ignored) {}

        FrameLayout root = new FrameLayout(this);
        webView = new WebView(this);
        loading = new ProgressBar(this);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        params.gravity = android.view.Gravity.CENTER;

        root.addView(webView);
        root.addView(loading, params);

        setContentView(root);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setUserAgentString("Mozilla/5.0 (Android)");

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                loading.setVisibility(android.view.View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

                String url = request.getUrl().toString();

                // Intercepta o redirect_uri
                if (url.startsWith(REDIRECT_URI)) {

                    Uri uri = Uri.parse(url);
                    String code = uri.getQueryParameter("code");

                    if (code != null) {
                        trocarCodePorToken(code);
                    } else {
                        Toast.makeText(Activity_GitAuth.this,
                                "Erro ao receber código", Toast.LENGTH_SHORT).show();
                    }

                    return true;
                }

                return false;
            }
        });

        carregarGithubOAuth();
    }

    private void carregarGithubOAuth() {
        String loginUrl =
                "https://github.com/login/oauth/authorize" +
                        "?client_id=" + CLIENT_ID +
                        "&scope=read:user%20user:email" +
                        "&redirect_uri=" + REDIRECT_URI;

        loading.setVisibility(android.view.View.VISIBLE);
        webView.loadUrl(loginUrl);
    }

    private void trocarCodePorToken(String code) {

        loading.setVisibility(android.view.View.VISIBLE);

        OkHttpClient client = new OkHttpClient();

        // Envia somente o code para o backend
        RequestBody body = new FormBody.Builder()
                .add("code", code)
                .add("redirect_uri", REDIRECT_URI)
                .build();

        Request request = new Request.Builder()
                .url(BACKEND_OAUTH_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String json = response.body().string();

                runOnUiThread(() -> {
                    Intent result = new Intent();
                    result.putExtra("result_json", json);
                    setResult(RESULT_OK, result);
                    finish();
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(Activity_GitAuth.this,
                                "Erro ao autenticar", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}
