package mapabea.mapabea;

public class Constants {


//    public static final String API_URL = "http://riderapp.maxsgroupinc.com:8000/api/m/";
    public static final String API_URL = "http://210.14.16.68:8001/api/m/";

    public static final int OVERLAY_PERMISSION_REQ_CODE_CHATHEAD = 1234;
    public static final int REQUEST_PERMISSION_CODE = 501;

    public static final int CAMERA_CODE = 503;
    public static final String SHARED_PREF_NAME = "pota";
    public static final String USER_ID = "user_id";
    public static final String BRANCH_ID = "branch_id";
    public static final String DATABASE_NAME = "mapabea";
    public static final int DATABASE_VERSION = 4;


    public static final String COLUMN_ID = "id";

    public static final String TABLE_ORDERS = "orders";
    public static final String COLUMN_ORDER_NUMBER = "order_number";
    public static final String COLUMN_CONSIGNEE_ADDRESS = "consignee_address";
    public static final String COLUMN_CONSIGNEE_CONTACT_NUMBER = "consignee_contact_number";
    public static final String COLUMN_CONSIGNEE_NAME = "consignee_name";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LNG = "lng";
    public static final String COLUMN_CIENT_ID = "client_id";
    public static final String COLUMN_SENT = "shouldUpload";
    public static final String COLUMN_BRANCH_NAME = "branch_name";
    public static final String COLUMN_STORE_IN_TIMESTAMP = "storein_timestamp";
    public static final String COLUMN_STORE_OUT_TIMESTAMP = "storeout_timestamp";
    public static final String COLUMN_VICINITY_IN_TIMESTAMP = "vicinity_timestamp";
    public static final String COLUMN_SERVED_TIMESTAMP = "served_timestamp";
    public static final String COLUMN_REMITTED_TIMESTAMP = "remitted_timestamp";
    public static final String COLUMN_STORE_IN_TIMESTAMP_SENT = "storein_timestamp_sent";
    public static final String COLUMN_STORE_OUT_TIMESTAMP_SENT  = "storeout_timestamp_sent";
    public static final String COLUMN_VICINITY_IN_TIMESTAMP_SENT  = "vicinity_timestamp_sent";
    public static final String COLUMN_SERVED_TIMESTAMP_SENT  = "served_timestamp_sent";
    public static final String COLUMN_REMITTED_TIMESTAMP_SENT  = "remitted_timestamp_sent";
    public static final String COLUMN_SIGNATURE_PATH  = "signature_path";
    public static final String COLUMN_PHOTO_PATH  = "photo_path";
    public static final String COLUMN_RATING  = "rating";
    public static final String COLUMN_PAYMENT_MODE  = "payment_mode";
    public static final String COLUMN_AMOUNT  = "amount";
    public static final String COLUMN_BRANCH_LAT  = "branch_lat";
    public static final String COLUMN_BRANCH_LNG  = "branch_lng";
    public static final String COLUMN_CLOSED  = "closed";
    public static final String COLUMN_CLOSED_TIMESTAMP = "closed_timestamp";
    public static final String COLUMN_BRANCH_ID  = "branch_id";
    public static final String COLUMN_DELIVERY_TIME  = "delivery_time";


    public static final String KEY_SECONDS = "seconds";
    public static final String KEY_MINUTES = "minutes";

    public static final String TABLE_MESSAGES = "messages";

    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_MESSAGE_TYPE = "type";
    public static final String COLUMN_TIMESTAMP = "timestamp";





    public static final String COLUMN_ORDER_ID = "order_id";



    public static final String KEY_STATUS_ID = "status_id";
    public static final String KEY_DRIVER_ID = "driver_id";
    public static final String KEY_HUB_ID = "hub_od";
    public static final String KEY_ORDER_ID = "order_id";
    public static final String KEY_HISTORY_TYPE = "history_type";
    public static final String KEY_IS_DRIVER = "is_driver";
    public static final String KEY_STORED_IN = "stored_in";



    public static final int STORE_IN_COUNTDOWN = 600000;

    public static final String SHARED_PREF_FAILED_STRING = "SHARED_PREF_FAILED";
    public static final int SHARED_PREF_FAILED_INT = -69;

    enum ORDER_STATUS_CODE {
        ASSIGNED(1),
        STORE_IN(3),
        STORE_OUT(4),
        VICINITY_IN(5),
        SERVED(6),
        REMITTED(7);

        private final int value;
        private ORDER_STATUS_CODE(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    enum HISTORY_TYPE_CODE {
        CLOSED_TICKET(1),
        FOR_REMITTANCE(2),
        ARCHIVE_TICKETS(3);



        private final int value;
        private HISTORY_TYPE_CODE(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    enum MESSAGE_TYPE_CODE {
        SMS(1), CHAT(2);

        private final int value;
        private MESSAGE_TYPE_CODE(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }



}
