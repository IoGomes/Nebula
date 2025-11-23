package Nebula.Android.Nebula_Model.Entitys;

import java.util.Date;

@SuppressWarnings("SpellCheckingInspection")
public class Entity_Call {

    private String contactNumber;
    private Date dateTimeCall;
    private  Boolean reiceved;

    public String getCallID() {
        return callID;
    }

    public void setCallID(String callID) {
        this.callID = callID;
    }
    private String callID;
    private String contactName;

    public Entity_Call(){}

    public Entity_Call(String contactName, Date dateTimeCall, Boolean reiceved) {
        setContactName(contactName);
        setDateTimeCall(dateTimeCall);
        setReiceved(reiceved);
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Date getDateTimeCall() {
        return dateTimeCall;
    }

    public void setDateTimeCall(Date dateTimeCall) {
        this.dateTimeCall = dateTimeCall;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String nomeDeContato) {
        this.contactName = nomeDeContato;
    }

    public Boolean getReiceved() {
        return reiceved;
    }

    public void setReiceved(Boolean reiceved) {
        this.reiceved = reiceved;
    }
}
