package Nebula.Android.Nebula_Model.Entitys;

import java.util.Date;

public class Entity_Message {
    private String message;
    private Date dateTimeMessage;
    private Boolean wasVisualized;
    private String senderName;
    private Boolean isSentByMe;

    // Getter e Setter para isSentByMe
    public Boolean isSentByMe() {
        return isSentByMe;
    }

    public void setIsSentByMe(Boolean isSentByMe) {
        this.isSentByMe = isSentByMe;
    }

    // Getters e Setters existentes
    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Boolean getWasVisualized() {
        return wasVisualized;
    }

    public void setWasVisualized(Boolean wasVisualized) {
        this.wasVisualized = wasVisualized;
    }

    public Date getDateTimeMessage() {
        return dateTimeMessage;
    }

    public void setDateTimeMessage(Date dateTimeMessage) {
        this.dateTimeMessage = dateTimeMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
