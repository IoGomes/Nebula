package Nebula.Android.Nebula_View.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import Nebula.Android.Nebula_View.Utils.NavBar_Inserts;
import Nebula.Android.R;
import Nebula.Android.databinding.Act04VoiceCallBinding;

public class Activity_04_Voice_Call extends AppCompatActivity {

    private Act04VoiceCallBinding bind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        setTheme(androidx.appcompat.R.style.Theme_AppCompat);

        super.onCreate(savedInstanceState);

        View rootLayout = findViewById(R.id.root);
        NavBar_Inserts.adjustPaddingForNavigationBar(rootLayout, this);

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        Objects.requireNonNull(getSupportActionBar()).hide();

        bind = Act04VoiceCallBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

    }
}
