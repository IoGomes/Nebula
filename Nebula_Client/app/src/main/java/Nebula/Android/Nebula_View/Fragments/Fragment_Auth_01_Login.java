package Nebula.Android.Nebula_View.Fragments;

import static android.view.View.INVISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Nebula.Android.Nebula_View.Activities.git_login_activity;
import Nebula.Android.Nebula_ViewModel.Controllers.Controller_Auth;
import Nebula.Android.databinding.Frg01LoginBinding;

public class Fragment_Auth_01_Login extends Fragment {

    private Frg01LoginBinding bind;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        bind = Frg01LoginBinding.inflate(inflater, container, false);

        String forgot = "<u>Forgot Password?</u>";

        bind.textView2.setText(Html.fromHtml(forgot, Html.FROM_HTML_MODE_LEGACY));

        bind.login.setOnClickListener(v ->
                Controller_Auth.getInstance().performLogin(
                        requireActivity(),
                        bind.login,
                        bind.emailTextfield,
                        bind.userPasswordTextfield,
                        bind.loadAnimation,
                        executor,
                        mainHandler
                )
        );

        bind.google.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), git_login_activity.class);
                    startActivity(intent);
                });

        return bind.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind = null;
        executor.shutdownNow();
    }

}
