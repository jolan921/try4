package mapabea.mapabea;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class OrderListActivity extends BaseActivity {

    private RecyclerView orderRecyclerView;
    private OrderListAdapter orderListAdapter;
    private TextView countdownTimerTextView;
    private TextView distanceTextView;
    private Button storeInButton;
    private List<Order> assignedOrders;
    private LocationManager lm;
    private Ringtone ringtone;
    private NavigationView navigationView;

    private CountDownTimer countDownTimer;
    @Override
    protected void initializeViews() {

        navigationView = (NavigationView)findViewById(R.id.navigationView);
        orderRecyclerView = findViewById(R.id.orderRecyclerView);
        countdownTimerTextView = findViewById(R.id.countdownTimerTextView);
        storeInButton = findViewById(R.id.storeInButton);
        distanceTextView = findViewById(R.id.distanceTextView);

    }

    @Override
    protected void initializeListeners() {
        navigationView.setNavigationItemSelectedListener(navigationViewOnNavigationItemSelectedListener);
        storeInButton.setOnClickListener(storeInButtonOnClickListener);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    int retry = 0;
    void checkForStoreVicinityIntervally() {

        Timer timer = new Timer();


        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                timer.cancel();

                float branchLat = assignedOrders.get(0).getBranch_lat();
                float branchLng = assignedOrders.get(0).getBranch_lng();

                System.out.println(branchLat);
                System.out.println(branchLng);
                System.out.println(MainActivity.LNG);
                System.out.println(MainActivity.LAT);
                double meters = Utils.distance(MainActivity.LAT, branchLat, MainActivity.LNG, branchLng);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        boolean storedIn = getIntFromPref(Constants.KEY_STORED_IN) == 9;


                        if ( !storedIn ) {
                            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                distanceTextView.setText("gps not enabled");
                                storeInButton.setEnabled(true);

                                retry ++;
                            } else {

                                if ((MainActivity.LAT == 0 || MainActivity.LNG == 0)) {
                                    distanceTextView.setText("gps not working");
                                    storeInButton.setEnabled(true);
                                    retry ++;
                                } else {
                                    distanceTextView.setText(Double.toString(meters));
                                    storeInButton.setEnabled(false);
                                    retry = 0;
                                }
                            }
                        }


                    }
                });

                if ( retry == 3 ) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            storeInButton.setEnabled(true);
                        }
                    });
                    if ( countDownTimer != null ) {
                        countDownTimer.cancel();
                    }

                }

                else if ( meters <= 200 ) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            storeInButtonOnClickListener.onClick(storeInButton);
                        }
                    });
                    if ( countDownTimer != null ) {
                        countDownTimer.cancel();
                    }

                } else {

                    checkForStoreVicinityIntervally();

                }
            }

        }, 5000);
    }

    @Override
    protected void onTransferredTicketEvicted(Integer[] ids) {
        super.onTransferredTicketEvicted(ids);

        finish();
        startActivity(getIntent());

    }

    @Override
    protected void onAssignedTicketFound(List<Order> orders) {
        super.onAssignedTicketFound(orders);

        databaseManager.insertOrders(orders, getIntFromPref(Constants.KEY_DRIVER_ID));

        assignedOrders = databaseManager.getAssignedOrders();

        finish();

        startActivity(getIntent());

    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if ( countDownTimer != null ) {
            countDownTimer.cancel();
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        assignedOrders = databaseManager.getAssignedOrders();

        if ( assignedOrders.size() <= 0 ) {

            Intent intent = new Intent(this, StatusActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

            return;
        }



        initDeviceId();
        startEvictingTransferredTicket();
        if ( assignedOrders.size() < 2 ) {
            startCheckingForTransferTicket();
        }

        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        try {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
            ringtone.play();

            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC), 0);

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    timer.cancel();
                    ringtone.stop();
                }
            }, 60000);
        } catch(Exception e) {
            e.printStackTrace();
        }

//        int minutes = 0;
//        int seconds = 0;
        if ( getIntFromPref(Constants.KEY_STORED_IN) == Constants.SHARED_PREF_FAILED_INT ) {

//            minutes = getIntFromPref(Constants.KEY_MINUTES);
//            seconds = getIntFromPref(Constants.KEY_SECONDS);
//            if ( seconds != Constants.SHARED_PREF_FAILED_INT && minutes != Constants.SHARED_PREF_FAILED_INT ) {
//
//                countdownTimerTextView.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
//            } else {
//                sharedPreferencesEditor.putInt(Constants.KEY_MINUTES, 15);
//                sharedPreferencesEditor.putInt(Constants.KEY_SECONDS, 0);
//                sharedPreferencesEditor.apply();
//            }

            countdownTimerTextView.setVisibility(View.VISIBLE);
        } else {

            countdownTimerTextView.setVisibility(View.GONE);
            storeInButton.setEnabled(false);
        }

        if ( assignedOrders.size() > 0 ) {
            checkForStoreVicinityIntervally();
        }
        orderListAdapter = new OrderListAdapter(this, assignedOrders);

        orderRecyclerView.setLayoutManager(new LinearLayoutManager(OrderListActivity.this));
        orderRecyclerView.setAdapter(orderListAdapter);

        orderListAdapter.setOnClickeditem(new OrderListAdapter.OnClickedItem() {
            @Override
            public void onClickItem(int orderId) {
                Order order = databaseManager.getOrder(orderId);
                if ( order.getStoreInTimestamp() != null ) {

                    MainActivity.DEST_LAT = (float) order.getLat();
                    MainActivity.DEST_LNG = (float) order.getLng();

                    if ( ringtone != null ) {
                        ringtone.stop();
                    }

                    for (Order assignedOrder : assignedOrders) {
                        databaseManager.updateStatusOrder(assignedOrder.getId(), Constants.ORDER_STATUS_CODE.STORE_OUT, true);
                    }


                    Intent intent = new Intent(OrderListActivity.this, Navigation.class);
                    sharedPreferencesEditor.putInt(Constants.KEY_ORDER_ID, orderId);
                    sharedPreferencesEditor.apply();
                    intent.putExtra(Constants.KEY_ORDER_ID, orderId);
                    startActivity(intent);
                    finish();
                }
            }
        });
//
        if ( getIntFromPref(Constants.KEY_STORED_IN) == Constants.SHARED_PREF_FAILED_INT ) {
            countDownTimer = new CountDownTimer(Constants.STORE_IN_COUNTDOWN, 1000) {

                public void onTick(long millisUntilFinished) {

                    int seconds = (int) ((millisUntilFinished / 1000) % 60);
                    int minutes = (int) ((millisUntilFinished / 1000) / 60);

//                    sharedPreferencesEditor.putInt(Constants.KEY_SECONDS, seconds);
//                    sharedPreferencesEditor.putInt(Constants.KEY_MINUTES, minutes);
//                    sharedPreferencesEditor.apply();


                    countdownTimerTextView.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
                }

                public void onFinish() {


                }
            }.start();
        }
    }
    private View.OnClickListener storeInButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            StoreInProcessDialog dialog = new StoreInProcessDialog(OrderListActivity.this);
            dialog.setOnClickProcess(new StoreInProcessDialog.OnClickProcess() {
                @Override
                public void onClickProcess(String code) {
                    if ( code.length() == 0 ) {
                        dialog.setErrorMode();
                    }
                     else {
                        checkCode(code);
                        dialog.dismiss();
                    }
                }
            });
            dialog.show();

        }
    };


    private NavigationView.OnNavigationItemSelectedListener navigationViewOnNavigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            int id = item.getItemId();

            Intent intent;
            switch(id) {

                case R.id.closedTicketMenu:
                    intent = new Intent(OrderListActivity.this, OrderHistoryActivity.class);
                    intent.putExtra(Constants.KEY_HISTORY_TYPE, Constants.HISTORY_TYPE_CODE.CLOSED_TICKET.getValue());
                    startActivity(intent);
                    break;
                case R.id.remittanceMenu:
                    intent = new Intent(OrderListActivity.this, OrderHistoryActivity.class);
                    intent.putExtra(Constants.KEY_HISTORY_TYPE, Constants.HISTORY_TYPE_CODE.FOR_REMITTANCE.getValue());
                    startActivity(intent);
                    break;
                case R.id.archiveMenu:
                    intent = new Intent(OrderListActivity.this, OrderHistoryActivity.class);
                    intent.putExtra(Constants.KEY_HISTORY_TYPE, Constants.HISTORY_TYPE_CODE.ARCHIVE_TICKETS.getValue());
                    startActivity(intent);
                    break;
                default:
                    return true;
            }
            return true;

        }
    };

    void checkCode(String code) {
        Order order = assignedOrders.get(0);
        HttpConnection.doPost(new HttpConnection.Param[]{
                new HttpConnection.Param("code", code),
                new HttpConnection.Param("branch_id", order.getBranchId())
        }, Constants.API_URL + "cashier/check_code", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseBody = response.body().string();

                MResponse<Integer> responseObject = gson.fromJson(responseBody,
                        new TypeToken<MResponse<Integer>>(){}.getType());

                if ( !responseObject.isError() ) {
                    if ( responseObject.getResult() == 1 ) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sharedPreferencesEditor.putInt(Constants.KEY_STORED_IN, 9);
                                sharedPreferencesEditor.apply();
                                if ( countDownTimer != null ) {
                                    countDownTimer.cancel();

                                    for (Order order : assignedOrders) {
                                        databaseManager.updateStatusOrder(order.getId(), Constants.ORDER_STATUS_CODE.STORE_IN, true);
                                    }
                                }

                                storeInButton.setEnabled(false);
                                assignedOrders = databaseManager.getAssignedOrders();
                                orderListAdapter.updateOrders(assignedOrders);
                            }
                        });


                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(OrderListActivity.this).setMessage("wrong cashier's code")
                                        .show();
                            }
                        });
                    }
                }

            }
        });
    }

}
