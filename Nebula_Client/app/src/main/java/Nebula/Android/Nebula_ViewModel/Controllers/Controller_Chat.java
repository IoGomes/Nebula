package Nebula.Android.Nebula_ViewModel.Controllers;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.security.identity.SessionTranscriptMismatchException;
import android.util.Log;

import Nebula.Android.Nebula_Data.Preferences.SessionPreferences;
import Nebula.Android.Nebula_View.Activities.Activity_03_Chat;
import Nebula.Android.R;

public class Controller_Chat {

    private final String TAG = "Controller_Chat";

    public void intentPutExtra(Intent intent,
                             String receiverId,
                             String receiverName,
                             String receiverNumber){
        intent.putExtra("RECEIVER_ID", receiverId);
        intent.putExtra("RECEIVER_NAME", receiverName);
        intent.putExtra("RECEIVER_NUMBER", receiverNumber);
    }

    public void performChat(Activity activity, String receiverId, String receiverName, String receiverNumber){

        Intent intent = new Intent(activity, Activity_03_Chat.class);
        intentPutExtra(intent, receiverId, receiverName, receiverNumber);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

}
