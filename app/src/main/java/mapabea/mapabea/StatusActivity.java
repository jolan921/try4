package mapabea.mapabea;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

//import com.crashlytics.android.Crashlytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//import io.fabric.sdk.android.Fabric;
import mapabea.mapabea.interfaces.LoginService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StatusActivity extends BaseActivity {
    private LinearLayout statusColorLinearLayout;
    private TextView statusNameTextView;

    private RecyclerView orderRecyclerView;
    private OrderAdapter orderAdapter;
    private RelativeLayout loadingRelativeLayout;

    private NavigationView navigationView;
    public static LocationManager locationManager;
    public static LocationListener locationListener;
    private boolean onPaused = false;
    private boolean onMenu = false;
    private String imei;
    public static String static_imei;
    private PowerManager.WakeLock wakeLock;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        throw new RuntimeException("Test Crash"); // Force a crash




    }

    @Override
    protected void initializeViews() {
        navigationView = (NavigationView)findViewById(R.id.navigationView);
        orderRecyclerView = findViewById(R.id.orderRecyclerView);
        loadingRelativeLayout = findViewById(R.id.loadingRelativeLayout);
        statusNameTextView = findViewById(R.id.statusNameTextView);
        statusColorLinearLayout = findViewById(R.id.statusColorLinearLayout);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if ( wakeLock != null ) {
            wakeLock.release();
        }
    }

    @Override
    protected void initializeListeners() {

        navigationView.setNavigationItemSelectedListener(navigationViewOnNavigationItemSelectedListener);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void initialize() {
        super.initialize();



//        Fabric.with(getApplicationContext(), new Crashlytics());

        if ( !Utils.canDrawOverlays(this) ) {

            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, Constants.OVERLAY_PERMISSION_REQ_CODE_CHATHEAD);

        } else {

            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            imei = telephonyManager.getDeviceId();
            static_imei = imei;
            startService(new Intent(getApplicationContext(), UploadService.class));
            startService(new Intent(getApplicationContext(), CacheCleanerService.class));

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "mapabea: wakelock:status_activity");
            wakeLock.acquire();

        }

        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE

        }, Constants.REQUEST_PERMISSION_CODE);

        int permissionWriteExternal = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if ( permissionWriteExternal == PackageManager.PERMISSION_GRANTED ) {

            File file = new File(Environment.getExternalStorageDirectory()+"/riderAppFiles");
            if ( !file.exists() ) {
                file.mkdirs();
            }
        }


        int driverId = getIntFromPref(Constants.KEY_DRIVER_ID);

        if ( driverId != Constants.SHARED_PREF_FAILED_INT ) {

            int currentOrderId = getIntFromPref(Constants.KEY_ORDER_ID);

            Intent intent = null;
            if ( currentOrderId == Constants.SHARED_PREF_FAILED_INT ) {

                checkStatus();

            } else {

                Integer currentOrderStatus = databaseManager.getOrderStatus(currentOrderId);


                if ( currentOrderStatus != null ) {

                    if ( currentOrderStatus == Constants.ORDER_STATUS_CODE.ASSIGNED.getValue() ) { // will never happen
                        intent = new Intent(StatusActivity.this, OrderListActivity.class);

                    } else if ( currentOrderStatus == Constants.ORDER_STATUS_CODE.STORE_IN.getValue() ) {
                        intent = new Intent(StatusActivity.this, OrderListActivity.class);

                    } else if ( currentOrderStatus == Constants.ORDER_STATUS_CODE.STORE_OUT.getValue() ) {
                        intent = new Intent(StatusActivity.this, Navigation.class);

                    } else if ( currentOrderStatus == Constants.ORDER_STATUS_CODE.VICINITY_IN.getValue() ) {
                        intent = new Intent(StatusActivity.this, ProofActivity.class);

                    }


                }
                if ( intent != null ) {

                    initLocationService();
                    intent.putExtra(Constants.KEY_ORDER_ID, currentOrderId);
                    startActivity(intent);
                    finish();
                } else {
                    checkStatus();
                }
            }
        } else {
            checkStatus();
        }




    }



    public void checkStatus() {


        List<Order> assignedOrders = databaseManager.getAssignedOrders();

        if ( assignedOrders.size() > 0 ) {

            initLocationService();

            Intent intent = new Intent(StatusActivity.this, OrderListActivity.class);

            startActivity(intent);

            finish();

            return;
        }


        sharedPreferencesEditor.remove(Constants.KEY_STORED_IN);
        sharedPreferencesEditor.apply();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timer.cancel();

                if (imei != null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(Constants.API_URL)

                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    LoginService checkDriverStatusService = retrofit.create(LoginService.class);

                    retrofit2.Call<MResponse<User>> userCall = checkDriverStatusService.getDriverStatus(imei);

                    try {
                        retrofit2.Response<MResponse<User>> response = userCall.execute();
                        User user = response.body().getResult();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (user != null && user.getIsLogin() == 1) {

                                    System.out.println("LOGGED IN");
                                    initLocationService();
                                    statusNameTextView.setText("Online");
                                    statusColorLinearLayout.setBackgroundColor(getResources().getColor(R.color.status_online));
                                    loadingRelativeLayout.setVisibility(View.VISIBLE);
                                    sharedPreferencesEditor.clear();
                                    sharedPreferencesEditor.apply();
                                    sharedPreferencesEditor.putInt(Constants.BRANCH_ID, user.getBranchId());
                                    sharedPreferencesEditor.putInt(Constants.KEY_DRIVER_ID, user.getId());
                                    sharedPreferencesEditor.putInt(Constants.KEY_HUB_ID, user.getHubId());
                                    sharedPreferencesEditor.apply();

                                    if ( onPaused ) {
                                        checkStatus();
                                    } else {
                                        System.out.println("looking for tickets before");
                                        getOrdersOnline();
                                    }
                                } else {

                                    statusNameTextView.setText("Offline");
                                    statusColorLinearLayout.setBackgroundColor(getResources().getColor(R.color.status_offline));
                                    loadingRelativeLayout.setVisibility(View.INVISIBLE);
                                    stopLocationService();

//                                         sharedPreferencesEditor.clear();
//                                            sharedPreferencesEditor.apply();

                                    checkStatus();
                                }
                            }
                        });


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    HttpConnection.doPost(new HttpConnection.Param[]{
//                            new HttpConnection.Param("imei", imei)
//                    }, Constants.API_URL + "/driver/status", new Callback() {
//                        @Override
//                        public void onFailure(Call call, IOException e) {
//
//                            checkStatus();
//                        }
//
//                        @Override
//                        public void onResponse(Call call, Response response) throws IOException {
//
//                            final String responseBody = response.body().string();
//
//                            System.out.println("check status response: ");
//                            System.out.println(responseBody);
//                            MResponse<User> responseObject = gson.fromJson(responseBody,
//                                    new TypeToken<MResponse<User>>() {
//                                    }.getType());
//
//                            if (responseObject.isError()) {
//
//
//                            } else {
//
//                                User user = responseObject.getResult();
//
//
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        if (user != null && user.getIsLogin() == 1) {
//
//                                            initLocationService();
//                                            statusNameTextView.setText("Online");
//                                            statusColorLinearLayout.setBackgroundColor(getResources().getColor(R.color.status_online));
//                                            loadingRelativeLayout.setVisibility(View.VISIBLE);
//                                            sharedPreferencesEditor.clear();
//                                            sharedPreferencesEditor.apply();
//                                            sharedPreferencesEditor.putInt(Constants.BRANCH_ID, user.getBranchId());
//                                            sharedPreferencesEditor.putInt(Constants.KEY_DRIVER_ID, user.getId());
//                                            sharedPreferencesEditor.putInt(Constants.KEY_HUB_ID, user.getHubId());
//                                            sharedPreferencesEditor.apply();
//
//                                            if ( onPaused ) {
//                                                checkStatus();
//                                            } else {
//
//                                                System.out.println("looking for tickets before");
//                                                getOrdersOnline();
//                                            }
//                                        } else {
//
//                                            statusNameTextView.setText("Offline");
//                                            statusColorLinearLayout.setBackgroundColor(getResources().getColor(R.color.status_offline));
//                                            loadingRelativeLayout.setVisibility(View.INVISIBLE);
//                                            stopLocationService();
//
////                                            sharedPreferencesEditor.clear();
////                                            sharedPreferencesEditor.apply();
//
//                                            checkStatus();
//
//                                        }
//                                    }
//                                });
//                            }
//                        }
//                    });
//                } else {
//                    checkStatus();
//                }
                } else {
                   checkStatus();
                }
            }

        }, 3000);


    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.OVERLAY_PERMISSION_REQ_CODE_CHATHEAD) {

            if (!Utils.canDrawOverlays(StatusActivity.this)) {

                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, Constants.OVERLAY_PERMISSION_REQ_CODE_CHATHEAD);
            } else {

                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                imei = telephonyManager.getDeviceId();
                static_imei = imei;
                startService(new Intent(getApplicationContext(), UploadService.class));
                startService(new Intent(getApplicationContext(), CacheCleanerService.class));
            }


        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if ( grantResults.length > 0 && grantResults[3] == PackageManager.PERMISSION_GRANTED ) {

            File file = new File(Environment.getExternalStorageDirectory()+"/riderAppFiles");
            if ( !file.exists() ) {
                file.mkdirs();
            }
        }


    }

    void stopLocationService() {
        if ( locationManager != null ) {
            locationManager.removeUpdates(locationListener);
            locationManager = null;
        }
    }
    @SuppressLint("MissingPermission")
    void initLocationService() {

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    HttpConnection.Param[] params = new HttpConnection.Param[5];


                    MainActivity.LAT = (float) location.getLatitude();
                    MainActivity.LNG = (float) location.getLongitude();

                    List<Order> assignedOrders = databaseManager.getAssignedOrders();
                    for ( Order order : assignedOrders ) {

                        params[0] = new HttpConnection.Param("lat", location.getLatitude());
                        params[1] = new HttpConnection.Param("lng", location.getLongitude());
                        params[2] = new HttpConnection.Param("actual_time", Utils.getStringifyCurrentDate());
//                        params[3] = new HttpConnection.Param("driver_id", sharedPreferences.getInt(Constants.KEY_DRIVER_ID, 0));
                        params[3] = new HttpConnection.Param("driver_id", sharedPreferences.getInt(Constants.KEY_DRIVER_ID, 0));
//                        params[4] = new HttpConnection.Param("trip_id", sharedPreferences.getInt(Constants.KEY_ORDER_ID, -1));
                        params[4] = new HttpConnection.Param("trip_id", order.getId());

                        HttpConnection.doPost(params, Constants.API_URL + "location/send", new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {

                            }
                        });
                    }

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        onPaused = false;
        onMenu = false;

    }

    @Override
    protected void onPause() {
        super.onPause();
        onPaused = onMenu;


    }

    private NavigationView.OnNavigationItemSelectedListener navigationViewOnNavigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            onMenu = true;

            int id = item.getItemId();
            Intent intent;
            switch(id) {

                case R.id.closedTicketMenu:
                    intent = new Intent(StatusActivity.this, OrderHistoryActivity.class);
                    intent.putExtra(Constants.KEY_HISTORY_TYPE, Constants.HISTORY_TYPE_CODE.CLOSED_TICKET.getValue());
                    startActivity(intent);
                    break;
                case R.id.remittanceMenu:
                    intent = new Intent(StatusActivity.this, OrderHistoryActivity.class);
                    intent.putExtra(Constants.KEY_HISTORY_TYPE, Constants.HISTORY_TYPE_CODE.FOR_REMITTANCE.getValue());
                    startActivity(intent);
                    break;
                case R.id.archiveMenu:
                    intent = new Intent(StatusActivity.this, OrderHistoryActivity.class);
                    intent.putExtra(Constants.KEY_HISTORY_TYPE, Constants.HISTORY_TYPE_CODE.ARCHIVE_TICKETS.getValue());
                    startActivity(intent);
                    break;
                case R.id.chatMenu:
                    intent = new Intent(StatusActivity.this, MessengerActivity.class);
                    startActivity(intent);
                    break;
                default:
                    return true;
            }
            return true;

        }
    };



    void getOrdersOnline() {


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timer.cancel();

                int forRemmitanceCount = databaseManager.getPendingRemittanceCount();

                if ( forRemmitanceCount >= 3 && false ) {

                    showMessage("There are more than 2 pending ticket for remittance...");
                    checkStatus();
                    return;

                } else {
                    if ( MainActivity.LAT == 0 || MainActivity.LAT == 0 ) {
//                        showMessage("Latitude: " + MainActivity.LAT + "longitude: " + MainActivity.LNG + "\nGPS SIGNAL IS WEAK... retying to find ticket....");
//                        checkStatus();
//                        return;
                    }
                    System.out.println(Constants.API_URL + "trips/find");
                    HttpConnection.doPost(new HttpConnection.Param[]{
                                    new HttpConnection.Param("lat", MainActivity.LAT),
                                    new HttpConnection.Param("lng", MainActivity.LNG),
                                    new HttpConnection.Param("imei", imei),
                                    new HttpConnection.Param("timestamp", Utils.getStringifyCurrentDate()),
                                    new HttpConnection.Param("driver_id", sharedPreferences.getInt(Constants.KEY_DRIVER_ID, -1))},
                            Constants.API_URL + "trips/find", new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {


                                    checkStatus();

                                    //getOrdersOnline
                                    // ();

                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {

                                    String responseBody = response.body().string();



                                    MResponse<List<Order>> responseObject = gson.fromJson(responseBody, new TypeToken<MResponse<List<Order>>>() {}.getType());

                                    if (responseObject.isError()) {


                                    } else {

                                        StatusActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                if ( databaseManager.getAssignedOrders().size() <= 0 ) {

                                                    List<Order> fetchedOrders = responseObject.getResult();

                                                    if (fetchedOrders != null && fetchedOrders.size() > 0) {

                                                        databaseManager.insertOrders(fetchedOrders, getIntFromPref(Constants.KEY_DRIVER_ID), Constants.ORDER_STATUS_CODE.ASSIGNED);

//
//                                                    MainActivity.DEST_LAT = (float) fetchedOrder.getLat();
//                                                    MainActivity.DEST_LNG = (float) fetchedOrder.getLng();
//                                                    sharedPreferencesEditor.putInt(Constants.KEY_ORDER_ID, fetchedOrder.getId());
//                                                    sharedPreferencesEditor.apply();

                                                        Intent intent = new Intent(StatusActivity.this, OrderListActivity.class);

                                                        startActivity(intent);

                                                        finish();


                                                    } else {


                                                        checkStatus();
                                                        // getOrdersOnline();

                                                    }
                                                }

                                            }
                                        });

                                    }
                                }
                            });
                }

            }
        }, 2000);
    }

}
