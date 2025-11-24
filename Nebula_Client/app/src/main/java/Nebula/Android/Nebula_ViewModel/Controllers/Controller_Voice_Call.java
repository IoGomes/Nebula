package Nebula.Android.Nebula_ViewModel.Controllers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;

import androidx.core.app.ActivityCompat;

import Nebula.Android.Nebula_Model.Services.Svc_Network_Checker;
import Nebula.Android.Nebula_Model.Services.Svc_Permission;
import Nebula.Android.Nebula_Model.UseCases.UseCase_Voice_Call;
import Nebula.Android.Nebula_View.Activities.Activity_04_Voice_Call;
import Nebula.Android.R;

public class Controller_Voice_Call {

    private final UseCase_Voice_Call useCaseVoiceCall;

    private static final int VOICE_CALL_PERMISSION_REQUEST_CODE = 100;

    public Controller_Voice_Call(Activity activity) {
        Svc_Permission servicePermission = new Svc_Permission(activity);
        Svc_Network_Checker networkChecker = new Svc_Network_Checker(activity);
        useCaseVoiceCall = new UseCase_Voice_Call(servicePermission, networkChecker);
    }

    private void intentBuilder(Intent intent, String senderId, String receiverId, String receiverName, String receiverNumber){
        intent.putExtra("SENDER_USER_ID", senderId);
        intent.putExtra("RECEIVER_USER_ID", receiverId);
        intent.putExtra("RECEIVER_USER_NAME", receiverName);
        intent.putExtra("RECEIVER_USER_PHONE_NUMBER", receiverNumber);
    }

    public void performVoiceCall(Activity activity, String senderId, String receiverId, String receiverName, String receiverNumber) {
        if (useCaseVoiceCall.isEnabled()) {
            Intent intent = new Intent(activity, Activity_04_Voice_Call.class);
            intentBuilder(intent, senderId, receiverId, receiverName, receiverNumber);
            activity.startActivity(intent);
            activity.overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
            );
        }
        else {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                    },
                    VOICE_CALL_PERMISSION_REQUEST_CODE
            );
        }
    }
}
