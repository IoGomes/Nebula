package Nebula.Android.Nebula_Model.Repository;

import java.util.ArrayList;
import java.util.List;

import Nebula.Android.Nebula_Model.Entitys.Entity_02_Chat_Session;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_01_Chat_Adapter;

public class Chat_Repository {

    private static List<Entity_02_Chat_Session> chats = new ArrayList<>();
    private static RV_Feed_01_Chat_Adapter feedAdapter;

    public static List<Entity_02_Chat_Session> getChats() {
        return chats;
    }

    public static void setChats(List<Entity_02_Chat_Session> chatList) {
        chats = chatList;
    }

    public static RV_Feed_01_Chat_Adapter getFeedAdapter() {
        return feedAdapter;
    }

    public static void setFeedAdapter(RV_Feed_01_Chat_Adapter adapter) {
        feedAdapter = adapter;
    }
}