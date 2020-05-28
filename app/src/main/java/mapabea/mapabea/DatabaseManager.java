package mapabea.mapabea;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager extends SQLiteOpenHelper {


    public DatabaseManager(@Nullable Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
        getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("CREATE TABLE " + Constants.TABLE_ORDERS + "(" +
                Constants.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                Constants.COLUMN_ORDER_NUMBER + " TEXT, " +
                Constants.COLUMN_CONSIGNEE_ADDRESS + " TEXT, " +
                Constants.COLUMN_CONSIGNEE_CONTACT_NUMBER + " TEXT, " +
                Constants.COLUMN_CONSIGNEE_NAME + " TEXT," +
                Constants.COLUMN_LAT + " REAL," +
                Constants.COLUMN_LNG + " REAL," +
                Constants.COLUMN_CIENT_ID + " INTEGER," +
                Constants.COLUMN_BRANCH_NAME + " TEXT," +
                Constants.COLUMN_STATUS + " INTEGER," +
                Constants.KEY_DRIVER_ID + " INTEGER," +
                Constants.COLUMN_STORE_IN_TIMESTAMP + " TEXT default NULL," +
                Constants.COLUMN_STORE_OUT_TIMESTAMP + " TEXT default NULL," +
                Constants.COLUMN_VICINITY_IN_TIMESTAMP + " TEXT default NULL," +
                Constants.COLUMN_SERVED_TIMESTAMP + " TEXT default NULL," +
                Constants.COLUMN_REMITTED_TIMESTAMP + " TEXT default NULL," +
                Constants.COLUMN_STORE_IN_TIMESTAMP_SENT  + " INTEGER DEFAULT 0," +
                Constants.COLUMN_STORE_OUT_TIMESTAMP_SENT  + " INTEGER DEFAULT 0," +
                Constants.COLUMN_VICINITY_IN_TIMESTAMP_SENT  + " INTEGER DEFAULT 0," +
                Constants.COLUMN_SERVED_TIMESTAMP_SENT  + " INTEGER DEFAULT 0," +
                Constants.COLUMN_REMITTED_TIMESTAMP_SENT  + " INTEGER DEFAULT 0," +
                Constants.COLUMN_SIGNATURE_PATH  + " TEXT," +
                Constants.COLUMN_PHOTO_PATH  + " TEXT," +
                Constants.COLUMN_RATING  + " INTEGER DEFAULT 0," +
                Constants.COLUMN_PAYMENT_MODE  + " INTEGER," +
                Constants.COLUMN_AMOUNT  + " TEXT," +
                Constants.COLUMN_BRANCH_LAT  + " REAL," +
                Constants.COLUMN_BRANCH_LNG  + " REAL," +
                Constants.COLUMN_BRANCH_ID + " INTEGER," +
                Constants.COLUMN_CLOSED + " INTEGER DEFAULT 0," +
                Constants.COLUMN_DELIVERY_TIME + " TEXT," +
                Constants.COLUMN_CLOSED_TIMESTAMP + " TEXT)");

        sqLiteDatabase.execSQL("CREATE TABLE " + Constants.TABLE_MESSAGES + "(" +
                Constants.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                Constants.COLUMN_MESSAGE + " TEXT, " +
                Constants.COLUMN_MESSAGE_TYPE + " INTEGER, " +
                Constants.COLUMN_TIMESTAMP + " TEXT)");



    }
    public List<Message> getMessages(Constants.MESSAGE_TYPE_CODE messageTypeCode) {


        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        int messageType = Constants.MESSAGE_TYPE_CODE.SMS.getValue();
        if ( messageTypeCode == Constants.MESSAGE_TYPE_CODE.CHAT ) {
            messageType = Constants.MESSAGE_TYPE_CODE.CHAT.getValue();
        }

        String query = "SELECT * FROM " + Constants.TABLE_MESSAGES + " WHERE "
                + Constants.COLUMN_MESSAGE_TYPE + " = " + messageType + " ORDER BY " + Constants.COLUMN_TIMESTAMP + " DESC";

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        List<Message> messages = new ArrayList<>();
        if ( cursor.moveToFirst() ) {
            do {
                Message message = new Message();

                message.setMessage(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_MESSAGE)));
                message.setType(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_MESSAGE_TYPE)));
                message.setTimestamp(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_TIMESTAMP)));
            } while(cursor.moveToNext());
        }


        return messages;

    }


    public void deleteOrders() {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "DELETE from " + Constants.TABLE_ORDERS + " where " + Constants.COLUMN_CLOSED + " = 1 and date(" + Constants.COLUMN_CLOSED_TIMESTAMP + ", '+1 day') = DATE('now')";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_ORDERS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_MESSAGES);
    }

    public void updateForDelivered(int orderId, String photoPath, String sigPath, int rating) {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "UPDATE " + Constants.TABLE_ORDERS + " SET "
                + Constants.COLUMN_SIGNATURE_PATH + " = '" + sigPath + "', "
                + Constants.COLUMN_PHOTO_PATH + " = '" + photoPath + "', "
                + Constants.COLUMN_RATING + " = " + rating + " WHERE " + Constants.COLUMN_ID + " = " + orderId;

        sqLiteDatabase.execSQL(query);
    }

    public void updateStatusOrder(int orderId, Constants.ORDER_STATUS_CODE status, boolean offline) {
        updateStatusOrder(Integer.toString(orderId), status, offline);
    }

    public void updateStatusOrder(String orderId, Constants.ORDER_STATUS_CODE status, boolean offline) {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        String columnTimestamp = "";
        int sent = 1;

        if (status == Constants.ORDER_STATUS_CODE.STORE_IN) {
            columnTimestamp = Constants.COLUMN_STORE_IN_TIMESTAMP;

        } else if (status == Constants.ORDER_STATUS_CODE.STORE_OUT) {
            columnTimestamp = Constants.COLUMN_STORE_OUT_TIMESTAMP;

        } else if (status == Constants.ORDER_STATUS_CODE.VICINITY_IN) {
            columnTimestamp = Constants.COLUMN_VICINITY_IN_TIMESTAMP;

        } else if (status == Constants.ORDER_STATUS_CODE.SERVED) {
            columnTimestamp = Constants.COLUMN_SERVED_TIMESTAMP;

        } else if (status == Constants.ORDER_STATUS_CODE.REMITTED) {
            columnTimestamp = Constants.COLUMN_REMITTED_TIMESTAMP;

        }

        if (offline) {
            sent = 0;
        }


        sqLiteDatabase.execSQL("UPDATE " + Constants.TABLE_ORDERS + " set " + Constants.COLUMN_STATUS + " = '" + status.name() + "', " + columnTimestamp + " = '" + Utils.getStringifyCurrentDate() + "'," +
                columnTimestamp + "_sent = " + sent +  " where " + Constants.COLUMN_ID + " = " + orderId);

        if ( status == Constants.ORDER_STATUS_CODE.REMITTED ) {
            sqLiteDatabase.execSQL("UPDATE " + Constants.TABLE_ORDERS + " SET " + Constants.COLUMN_CLOSED + " = 1, " + Constants.COLUMN_CLOSED_TIMESTAMP + " = '" + Utils.getStringifyCurrentDate() + "' WHERE " + Constants.COLUMN_ID + " = " + orderId);

        } else if ( status == Constants.ORDER_STATUS_CODE.SERVED ) {
            sqLiteDatabase.execSQL("UPDATE " + Constants.TABLE_ORDERS + " SET " + Constants.COLUMN_CLOSED + " = 1, " + Constants.COLUMN_CLOSED_TIMESTAMP + " = '" + Utils.getStringifyCurrentDate() + "' WHERE " + Constants.COLUMN_ID + " = " + orderId
                    + " AND " + Constants.COLUMN_PAYMENT_MODE + " != 1"
                    + " AND " + Constants.COLUMN_PAYMENT_MODE + " != 2");
        }
    }

    public boolean insertOrder(Order order) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(Constants.COLUMN_ID, order.getId());
        cv.put(Constants.COLUMN_ORDER_NUMBER, order.getOrderNumber());
        cv.put(Constants.COLUMN_CONSIGNEE_ADDRESS, order.getOrderConsigneeAddress());
        cv.put(Constants.COLUMN_CONSIGNEE_CONTACT_NUMBER, order.getOrderConsigneeContactNumber());
        cv.put(Constants.COLUMN_CONSIGNEE_NAME, order.getOrderOriginalConsigneeName());
        cv.put(Constants.COLUMN_STATUS, order.getStatusId());
        cv.put(Constants.COLUMN_LAT, order.getLat());
        cv.put(Constants.COLUMN_LNG, order.getLng());
        cv.put(Constants.COLUMN_CIENT_ID, order.getClient_id());
        cv.put(Constants.COLUMN_BRANCH_NAME, order.getBranchName());
        cv.put(Constants.KEY_DRIVER_ID, order.getDriverId());
        cv.put(Constants.COLUMN_PAYMENT_MODE, order.getPaymentMode());
        cv.put(Constants.COLUMN_AMOUNT, order.getAmount());
        cv.put(Constants.COLUMN_BRANCH_LAT, order.getBranch_lat());
        cv.put(Constants.COLUMN_BRANCH_LNG, order.getBranch_lng());
        cv.put(Constants.COLUMN_DELIVERY_TIME, order.getDeliveryTime());
        cv.put(Constants.COLUMN_BRANCH_ID, order.getBranchId());
        if ( order.getStoreInTimestamp() != null ) {
            cv.put(Constants.COLUMN_STORE_IN_TIMESTAMP, order.getStoreInTimestamp());
            cv.put(Constants.COLUMN_STORE_IN_TIMESTAMP_SENT, 1);
        }
        if ( order.getStoreOutTimestamp() != null ) {
            cv.put(Constants.COLUMN_STORE_OUT_TIMESTAMP, order.getStoreOutTimestamp());
            cv.put(Constants.COLUMN_STORE_OUT_TIMESTAMP_SENT, 1);
        }
        if ( order.getVicinityInTimestamp() != null ) {
            cv.put(Constants.COLUMN_VICINITY_IN_TIMESTAMP, order.getVicinityInTimestamp());
            cv.put(Constants.COLUMN_VICINITY_IN_TIMESTAMP_SENT, 1);
        }
        if ( order.getServedTimestamp() != null ) {
            cv.put(Constants.COLUMN_SERVED_TIMESTAMP, order.getServedTimestamp());
            cv.put(Constants.COLUMN_SERVED_TIMESTAMP_SENT, 1);
        }
        if ( order.getRemittedTimestamp() != null ) {
            cv.put(Constants.COLUMN_REMITTED_TIMESTAMP, order.getRemittedTimestamp());
            cv.put(Constants.COLUMN_REMITTED_TIMESTAMP_SENT, 1);
        }
        long res = -1;

        try {
            res = sqLiteDatabase.insertOrThrow(Constants.TABLE_ORDERS, null, cv);
        } catch(SQLiteConstraintException exception) {
            exception.printStackTrace();
        }

        return res != -1;
    }

    public void insertOrders(List<Order> orders, int driverId, Constants.ORDER_STATUS_CODE orderStatusCode) {
        for ( Order order : orders ) {

            order.setStatusId(orderStatusCode.getValue());
            order.setDriverId(driverId);
            insertOrder(order);
        }
    }
    public void insertOrders(List<Order> orders, int driverId) {
        for ( Order order : orders ) {

            System.out.println("ORDER FOUND ASSIGNED: " + order.getId());
            order.setDriverId(driverId);
            insertOrder(order);
        }
    }

    public void updateSent(int orderId, Constants.ORDER_STATUS_CODE status) {


        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        String timestampSent = "";

        if ( status == Constants.ORDER_STATUS_CODE.STORE_IN ) {
            timestampSent = Constants.COLUMN_STORE_IN_TIMESTAMP_SENT;

        } else if ( status == Constants.ORDER_STATUS_CODE.STORE_OUT ) {
            timestampSent = Constants.COLUMN_STORE_OUT_TIMESTAMP_SENT;

        } else if ( status == Constants.ORDER_STATUS_CODE.VICINITY_IN ) {
            timestampSent = Constants.COLUMN_VICINITY_IN_TIMESTAMP_SENT;

        } else if ( status == Constants.ORDER_STATUS_CODE.SERVED ) {
            timestampSent = Constants.COLUMN_SERVED_TIMESTAMP_SENT;

        } else if ( status == Constants.ORDER_STATUS_CODE.REMITTED ) {
            timestampSent = Constants.COLUMN_REMITTED_TIMESTAMP_SENT;

        }

        String query = "UPDATE " + Constants.TABLE_ORDERS + " SET " + timestampSent + " = 1 WHERE " + Constants.COLUMN_ID + " = " + orderId;

        sqLiteDatabase.execSQL(query);

    }

    public Order getLatestClosedOrder() {

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        return null;

    }

    public void deleteOrder(int orderId) {


        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "DELETE from " + Constants.TABLE_ORDERS + " where " + Constants.COLUMN_ID + " = " + orderId;
        sqLiteDatabase.execSQL(query);
//        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
//        sqLiteDatabase.delete(Constants.TABLE_ORDERS, Constants.COLUMN_ID + "=? and "
//                        + Constants.COLUMN_STORE_IN_TIMESTAMP_SENT + "=1 and "
//                        + Constants.COLUMN_STORE_OUT_TIMESTAMP_SENT + "=1 and "
//                        + Constants.COLUMN_VICINITY_IN_TIMESTAMP_SENT + "=1 and "
//                        + Constants.COLUMN_SERVED_TIMESTAMP_SENT + "=1 and "
//                        + Constants.COLUMN_REMITTED_TIMESTAMP_SENT + "=1", new String[]{Integer.toString(orderId)});

    }

    public List<Order> getPendingOrders() {

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

//        String query = "SELECT * FROM " + Constants.TABLE_ORDERS + " WHERE "
//                + Constants.COLUMN_STORE_IN_TIMESTAMP_SENT + "=0 or "
//                + Constants.COLUMN_STORE_OUT_TIMESTAMP_SENT + "=0 or "
//                + Constants.COLUMN_VICINITY_IN_TIMESTAMP_SENT + "=0 or "
//                + Constants.COLUMN_SERVED_TIMESTAMP_SENT + "=0 or ("
//                + Constants.COLUMN_REMITTED_TIMESTAMP_SENT + "=0 AND "
//                + Constants.COLUMN_PAYMENT_MODE + " = 1)";
        String query = "SELECT * FROM " + Constants.TABLE_ORDERS + " WHERE "
                + Constants.COLUMN_STORE_IN_TIMESTAMP_SENT + "=0 or "
                + Constants.COLUMN_STORE_OUT_TIMESTAMP_SENT + "=0 or "
                + Constants.COLUMN_VICINITY_IN_TIMESTAMP_SENT + "=0 or "
                + Constants.COLUMN_SERVED_TIMESTAMP_SENT + "=0 or ("
                + Constants.COLUMN_REMITTED_TIMESTAMP_SENT + "=0 AND "
                + Constants.COLUMN_PAYMENT_MODE + " = 1)";

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        List<Order> orders = new ArrayList<>();
        if ( cursor.moveToFirst() ) {

            do {

                String storeInTimestamp = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_STORE_IN_TIMESTAMP));
                String storeOutTimestamp = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_STORE_OUT_TIMESTAMP));
                String vicinityInTimestamp = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_VICINITY_IN_TIMESTAMP));
                String servedTimestamp = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_SERVED_TIMESTAMP));
                String remitTimestamp = cursor.getString(cursor.getColumnIndex(Constants.COLUMN_REMITTED_TIMESTAMP));
                int storeInTimestampSent = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_STORE_IN_TIMESTAMP_SENT));
                int storeOutTimestampSent = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_STORE_OUT_TIMESTAMP_SENT));
                int vicinityInTimestampSent = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_VICINITY_IN_TIMESTAMP_SENT));
                int servedTimestampSent = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_SERVED_TIMESTAMP_SENT));
                int remitTimestampSent = cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_REMITTED_TIMESTAMP_SENT));

                System.out.println(storeInTimestamp);
                System.out.println(storeInTimestampSent);
                if ( storeInTimestamp != null && storeInTimestampSent == 0 ) {
                    System.out.println("GOT STOREIN");
                    Order order = new Order();
                    order.setId(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_ID)));
                    order.setStoreInTimestamp(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_STORE_IN_TIMESTAMP)));
                    order.setStatusId(Constants.ORDER_STATUS_CODE.STORE_IN.getValue());
                    order.setDriverId(cursor.getInt(cursor.getColumnIndex(Constants.KEY_DRIVER_ID)));
                    orders.add(order);
                }
                if ( storeOutTimestamp != null && storeOutTimestampSent == 0 ) {
                    System.out.println("GOT STOREOUT");
                    Order order = new Order();
                    order.setId(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_ID)));
                    order.setStoreOutTimestamp(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_STORE_OUT_TIMESTAMP)));
                    order.setStatusId(Constants.ORDER_STATUS_CODE.STORE_OUT.getValue());
                    order.setDriverId(cursor.getInt(cursor.getColumnIndex(Constants.KEY_DRIVER_ID)));
                    orders.add(order);
                }

                if ( vicinityInTimestamp != null && vicinityInTimestampSent == 0 ) {

                    System.out.println("GOT VICINITY");
                    Order order = new Order();
                    order.setId(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_ID)));
                    order.setVicinityInTimestamp(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_VICINITY_IN_TIMESTAMP)));
                    order.setStatusId(Constants.ORDER_STATUS_CODE.VICINITY_IN.getValue());
                    order.setDriverId(cursor.getInt(cursor.getColumnIndex(Constants.KEY_DRIVER_ID)));
                    orders.add(order);
                }
                if ( servedTimestamp != null && servedTimestampSent == 0 ) {
                    System.out.println("GOT SERVED");
                    Order order = new Order();
                    order.setId(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_ID)));
                    order.setServedTimestamp(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_SERVED_TIMESTAMP)));
                    order.setStatusId(Constants.ORDER_STATUS_CODE.SERVED.getValue());
                    order.setDriverId(cursor.getInt(cursor.getColumnIndex(Constants.KEY_DRIVER_ID)));
                    order.setPhotoPath(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_PHOTO_PATH)));
                    order.setSignaturePath(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_SIGNATURE_PATH)));
                    order.setRating(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_RATING)));
                    orders.add(order);
                }
                if ( remitTimestamp != null && remitTimestampSent == 0 ) {

                    System.out.println("GOT REMIT");
                    Order order = new Order();
                    order.setId(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_ID)));
                    order.setRemittedTimestamp(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_REMITTED_TIMESTAMP)));
                    order.setStatusId(Constants.ORDER_STATUS_CODE.REMITTED.getValue());
                    order.setDriverId(cursor.getInt(cursor.getColumnIndex(Constants.KEY_DRIVER_ID)));
                    orders.add(order);
                }




            } while(cursor.moveToNext());

        }

        return orders;
    }

    public Integer getOrderStatus(int orderId) {

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        String query = "SELECT * FROM " + Constants.TABLE_ORDERS + " WHERE " + Constants.COLUMN_ID + " = ?";

        Cursor cursor = sqLiteDatabase.rawQuery(query, new String[]{ String.valueOf(orderId) });


        if ( cursor.moveToFirst() ) {

            return cursor.getInt(9);
        }

        return null;

    }

    public List<Order> getAssignedOrders() {

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();



        List<Order> orders = new ArrayList<>();


        String query = "SELECT * FROM " + Constants.TABLE_ORDERS + " WHERE "
                + Constants.COLUMN_STORE_IN_TIMESTAMP + " IS NULL or "
                + Constants.COLUMN_STORE_OUT_TIMESTAMP + " IS NULL or " /// to be revised
                + Constants.COLUMN_VICINITY_IN_TIMESTAMP+ " IS NULL or "
                + Constants.COLUMN_SERVED_TIMESTAMP + " IS NULL";


        Cursor cursor = sqLiteDatabase.rawQuery(query, null);


        System.out.println("CURSOR SIZE: " + cursor.getCount());
        if ( cursor.moveToFirst() ) {
            do {

                Order order = new Order();

                order.setId(cursor.getInt(0));
                order.setOrderNumber(cursor.getString(1));

                order.setOrderConsigneeAddress(cursor.getString(2));
                order.setOrderConsigneeContactNumber(cursor.getString(3));
                order.setOrderOriginalConsigneeName(cursor.getString(4));
                order.setLat(cursor.getFloat(5));
                order.setLng(cursor.getFloat(6));
                order.setClient_id(cursor.getInt(7));
                order.setBranchName(cursor.getString(8));
                order.setBranchId(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_BRANCH_ID)));
                order.setStatusId(cursor.getInt(9));
                order.setDriverId(cursor.getInt(10));

                order.setStoreInTimestamp(cursor.getString(11));
                order.setStoreOutTimestamp(cursor.getString(12));
                order.setVicinityInTimestamp(cursor.getString(13));
                order.setDeliveredTimestamp(cursor.getString(14));
                order.setRemittedTimestamp(cursor.getString(15));
                order.setStoreInTimestamp(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_STORE_IN_TIMESTAMP)));
                order.setBranch_lat(cursor.getFloat(cursor.getColumnIndex(Constants.COLUMN_BRANCH_LAT)));
                order.setBranch_lng(cursor.getFloat(cursor.getColumnIndex(Constants.COLUMN_BRANCH_LNG)));

                order.setDeliveryTime(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DELIVERY_TIME)));
                order.setPaymentMode(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PAYMENT_MODE)));
                order.setAmount(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_AMOUNT)));
                orders.add(order);
            } while(cursor.moveToNext());

        }

        return orders;
    }

    public int getPendingRemittanceCount() {

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        String query = "SELECT * FROM " + Constants.TABLE_ORDERS + " WHERE " +
                Constants.COLUMN_PAYMENT_MODE + " = 1 AND " +
                Constants.COLUMN_SERVED_TIMESTAMP + " IS NOT NULL AND " +
                Constants.COLUMN_REMITTED_TIMESTAMP + " IS NULL ";

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        return cursor.getCount();

    }

    public List<Order> getHistoryOrders() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        List<Order> orders = new ArrayList<>();


        String query = "SELECT * FROM " + Constants.TABLE_ORDERS + " WHERE "
                + Constants.COLUMN_CLOSED + " = 1";

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        System.out.println("cusror count: " + cursor.getCount());

        if ( cursor.moveToFirst() ) {
            do {

                Order order = new Order();

                order.setId(cursor.getInt(0));
                order.setOrderNumber(cursor.getString(1));

                order.setStoreInTimestamp(cursor.getString(11));
                order.setStoreOutTimestamp(cursor.getString(12));
                order.setVicinityInTimestamp(cursor.getString(13));
                order.setServedTimestamp(cursor.getString(14));
                order.setRemittedTimestamp(cursor.getString(15));

                orders.add(order);
            } while(cursor.moveToNext());

        }

        return orders;
    }

    public List<Order> getForRemittanceOrders() {

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        List<Order> orders = new ArrayList<>();

        String query = "SELECT * FROM " + Constants.TABLE_ORDERS + " WHERE (" +
                Constants.COLUMN_PAYMENT_MODE + " = 1 AND " +
                Constants.COLUMN_SERVED_TIMESTAMP + " IS NOT NULL AND " +
                Constants.COLUMN_REMITTED_TIMESTAMP + " IS NULL) or (" +
                Constants.COLUMN_PAYMENT_MODE + " = 2 AND " +
                Constants.COLUMN_SERVED_TIMESTAMP + " IS NOT NULL" +
                //Constants.COLUMN_CLOSED + " = 0 " +
                ") ORDER BY " + Constants.COLUMN_ID + " ASC";

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);


        if ( cursor.moveToFirst() ) {
            do {

                Order order = new Order();

                order.setId(cursor.getInt(0));
                order.setOrderNumber(cursor.getString(1));

                order.setStoreInTimestamp(cursor.getString(11));
                order.setStoreOutTimestamp(cursor.getString(12));
                order.setVicinityInTimestamp(cursor.getString(13));
                order.setServedTimestamp(cursor.getString(14));
                order.setRemittedTimestamp(cursor.getString(15));

                order.setPaymentMode(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PAYMENT_MODE)));

                order.setBranchId(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_BRANCH_ID)));
                order.setBranchName(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_BRANCH_NAME)));
                orders.add(order);
            } while(cursor.moveToNext());

        }

        return orders;
    }

    public void deleteOrder(Integer[] ids) {
        for ( Integer id : ids ) {
            deleteOrder(id);
        }

    }
    public Order getOrder(int orderId) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        String query = "SELECT * FROM " + Constants.TABLE_ORDERS + " WHERE " + Constants.COLUMN_ID + " = ?";

        Cursor cursor = sqLiteDatabase.rawQuery(query, new String[]{ Integer.toString(orderId) });
        Order order = null;
        if ( cursor.moveToFirst() ) {
            order = new Order();
            order.setId(cursor.getInt(0));
            order.setOrderNumber(cursor.getString(1));
            order.setOrderConsigneeAddress(cursor.getString(2));
            order.setOrderConsigneeContactNumber(cursor.getString(3));
            order.setOrderOriginalConsigneeName(cursor.getString(4));
            order.setLat(cursor.getFloat(5));
            order.setLng(cursor.getFloat(6));
            order.setClient_id(cursor.getInt(7));
            order.setBranchName(cursor.getString(8));
            order.setStatusId(cursor.getInt(9));
            order.setDriverId(cursor.getInt(10));

            order.setStoreInTimestamp(cursor.getString(11));
            order.setStoreOutTimestamp(cursor.getString(12));
            order.setVicinityInTimestamp(cursor.getString(13));
            order.setDeliveredTimestamp(cursor.getString(14));
            order.setRemittedTimestamp(cursor.getString(15));
            order.setPaymentMode(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_PAYMENT_MODE)));
            order.setAmount(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_AMOUNT)));
            order.setBranch_lat(cursor.getFloat(cursor.getColumnIndex(Constants.COLUMN_BRANCH_LAT)));
            order.setBranch_lng(cursor.getFloat(cursor.getColumnIndex(Constants.COLUMN_BRANCH_LNG)));
            order.setDeliveryTime(cursor.getString(cursor.getColumnIndex(Constants.COLUMN_DELIVERY_TIME)));
            order.setBranchId(cursor.getInt(cursor.getColumnIndex(Constants.COLUMN_BRANCH_ID)));


        }

        return order;

    }
}
