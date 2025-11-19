package Nebula.Android.Nebula_Model.Entitys;

import java.sql.Time;
import java.util.List;

public class Entity_Video_Chat_Session {

    private List<String> usersId;
    private String videoChatSessionId;
    private boolean wasAttended;
    private Time videoChatSessionDurationTime;

    public List<String> getUsersId() {
        return usersId;
    }

    public void setUsersId(List<String> usersId) {
        this.usersId = usersId;
    }

    public String getVideoChatSessionId() {
        return videoChatSessionId;
    }

    public void setVideoChatSessionId(String videoChatSessionId) {
        this.videoChatSessionId = videoChatSessionId;
    }

    public boolean isWasAttended() {
        return wasAttended;
    }

    public void setWasAttended(boolean wasAttended) {
        this.wasAttended = wasAttended;
    }

    public Time getVideoChatSessionDurationTime() {
        return videoChatSessionDurationTime;
    }

    public void setVideoChatSessionDurationTime(Time videoChatSessionDurationTime) {
        this.videoChatSessionDurationTime = videoChatSessionDurationTime;
    }
}
