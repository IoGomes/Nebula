package Nebula.Android.Nebula_ViewModel.Controllers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import Nebula.Android.Nebula_Model.Entitys.Entity_Pv_Chat;
import Nebula.Android.Nebula_View.Activities.Activity_03_Chat;
import Nebula.Android.Nebula_ViewModel.Server_Services.Service_Online;

import androidx.core.app.ActivityCompat;

import Nebula.Android.Nebula_Model.Services.Svc_Network_Checker;
import Nebula.Android.Nebula_Model.Services.Svc_Permission;
import Nebula.Android.Nebula_Model.UseCases.UseCase_Video_Call;
import Nebula.Android.Nebula_View.Activities.Activity_05_Video_Call;
import Nebula.Android.R;
import io.socket.client.Socket;

public class Controller_Video_Call {

    private final UseCase_Video_Call useCaseVideoCall;

    private static final int VIDEO_CALL_PERMISSION_REQUEST_CODE = 100;

    public Controller_Video_Call(Activity activity) {
        Svc_Permission servicePermission = new Svc_Permission(activity);
        Svc_Network_Checker networkChecker = new Svc_Network_Checker(activity);
        useCaseVideoCall = new UseCase_Video_Call(servicePermission, networkChecker);
    }

    public void intentPutExtra(Intent intent, String userName, String userId, String contactNumber){
        intent.putExtra("SENDER_USER_ID", userId);

        intent.putExtra("RECEIVER_USER_ID", userId);
        intent.putExtra("RECEIVER_USER_NAME", userName);
        intent.putExtra("RECEIVER_USER_PHONE_NUMBER", contactNumber);
    }

    public void performVideoCall(Activity activity, String userName, String userId, String contactNumber) {

        if (useCaseVideoCall.isEnabled())
        {
            Intent intent = new Intent(activity, Activity_05_Video_Call.class);
            intentPutExtra(intent, userName, userId, contactNumber);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        else {
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