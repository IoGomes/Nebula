package Nebula.Android.Nebula_Model.Repository;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Nebula.Android.Nebula_Model.Entitys.Entity_03_Message;

public class Repo_Chat_Storage {

    private static final String PREFS_NAME = "NebulaChatPrefs";
    private static final String KEY_MESSAGES_PREFIX = "messages_";
    private SharedPreferences sharedPreferences;
    private Context context;

    public Repo_Chat_Storage(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveMessages(String chatSessionId, List<Entity_03_Message> messages) {
        JSONArray jsonArray = new JSONArray();

        try {
            for (Entity_03_Message message : messages) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("message", message.getMessage());
                jsonObject.put("senderName", message.getSenderName());
                jsonObject.put("dateTimeMessage", message.getDateTimeMessage().getTime());
                jsonObject.put("wasVisualized", message.getWasVisualized());
                jsonObject.put("isSentByMe", message.isSentByMe());

                jsonArray.put(jsonObject);
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_MESSAGES_PREFIX + chatSessionId, jsonArray.toString());
            editor.apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<Entity_03_Message> loadMessages(String chatSessionId) {
        List<Entity_03_Message> messages = new ArrayList<>();
        String jsonString = sharedPreferences.getString(KEY_MESSAGES_PREFIX + chatSessionId, "[]");

        try {
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                Entity_03_Message message = new Entity_03_Message();
                message.setMessage(jsonObject.getString("message"));
                message.setSenderName(jsonObject.getString("senderName"));
                message.setDateTimeMessage(new Date(jsonObject.getLong("dateTimeMessage")));
                message.setWasVisualized(jsonObject.getBoolean("wasVisualized"));
                message.setIsSentByMe(jsonObject.getBoolean("isSentByMe"));

                messages.add(message);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return messages;
    }

    public void addMessage(String chatSessionId, Entity_03_Message message) {
        List<Entity_03_Message> messages = loadMessages(chatSessionId);
        messages.add(message);
        saveMessages(chatSessionId, messages);
    }

    public void deleteMessage(String chatSessionId, int position) {
        List<Entity_03_Message> messages = loadMessages(chatSessionId);
        if (position >= 0 && position < messages.size()) {
            messages.remove(position);
            saveMessages(chatSessionId, messages);
        }
    }


    public void clearMessages(String chatSessionId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_MESSAGES_PREFIX + chatSessionId);
        editor.apply();
    }

    public void clearAllChats() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }


    public int getMessagesCount(String chatSessionId) {
        return loadMessages(chatSessionId).size();
    }

    public void markAllAsRead(String chatSessionId) {
        List<Entity_03_Message> messages = loadMessages(chatSessionId);
        for (Entity_03_Message message : messages) {
            if (!message.isSentByMe()) {
                message.setWasVisualized(true);
            }
        }
        saveMessages(chatSessionId, messages);
    }

    public int getUnreadCount(String chatSessionId) {
        List<Entity_03_Message> messages = loadMessages(chatSessionId);
        int count = 0;
        for (Entity_03_Message message : messages) {
            if (!message.isSentByMe() && !message.getWasVisualized()) {
                count++;
            }
        }
        return count;
    }

    public boolean hasSavedMessages(String chatSessionId) {
        return sharedPreferences.contains(KEY_MESSAGES_PREFIX + chatSessionId);
    }
}
