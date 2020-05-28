package mapabea.mapabea;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public abstract class BaseActivity extends AppCompatActivity {

    protected Gson gson;
    protected SharedPreferences sharedPreferences;
    protected SharedPreferences.Editor sharedPreferencesEditor;
    protected DatabaseManager databaseManager;
    protected SimpleDateFormat dateFormat;
    protected ToolBox toolBox;
    protected Timer assignedTicketCheckerScheduler = new Timer();
    protected Timer removedTicketCheckerScheduler = new Timer();

    protected String _imei;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialize();
    }

    @Override
    public void onBackPressed() {

    }


    public void superOnBackPressed() {
        super.onBackPressed();
    }

    protected abstract void initializeViews();

    protected abstract void initializeListeners();


    protected void initialize() {
        if (this instanceof ProofActivity) {
            setContentView(R.layout.receiver_layout);
        } else if (this instanceof Navigation) {
            setContentView(R.layout.navigation);
        } else if (this instanceof OrderHistoryActivity) {
            setContentView(R.layout.order_history_layout);
        } else if (this instanceof StatusActivity) {
            setContentView(R.layout.status_layout);
        } else if (this instanceof OrderListActivity) {
            setContentView(R.layout.orders_layout);
        } else if (this instanceof MessengerActivity) {
            setContentView(R.layout.messenger_layout);
        }
        initializeViews();
        initializeListeners();

        gson = new Gson();
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
        databaseManager = new DatabaseManager(this);
        dateFormat = new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss");
        toolBox = new ToolBox(this);


    }

    protected String getStringFromPref(String key) {
        return sharedPreferences.getString(key, Constants.SHARED_PREF_FAILED_STRING);
    }

    protected int getIntFromPref(String key) {

        return sharedPreferences.getInt(key, Constants.SHARED_PREF_FAILED_INT);

    }

    @SuppressLint("MissingPermission")
    protected void initDeviceId() {

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        _imei = telephonyManager.getDeviceId();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( assignedTicketCheckerScheduler != null ) {
            assignedTicketCheckerScheduler.cancel();
            assignedTicketCheckerScheduler = null;
        }

        if ( removedTicketCheckerScheduler != null ) {
            removedTicketCheckerScheduler.cancel();
            removedTicketCheckerScheduler = null;
        }

    }

    protected void showSavingOfflineMessage() {
        showMessage("Can't connect to server saving offline...");
    }

    protected void showMessage(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void onTransferredTicketEvicted(Integer[] ids) {
        databaseManager.deleteOrder(ids);

    }
    protected void onAssignedTicketFound(List<Order> orders) {




    }
    protected void startEvictingTransferredTicket() {

        if ( removedTicketCheckerScheduler == null ) {
            return;
        }

        removedTicketCheckerScheduler.schedule(new TimerTask() {
            @Override
            public void run() {
                removedTicketCheckerScheduler.cancel();
                removedTicketCheckerScheduler = new Timer();

                List<Order> assignedOrders = databaseManager.getAssignedOrders();

                String ids = "-1";

                for ( Order assignedOrder : assignedOrders ) {

                    ids += "," + assignedOrder.getId();
                }

                HttpConnection.doPost(new HttpConnection.Param[]{
                        new HttpConnection.Param("ids", ids),
                        new HttpConnection.Param("imei", _imei)
                }, Constants.API_URL + "trips/evicted_ticket", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        startEvictingTransferredTicket();

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        String responseBody = response.body().string();

//                        System.out.println(responseBody);


                        FirebaseCrashlytics.getInstance().log(responseBody);
                        FirebaseCrashlytics.getInstance().setUserId(_imei);

                        MResponse<Integer[]> responseObject = gson.fromJson(responseBody,
                                new TypeToken<MResponse<Integer[]>>() {
                                }.getType());

                        if ( responseObject.isError() ) {

                            startEvictingTransferredTicket();

                        } else {

                            Integer[] evictedOrderIds = responseObject.getResult();

                            if ( evictedOrderIds.length > 0 ) {

                                onTransferredTicketEvicted(responseObject.getResult());

                            } else {

                                startEvictingTransferredTicket();

                            }

                        }
                    }
                });
            }
        }, 3000);
    }



    protected void startCheckingForTransferTicket() {

        if ( assignedTicketCheckerScheduler == null ) {
            return;
        }

        assignedTicketCheckerScheduler.schedule(new TimerTask() {
            @Override
            public void run() {

                assignedTicketCheckerScheduler.cancel();
                assignedTicketCheckerScheduler = new Timer();

                List<Order> assignedOrders = databaseManager.getAssignedOrders();

                String ids = "-1";

                for ( Order assignedOrder : assignedOrders ) {

                    ids += "," + assignedOrder.getId();
                }

                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                @SuppressLint("MissingPermission") String imei = telephonyManager.getDeviceId();
                HttpConnection.doPost(new HttpConnection.Param[]{
                        new HttpConnection.Param("imei", imei),
                        new HttpConnection.Param("cached_order_ids", ids)

                }, Constants.API_URL + "trips/assigned_ticket", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        startCheckingForTransferTicket();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        String responseBody = response.body().string();
//                        System.out.println(responseBody);

                        FirebaseCrashlytics.getInstance().log(responseBody);
                        FirebaseCrashlytics.getInstance().setUserId(_imei);
                        MResponse<List<Order>> responseObject = gson.fromJson(responseBody, new TypeToken<MResponse<List<Order>>>() {}.getType());

                        if (responseObject.isError()) {

                            startCheckingForTransferTicket();

                        } else {

                            List<Order> newOrders = responseObject.getResult();

                            if (newOrders.size() > 0) {

                                onAssignedTicketFound(newOrders);

                            } else {

                                startCheckingForTransferTicket();

                            }
                        }

                    }
                });
            }

        }, 3000);

    }
}
