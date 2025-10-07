package Nebula.Android.Nebula_Data;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferences {
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";

    private SharedPreferences prefs;

    public UserPreferences(Context context) {
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
