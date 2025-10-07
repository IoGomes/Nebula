package Nebula.Android.Nebula_View.Fragments;

import static android.view.View.GONE;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import Nebula.Android.Nebula_ViewModel.Controllers.Controller_Auth;
import Nebula.Android.R;
import Nebula.Android.databinding.Frg02RegisterBinding;

/// @author Ítalo Oliveira Gomes

public class Fragment_Auth_02_Register extends Fragment {

    Frg02RegisterBinding bind;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        bind = Frg02RegisterBinding.inflate(inflater, container, false);

        String htmlTermsAndConditions = "I agree with the <u>Terms and Conditions</u>";
        String htmlPrivacyPolicy = "I agree with the <u>Privacy Policy</u>";

        bind.termsAndConditions.setText(Html.fromHtml(htmlTermsAndConditions, Html.FROM_HTML_MODE_LEGACY));
        bind.privacyPolicy.setText(Html.fromHtml(htmlPrivacyPolicy, Html.FROM_HTML_MODE_LEGACY));

        bind.signupButton.setOnClickListener(v ->
                Controller_Auth.getInstance().performRegister(
                        requireActivity(),
                        bind.signupButton,
                        bind.userNameTextfield,
                        bind.userEmailTextfield,
                        bind.userTelefoneTextfield,
                        bind.userPasswordTextfield,
                        bind.confirmUserPasswordTextfield,
                        bind.termosECondicoes,
                        bind.termosEPrivacidade,
                        bind.loadAnimation
                )
        );

        bind.googleLogin.setOnClickListener(v ->
                new Controller_Auth().performGoogleLogin(requireContext()));

        bind.gitAuth.setOnClickListener(view ->
                new Controller_Auth().performGitLogin(requireContext()));

        return bind.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();

        bind.signupButton.setText(R.string.signup_fragment);
        bind.loadAnimation.cancelAnimation();
        bind.loadAnimation.setVisibility(GONE);
        bind.signupButton.setClickable(true);
        bind.userNameTextfield.setText("");
        bind.userEmailTextfield.setText("");
        bind.userTelefoneTextfield.setText("");
        bind.userPasswordTextfield.setText("");
        bind.confirmUserPasswordTextfield.setText("");
        bind.termosECondicoes.setChecked(false);
        bind.termosEPrivacidade.setChecked(false);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bind = null;
    }
}