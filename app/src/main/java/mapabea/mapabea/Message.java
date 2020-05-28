package mapabea.mapabea;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Message {

    @SerializedName("body")
    private String message;

    @SerializedName("actual_time")
    private String timestamp;

    @SerializedName("hub_recepient_id")
    private int hub_recepient_id;

    @SerializedName("rider_recepient_id")
    private int rider_recepient_id;

    @SerializedName("sender_id")
    private int sender_id;

    @SerializedName("type")
    private int type;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public int getSender_id() {
        return sender_id;
    }

    public void setSender_id(int sender_id) {
        this.sender_id = sender_id;
    }

    public int getHub_recepient_id() {
        return hub_recepient_id;
    }

    public void setHub_recepient_id(int hub_recepient_id) {
        this.hub_recepient_id = hub_recepient_id;
    }

    public int getRider_recepient_id() {
        return rider_recepient_id;
    }

    public void setRider_recepient_id(int rider_recepient_id) {
        this.rider_recepient_id = rider_recepient_id;
    }
}
