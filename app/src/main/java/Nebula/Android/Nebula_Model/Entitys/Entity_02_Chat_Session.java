package Nebula.Android.Nebula_Model.Entitys;

import java.util.Date;
import java.util.List;

public class Entity_02_Chat_Session {

    private String chatSessionId;
    private List<String> chatUsersId;
    private String usersNumbers;
    private List<Date> chatDate;
    private String lastMessage;

    public Entity_02_Chat_Session(String chatSessionId, List<String> chatUsersId,
                                  List<Date> chatDate, String lastMessage) {
        this.chatSessionId = chatSessionId;
        this.chatUsersId = chatUsersId;
        this.chatDate = chatDate;
        this.lastMessage = lastMessage;
    }

    public String getChatSessionId() {
        return chatSessionId;
    }

    public void setChatSessionId(String chatSessionId) {
        this.chatSessionId = chatSessionId;
    }

    public List<String> getChatUsersId() {
        return chatUsersId;
    }

    public void setChatUsersId(List<String> chatUsersId) {
        this.chatUsersId = chatUsersId;
    }

    public List<Date> getChatDate() {
        return chatDate;
    }

    public void setChatDate(List<Date> chatDate) {
        this.chatDate = chatDate;
    }


    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

}
