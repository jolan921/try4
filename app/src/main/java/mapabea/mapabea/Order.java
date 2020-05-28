package mapabea.mapabea;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Order {

    @SerializedName("status_id")
    private int statusId;
    @SerializedName("id")
    private int id;
    @SerializedName("order_number")
    private String orderNumber;
    @SerializedName("address")
    private String orderConsigneeAddress;
    @SerializedName("contact_number")
    private String orderConsigneeContactNumber;
    @SerializedName("consignee_name")
    private String orderOriginalConsigneeName;
    @SerializedName("device_id")
    private int orderDeviceId;
    @SerializedName("lat")
    private float lat;
    @SerializedName("lng")
    private float lng;
    @SerializedName("branch_lat")
    private float branch_lat;
    @SerializedName("branch_lng")
    private float branch_lng;
    @SerializedName("client_id")
    private int client_id;
    @SerializedName("branch_name")
    private String branchName;
    @SerializedName("vicinityin_timestamp")
    private String vicinityInTimestamp;
    @SerializedName("storein_timestamp")
    private String storeInTimestamp;
    @SerializedName("storeout_timestamp")
    private String storeOutTimestamp;
    @SerializedName("delivered_timestamp")
    private String deliveredTimestamp;
    @SerializedName("served_timestamp")
    private String servedTimestamp;
    @SerializedName("remitted_timestamp")
    private String remittedTimestamp;
    @SerializedName("branch_id")
    private int branchId;
    private int driverId;
    @SerializedName("delivery_time")
    private String deliveryTime;
    private String photoPath;
    private String signaturePath;
    private int rating;
    private String amount;

    @SerializedName("payment_mode")
    private int paymentMode;

    public String getOrderOriginalConsigneeName() {
        return orderOriginalConsigneeName;
    }

    public void setOrderOriginalConsigneeName(String orderOriginalConsigneeName) {
        this.orderOriginalConsigneeName = orderOriginalConsigneeName;
    }

    public String getOrderConsigneeContactNumber() {
        return orderConsigneeContactNumber;
    }

    public void setOrderConsigneeContactNumber(String orderConsigneeContactNumber) {
        this.orderConsigneeContactNumber = orderConsigneeContactNumber;
    }

    public String getOrderConsigneeAddress() {
        return orderConsigneeAddress;
    }

    public void setOrderConsigneeAddress(String orderConsigneeAddress) {
        this.orderConsigneeAddress = orderConsigneeAddress;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderDeviceId() {
        return orderDeviceId;
    }

    public void setOrderDeviceId(int orderDeviceId) {
        this.orderDeviceId = orderDeviceId;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }

    public float getBranch_lat() {
        return branch_lat;
    }

    public void setBranch_lat(float branch_lat) {
        this.branch_lat = branch_lat;
    }

    public float getBranch_lng() {
        return branch_lng;
    }

    public void setBranch_lng(float branch_lng) {
        this.branch_lng = branch_lng;
    }

    public int getClient_id() {
        return client_id;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }


    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public String getDeliveredTimestamp() {
        return deliveredTimestamp;
    }

    public void setDeliveredTimestamp(String deliveredTimestamp) {
        this.deliveredTimestamp = deliveredTimestamp;
    }

    public String getStoreOutTimestamp() {
        return storeOutTimestamp;
    }

    public void setStoreOutTimestamp(String storeOutTimestamp) {
        this.storeOutTimestamp = storeOutTimestamp;
    }

    public String getStoreInTimestamp() {
        return storeInTimestamp;
    }

    public void setStoreInTimestamp(String storeInTimestamp) {
        this.storeInTimestamp = storeInTimestamp;
    }

    public String getVicinityInTimestamp() {
        return vicinityInTimestamp;
    }

    public void setVicinityInTimestamp(String vicinityInTimestamp) {
        this.vicinityInTimestamp = vicinityInTimestamp;
    }

    public String getRemittedTimestamp() {
        return remittedTimestamp;
    }

    public void setRemittedTimestamp(String remittedTimestamp) {
        this.remittedTimestamp = remittedTimestamp;
    }

    public String getServedTimestamp() {
        return servedTimestamp;
    }

    public void setServedTimestamp(String servedTimestamp) {
        this.servedTimestamp = servedTimestamp;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getSignaturePath() {
        return signaturePath;
    }

    public void setSignaturePath(String signaturePath) {
        this.signaturePath = signaturePath;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(int paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {

        this.statusId = statusId;
    }
}
