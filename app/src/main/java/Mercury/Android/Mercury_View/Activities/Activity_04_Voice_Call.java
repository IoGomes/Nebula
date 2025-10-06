package Mercury.Android.Mercury_View.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import Mercury.Android.Mercury_View.Utils.NavBar_Inserts;
import Mercury.Android.R;
import Mercury.Android.databinding.ActivityVoiceCallBinding;

public class Activity_04_Voice_Call extends AppCompatActivity {

    private ActivityVoiceCallBinding bind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        setTheme(androidx.appcompat.R.style.Theme_AppCompat);

        super.onCreate(savedInstanceState);

        View rootLayout = findViewById(R.id.root);
        NavBar_Inserts.adjustPaddingForNavigationBar(rootLayout, this);

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        Objects.requireNonNull(getSupportActionBar()).hide();

        bind = ActivityVoiceCallBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

    }
}
