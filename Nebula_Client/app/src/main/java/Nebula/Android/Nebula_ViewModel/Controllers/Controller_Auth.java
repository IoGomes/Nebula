package Nebula.Android.Nebula_ViewModel.Controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.widget.SwitchCompat;

import com.airbnb.lottie.LottieAnimationView;

import java.util.concurrent.ExecutorService;

import Nebula.Android.Nebula_Data.Preferences.SessionPreferences;
import Nebula.Android.Nebula_Model.Services.Svc_Network_Checker;
import Nebula.Android.Nebula_Model.Services.Svc_Permission;
import Nebula.Android.Nebula_Model.UseCases.UseCase_01_Login;
import Nebula.Android.Nebula_Model.UseCases.UseCase_02_Register;
import Nebula.Android.Nebula_View.Activities.Activity_02_Feed;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Auth_Login_Credentials;
import Nebula.Android.Nebula_View.Utils.ToastWarning;
import Nebula.Android.R;

public class Controller_Auth {
    private static final Controller_Auth instance = new Controller_Auth();



    public Controller_Auth() {
    }

    public static Controller_Auth getInstance() {
        return instance;
    }

    public void performGoogleLogin(Context context) {
        new ToastWarning(context)
                .showInfo("Google Sign-in is still under development.");

    }

    public void performGitLogin(Context context) {
        new ToastWarning(context)
                .showInfo("Google Sign-in is still under development.");
    }

    public void performLogin(
            Activity activity,
            Button loginButton,
            ImageButton googleButton,
            EditText emailField,
            EditText passwordField,
            View loadAnimation,
            ExecutorService executor,
            Handler mainHandler
    ) {
        loginButton.setClickable(false);
        googleButton.setClickable(false);

        final String email = emailField.getText().toString();
        final String password = passwordField.getText().toString();

        loginButton.setText(null);
        loadAnimation.setVisibility(View.VISIBLE);
        SessionPreferences prefsData = new SessionPreferences(activity);
        prefsData.setIsLoggedIn(true);
        if (loadAnimation instanceof LottieAnimationView) {
            ((LottieAnimationView) loadAnimation).playAnimation();
        }

        executor.submit(() -> {
            new UseCase_01_Login(email, password);

            mainHandler.post(() -> mainHandler.postDelayed(() -> {

                activity.startActivity(new Intent(activity, Activity_02_Feed.class));
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                loginButton.setClickable(true);
                googleButton.setClickable(true);

            }, 1500));
        });
    }

    public void performRegister(
            Activity activity,
            Button signupButton,
            EditText userNameField,
            EditText userEmailField,
            EditText userTelefoneField,
            EditText userPasswordField,
            EditText confirmPasswordField,
            SwitchCompat termosCondicoes,
            SwitchCompat termosPrivacidade,
            View loadAnimation
    ) {
        signupButton.setClickable(false);

        UseCase_02_Register register = new UseCase_02_Register(
                userNameField.getText().toString(),
                userEmailField.getText().toString(),
                userTelefoneField.getText().toString(),
                userPasswordField.getText().toString(),
                confirmPasswordField.getText().toString(),
                termosCondicoes.isChecked(),
                termosPrivacidade.isChecked(),
                new Svc_Network_Checker(activity),
                new Svc_Permission(activity)
        );

        if (register.isEnabled()) {
            signupButton.setText("");
            loadAnimation.setVisibility(View.VISIBLE);
            SessionPreferences prefsData = new SessionPreferences(activity);
            prefsData.setIsLoggedIn(true);
            if (loadAnimation instanceof LottieAnimationView) {
                ((LottieAnimationView) loadAnimation).playAnimation();
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                activity.startActivity(new Intent(activity, Activity_02_Feed.class));
                signupButton.setClickable(true);
            }, 1500);

        } else {
            new Dialog_Auth_Login_Credentials(activity).show();
            signupButton.setClickable(true);
        }
    }
}

