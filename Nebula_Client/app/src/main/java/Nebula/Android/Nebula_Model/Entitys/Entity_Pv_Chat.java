package Nebula.Android.Nebula_Model.Entitys;

import java.util.Date;
import java.util.List;

@SuppressWarnings("SpellCheckingInspection")
public class Entity_Pv_Chat {

    private String chatWith;
    private String chatWithNumber;
    private boolean hasUnread = true;
    private boolean isFavorited = false;
    private String chatSessionId;
    private List<String> chatUsersId;
    private List<Date> chatDate;
    private String lastMessage;

    public Entity_Pv_Chat() {

    }



    public Entity_Pv_Chat(String chatSessionId, List<String> chatUsersId,
                          List<Date> chatDate, String lastMessage, String chatWith, String chatWithNumber) {
        this.chatSessionId = chatSessionId;
        this.chatWith = chatWith;
        this.chatWithNumber = chatWithNumber;
        this.chatUsersId = chatUsersId;
        this.chatDate = chatDate;
        this.lastMessage = lastMessage;
    }

    public String getChatWithNumber() {
        return chatWithNumber;
    }

    public void setChatWithNumber(String chatWithNumber) {
        this.chatWithNumber = chatWithNumber;
    }

    public boolean hasUnread() {
        return hasUnread;
    }

    public void setHasUnread(boolean hasUnread) {
        this.hasUnread = hasUnread;
    }

    public List<Date> getChatDate() {
        return chatDate;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean isFavorite() {
        return isFavorited;  // ← CORREÇÃO AQUI
    }

    public void setFavorite(boolean favorited) {
        this.isFavorited = favorited;
    }

    public String getChatWith() {
        return chatWith;
    }

    public void setChatWith(String chatWith) {
        this.chatWith = chatWith;
    }

    public String getChatSessionId() {
        return chatSessionId;
    }

    public void setChatSessionId(String chatSessionId) {
        this.chatSessionId = chatSessionId;
    }
}