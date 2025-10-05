package Mercury.Android.Mercury_ViewModel.Controllers;

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

import Mercury.Android.Mercury_Model.Services.Network_Checker;
import Mercury.Android.Mercury_Model.Services.Service_Permission;
import Mercury.Android.Mercury_Model.UseCases.UseCase_01_Login;
import Mercury.Android.Mercury_Model.UseCases.UseCase_02_Register;
import Mercury.Android.Mercury_View.Activities.Activity_02_Feed;
import Mercury.Android.Mercury_View.Dialogs.Dialog_Auth_01_Login_Credentials;
import Mercury.Android.Mercury_View.Utils.ToastWarning;
import Mercury.Android.R;

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
                new Network_Checker(activity),
                new Service_Permission(activity)
        );

        if (register.isEnabled()) {
            signupButton.setText("");
            loadAnimation.setVisibility(View.VISIBLE);
            if (loadAnimation instanceof LottieAnimationView) {
                ((LottieAnimationView) loadAnimation).playAnimation();
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                activity.startActivity(new Intent(activity, Activity_02_Feed.class));
                signupButton.setClickable(true);
            }, 1500);

        } else {
            new Dialog_Auth_01_Login_Credentials(activity).show();
            signupButton.setClickable(true);
        }
    }
}

