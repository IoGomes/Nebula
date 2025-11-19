package Nebula.Android.Nebula_Model.Entitys;

import java.util.Date;

@SuppressWarnings("SpellCheckingInspection")
public class Entity_Call {

    public String getCallID() {
        return callID;
    }

    public void setCallID(String callID) {
        this.callID = callID;
    }

    private String callID;
    private String nomeDeContato;
    private Date dateTimeCall;
    private  Boolean reiceved;

    public Entity_Call(){}

    public Entity_Call(String nomeDeContato, Date dateTimeCall, Boolean reiceved) {
        setNomeDeContato(nomeDeContato);
        setDateTimeCall(dateTimeCall);
        setReiceved(reiceved);
    }

    public Date getDateTimeCall() {
        return dateTimeCall;
    }

    public void setDateTimeCall(Date dateTimeCall) {
        this.dateTimeCall = dateTimeCall;
    }

    public String getNomeDeContato() {
        return nomeDeContato;
    }

    public void setNomeDeContato(String nomeDeContato) {
        this.nomeDeContato = nomeDeContato;
    }

    public Boolean getReiceved() {
        return reiceved;
    }

    public void setReiceved(Boolean reiceved) {
        this.reiceved = reiceved;
    }
}
