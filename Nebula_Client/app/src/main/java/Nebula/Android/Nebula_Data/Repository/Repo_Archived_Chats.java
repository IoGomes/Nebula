package Nebula.Android.Nebula_Data.Repository;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import Nebula.Android.Nebula_Model.Entitys.Entity_Pv_Chat;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_04_Archived_Adapter;

public class Repo_Archived_Chats {

    private final static String TAG = "Repo_Archived_Chats";
    private static List<Entity_Pv_Chat> archivedChats = new ArrayList<>();
    private static RV_Feed_04_Archived_Adapter archivedAdapter;

    public interface OnArchivedChatsChangedListener {
        void onArchivedChatsChanged();
    }

    private static OnArchivedChatsChangedListener archivedChangedListener;

    private static void notifyArchivedChatsChanged() {
        if (archivedChangedListener != null) {
            archivedChangedListener.onArchivedChatsChanged();
            Log.e(TAG, "");
        }
    }

    public static List<Entity_Pv_Chat> getArchivedChats() {
        return archivedChats;
    }

    public static void setArchivedChats(List<Entity_Pv_Chat> chatList) {
        archivedChats = chatList;
        if (archivedAdapter != null) {
            archivedAdapter.notifyDataSetChanged();
            notifyArchivedChatsChanged();
            Log.e(TAG, "New Chat Archived");
        }
    }

    public static RV_Feed_04_Archived_Adapter getArchivedAdapter() {
        return archivedAdapter;
    }

    public static void setArchivedAdapter(RV_Feed_04_Archived_Adapter adapter) {
        archivedAdapter = adapter;
    }

    public static void addArchivedChat(Entity_Pv_Chat chat) {
        if (chat != null) {
            archivedChats.add(chat);
            if (archivedAdapter != null) {
                archivedAdapter.notifyItemInserted(archivedChats.size() - 1);
            }
        }
    }


    public static void addArchivedChat(List<Entity_Pv_Chat> chats) {
        if (chats == null || chats.isEmpty()) return;

        int startPos = archivedChats.size();

        for (Entity_Pv_Chat chat : chats) {
            if (chat != null) {
                archivedChats.add(chat);
            }
        }

        if (archivedAdapter != null) {

            archivedAdapter.notifyItemRangeInserted(startPos, chats.size());
        }
    }


    public static void removeArchivedChat(Entity_Pv_Chat chat) {
        if (chat != null) {
            int index = archivedChats.indexOf(chat);
            if (index != -1) {
                archivedChats.remove(index);
                if (archivedAdapter != null) {
                    archivedAdapter.notifyItemRemoved(index);
                }
            }
        }
    }

    public static void unarchiveChat(Entity_Pv_Chat chat) {
        if (chat != null) {
            removeArchivedChat(chat);
            Repo_Chat.addChat(chat);
        }
    }

    public static void unarchiveChatAt(int position) {
        if (position >= 0 && position < archivedChats.size()) {
            Entity_Pv_Chat chat = archivedChats.get(position);
            unarchiveChat(chat);
        }
    }
}