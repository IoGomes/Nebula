package Mercury.Android.Mercury_ViewModel.Controllers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;

import androidx.core.app.ActivityCompat;

import Mercury.Android.Mercury_Model.Services.Network_Checker;
import Mercury.Android.Mercury_Model.Services.Service_Permission;
import Mercury.Android.Mercury_Model.UseCases.UseCase_Video_Call;
import Mercury.Android.Mercury_View.Activities.Activity_05_Video_Call;
import Mercury.Android.R;

public class Controller_Video_Call {

    private final UseCase_Video_Call useCaseVideoCall;

    private static final int VIDEO_CALL_PERMISSION_REQUEST_CODE = 100;

    public Controller_Video_Call(Activity activity) {
        Service_Permission servicePermission = new Service_Permission(activity);
        Network_Checker networkChecker = new Network_Checker(activity);
        useCaseVideoCall = new UseCase_Video_Call(servicePermission, networkChecker);
    }

    public void performVideoCall(Activity activity) {
        if (useCaseVideoCall.isEnabled()) {
            Intent intent = new Intent(activity, Activity_05_Video_Call.class);
            activity.startActivity(intent);
            activity.overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
            );
        } else {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                    },
                    VIDEO_CALL_PERMISSION_REQUEST_CODE
            );
        }
    }
    }