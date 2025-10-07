package Nebula.Android.Nebula_Model.Entitys;

import java.util.Date;

@SuppressWarnings("SpellCheckingInspection")
public class Entity_05_call {

    private String nomeDeContato;
    private Date dateTimeCall;
    private  Boolean reiceved;


    public Entity_05_call(String nomeDeContato, Date dateTimeCall, Boolean reiceved) {
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
