package mapabea.mapabea;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("is_login")
    private int isLogin;
    @SerializedName("user_id")
    private int id;

    @SerializedName("branch_id")
    private int branchId;
    @SerializedName("hub_id")
    private int hubId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }


    public int getIsLogin() {
        return isLogin;
    }

    public void setIsLogin(int isLogin) {
        this.isLogin = isLogin;
    }

    public int getHubId() {
        return hubId;
    }

    public void setHubId(int hubId) {
        this.hubId = hubId;
    }
}
