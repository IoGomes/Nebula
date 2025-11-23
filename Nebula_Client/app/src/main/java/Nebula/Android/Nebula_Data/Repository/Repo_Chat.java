package Nebula.Android.Nebula_Data.Repository;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Nebula.Android.Nebula_Data.LocalDb.DatabaseHelper;
import Nebula.Android.Nebula_Model.Entitys.Entity_Pv_Chat;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_01_Chat_Adapter;

public class Repo_Chat {

    private static List<Entity_Pv_Chat> chats = new ArrayList<>();
    private static RV_Feed_01_Chat_Adapter feedAdapter;
    private static DatabaseHelper dbHelper;

    public interface OnChatsChangedListener {
        void onChatsChanged();
    }

    private static OnChatsChangedListener chatsChangedListener;

    public static void initialize(Context context) {
        if (context == null) return;

        dbHelper = new DatabaseHelper(context);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            chats = dbHelper.getAllChatSessions();
        });
        executor.shutdown();
    }

    public static void removeChat(int index) {
        chats.remove(index);
        feedAdapter.notifyItemRemoved(index);
    }

    private static void notifyChatsChanged() {
        if (chatsChangedListener != null) {
            chatsChangedListener.onChatsChanged();
        }
    }

    public static void setOnChatsChangedListener(OnChatsChangedListener listener) {
        chatsChangedListener = listener;
    }

    public static List<Entity_Pv_Chat> getChats() {
        return chats;
    }

    public static void setChats(List<Entity_Pv_Chat> chatList) {
        if (chatList == null) return;
        chats = chatList;
        if (feedAdapter != null) {
            feedAdapter.notifyDataSetChanged();
        }
        notifyChatsChanged();
    }

    public static RV_Feed_01_Chat_Adapter getFeedAdapter() {
        return feedAdapter;
    }

    public static void setFeedAdapter(RV_Feed_01_Chat_Adapter adapter) {
        feedAdapter = adapter;
    }

    public static void addChat(Entity_Pv_Chat chat) {
        if (chat == null || dbHelper == null) return;
        chats.add(chat);
        dbHelper.insertChat(chat);
        if (feedAdapter != null) {
            feedAdapter.notifyItemInserted(chats.size() - 1);
        }
        notifyChatsChanged();
    }

    public static void addChatFromDatabase(Entity_Pv_Chat chat) {
        if (chat == null) return;
        chats.add(chat);
        if (feedAdapter != null) {
            feedAdapter.notifyItemInserted(chats.size() - 1);
        }
        notifyChatsChanged();
    }

    public static void removeChat(Entity_Pv_Chat chat) {
        synchronized (chats) {
            chats.remove(chat);
            notifyChatsChanged();
        }
    }

    public static void removeChat(Entity_Pv_Chat chat, int adapterPosition) {
        if (chat == null || dbHelper == null) return;

        if (chats.indexOf(chat) != -1) {

            chats.remove(chat);

            if (feedAdapter != null) {
                feedAdapter.notifyItemRemoved(adapterPosition);

                feedAdapter.notifyItemRangeChanged(adapterPosition,
                        feedAdapter.getItemCount());
            }

            dbHelper.deleteChat(chat.getChatSessionId());
        }
    }


    public static void favoriteChat(Entity_Pv_Chat chat) {
        if (chat == null || dbHelper == null) return;
        int index = chats.indexOf(chat);
        if (index != -1) {
            chat.setFavorite(true);
            dbHelper.updateChat(chat);
            notifyChatsChanged();
        }
    }
}

