package Nebula.Android.Nebula_Data.Preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionPreferences {
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_LOGGED_IN = "isLoggedIn";
    private static final String KEY_DID_ONBOARD_TUTORIAL = "isOnboardTutorialDone";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ID = "keyId";

    private SharedPreferences prefs;

    public void generateQrCode(){

    }

    public void setKeyId(String id) {
        prefs.edit().putString(KEY_ID, id).apply();
    }

    public String getKeyId(){
        return prefs.getString(KEY_ID, null);
    }

    public SessionPreferences(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setIsLoggedIn(boolean value) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, value).apply();
    }

    public void saveLogin(String username) {
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "Usuário");
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}
