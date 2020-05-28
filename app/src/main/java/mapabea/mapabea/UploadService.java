package mapabea.mapabea;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.IBinder;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadService extends Service {

    private DatabaseManager databaseManager;

    private Gson gson;

    private List<Order> pendingOrders;

    private Order nextOrder;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.println("onStartCommand");

        databaseManager = new DatabaseManager(getApplicationContext());

        gson = new Gson();

        startUploading();

        return START_STICKY;
    }

    void startUploading() {

        pendingOrders = databaseManager.getPendingOrders();

        System.out.println("pending orders size: " + pendingOrders.size());

        uploadNext();

    }

    void uploadNext() {

        if ( pendingOrders.size() > 0 ) {


            nextOrder = pendingOrders.get(0);

            int statusId = nextOrder.getStatusId();

            if ( statusId == Constants.ORDER_STATUS_CODE.STORE_IN.getValue() ) {
                upload(Constants.ORDER_STATUS_CODE.STORE_IN,"trip/store_in", nextOrder.getStoreInTimestamp());

            } else if ( statusId == Constants.ORDER_STATUS_CODE.STORE_OUT.getValue() ) {
                upload(Constants.ORDER_STATUS_CODE.STORE_OUT, "trip/rider_out", nextOrder.getStoreOutTimestamp());

            } else if ( statusId == Constants.ORDER_STATUS_CODE.VICINITY_IN.getValue() ) {
                upload(Constants.ORDER_STATUS_CODE.VICINITY_IN, "trip/vicinity_in", nextOrder.getVicinityInTimestamp());

            } else if ( statusId == Constants.ORDER_STATUS_CODE.SERVED.getValue() ) {
                uploadComplete();

            }
            else if ( statusId == Constants.ORDER_STATUS_CODE.REMITTED.getValue() ) {
                upload(Constants.ORDER_STATUS_CODE.REMITTED, "trip/remit", nextOrder.getRemittedTimestamp());

            }

        } else {

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    timer.cancel();
                    startUploading();
                }
            }, 2000);


        }

    }

    public void uploadComplete() {

        final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

        HttpConnection.Param[] params = new HttpConnection.Param[6];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap photo = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+"/riderAppFiles/"+nextOrder.getPhotoPath(), options);
        Bitmap sig = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+"/riderAppFiles/"+nextOrder.getSignaturePath(), options);

        params[0] = new HttpConnection.Param("trip_actual_end_time", nextOrder.getServedTimestamp());
        params[1] = new HttpConnection.Param("photo", nextOrder.getPhotoPath(), RequestBody.create(MEDIA_TYPE_PNG, Utils.bitmapToRaw(photo)));
        params[2] = new HttpConnection.Param("signature", nextOrder.getSignaturePath(),  RequestBody.create(MEDIA_TYPE_PNG, Utils.bitmapToRaw(sig)));
        params[3] = new HttpConnection.Param("trip_id", nextOrder.getId());
        params[4] = new HttpConnection.Param("rating", nextOrder.getRating());
        params[5] = new HttpConnection.Param("driver_id", nextOrder.getDriverId());


        HttpConnection.doPost(params, Constants.API_URL + "trip/complete", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        timer.cancel();
                        startUploading();
                    }
                }, 2000);

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();

                System.out.println(responseBody);
                MResponse responseObject = gson.fromJson(responseBody, new TypeToken<MResponse>(){}.getType());

                if ( !responseObject.isError() ) {

                    pendingOrders.remove(nextOrder);

                    databaseManager.updateSent(nextOrder.getId(), Constants.ORDER_STATUS_CODE.SERVED);
//                    databaseManager.deleteOrder(nextOrder.getId());

                    Timer timer = new Timer();

                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            timer.cancel();

                            uploadNext();


                        }
                    }, 3000);


                }
            }
        });
    }

    public void upload(Constants.ORDER_STATUS_CODE statusCode, String statusUrl, String timestamp) {

        System.out.println(Constants.API_URL + statusUrl);
        HttpConnection.doPost(new HttpConnection.Param[]{

                new HttpConnection.Param("trip_id", nextOrder.getId()),
                new HttpConnection.Param("timestamp", timestamp),
                new HttpConnection.Param("driver_id", nextOrder.getDriverId())

        }, Constants.API_URL + statusUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        timer.cancel();
                        startUploading();
                    }
                }, 2000);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseBody = response.body().string();

                FirebaseCrashlytics.getInstance().log(responseBody);
                FirebaseCrashlytics.getInstance().setUserId(StatusActivity.static_imei);

                System.out.println(responseBody);
                MResponse responseObject = gson.fromJson(responseBody,
                        new TypeToken<MResponse>(){}.getType());


                if ( !responseObject.isError() ) {

                    pendingOrders.remove(nextOrder);

                    databaseManager.updateSent(nextOrder.getId(), statusCode);
//                    databaseManager.deleteOrder(nextOrder.getId());

                    Timer timer = new Timer();

                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            timer.cancel();

                            uploadNext();


                        }
                    }, 3000);

                }

            }
        });
    }

}
