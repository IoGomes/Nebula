package Nebula.Android.Nebula_Model.Repository;

import java.util.ArrayList;
import java.util.List;

import Nebula.Android.Nebula_Model.Entitys.Entity_02_Chat_Session;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_01_Chat_Adapter;

public class Repo_Chat {

    private static List<Entity_02_Chat_Session> chats = new ArrayList<>();
    private static RV_Feed_01_Chat_Adapter feedAdapter;

    public static List<Entity_02_Chat_Session> getChats() {
        return chats;
    }

    public static void setChats(List<Entity_02_Chat_Session> chatList) {
        chats = chatList;
        if (feedAdapter != null) {
            feedAdapter.notifyDataSetChanged();
        }
    }

    public static RV_Feed_01_Chat_Adapter getFeedAdapter() {
        return feedAdapter;
    }

    public static void setFeedAdapter(RV_Feed_01_Chat_Adapter adapter) {
        feedAdapter = adapter;
    }

    public static void addChat(Entity_02_Chat_Session chat) {
        if (chat != null) {
            chats.add(chat);
            if (feedAdapter != null) {
                feedAdapter.notifyItemInserted(chats.size() - 1);
            }
        }
    }

    public static void removeChat(Entity_02_Chat_Session chat) {
        if (chat != null) {
            int index = chats.indexOf(chat);
            if (index != -1) {
                chats.remove(index);
                if (feedAdapter != null) {
                    feedAdapter.notifyItemRemoved(index);
                }
            }
        }
    }

    public static void removeChatAt(int position) {
        if (position >= 0 && position < chats.size()) {
            chats.remove(position);
            if (feedAdapter != null) {
                feedAdapter.notifyItemRemoved(position);
            }
        }
    }
}
