package Nebula.Android.Nebula_Model.Entitys;

import java.util.Date;
import java.util.List;

@SuppressWarnings("SpellCheckingInspection")
public class Entity_Chat {

    private String chatId;
    private String senderId;
    private String receiverId;
    private String receiverName;
    private String receiverNumber;
    private String lastMessageSend;
    private List<Date> lastMessageSendDate;
    private boolean isRead = false;
    private boolean isFavorited = false;
    private boolean isChatEnabled = false;

    public Entity_Chat() {}

    public Entity_Chat(String chatId, String senderId, String receiverId, String receiverName, String receiverNumber, String lastMessageSend, List<Date> lastMessageSendDate) {
        setChatId(chatId);
        setSenderId(senderId);
        setReceiverId(receiverId);
        setReceiverName(receiverName);
        setReceiverNumber(receiverNumber);
        setLastMessageSend(lastMessageSend);
        setLastMessageSendDate(lastMessageSendDate);
    }

    public String getChatId() {
        return chatId;
    }
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSenderId() {
        return senderId;
    }
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverNumber() {
        return receiverNumber;
    }
    public void setReceiverNumber(String receiverNumber) {
        this.receiverNumber = receiverNumber;
    }

    public String getReceiverId() {
        return receiverId;
    }
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public boolean hasUnread() {
        return isRead;
    }
    public void setRead(boolean read) {
        this.isRead = read;
    }

    public List<Date> getLastMessageSendDate() {
        return lastMessageSendDate;
    }
    public void setLastMessageSendDate(List<Date> lastMessageSendDate) {
        this.lastMessageSendDate = lastMessageSendDate;
    }

    public String getLastMessageSend() {
        return lastMessageSend;
    }
    public void setLastMessageSend(String lastMessageSend) {
        this.lastMessageSend = lastMessageSend;
    }

    public boolean isFavorite() {
        return isFavorited;
    }
    public void setFavorite(boolean favorited) {
        this.isFavorited = favorited;
    }

    public String getReceiverName() {
        return receiverName;
    }
    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public boolean isChatEnabled() {
        return isChatEnabled;
    }
    public void setChatEnabled(boolean chatEnabled) {
        isChatEnabled = chatEnabled;
    }
}