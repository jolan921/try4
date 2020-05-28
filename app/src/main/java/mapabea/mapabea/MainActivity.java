package mapabea.mapabea;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

//import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends BaseActivity {

    private Button loginButton;
    private EditText usernameTextInputEditText;
    private EditText passwordTextInputEditText;


    private LocationManager locationManager;
    private LocationListener locationListener;


//    public static float LAT = (float) 14.559750;
//    public static float LNG = (float) 121.062958;
//    public static float DEST_LAT = (float) 14.688430;
//    public static float DEST_LNG = (float) 121.074530;
    public static float LAT = (float) 0;
    public static float LNG = (float) 0;
    public static float DEST_LAT = (float) 0;
    public static float DEST_LNG = (float) 0;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Fabric.with(this, new Crashlytics());

        if ( !Utils.canDrawOverlays(this) ) {

            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, Constants.OVERLAY_PERMISSION_REQ_CODE_CHATHEAD);

        } else {

            startService(new Intent(getApplicationContext(), UploadService.class));

        }

        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CAMERA}, Constants.REQUEST_PERMISSION_CODE);


        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        System.out.println("asdfadsfadsf");
        System.out.println(telephonyManager.getDeviceId());
    }



    @Override
    protected void initializeViews() {
        loginButton = findViewById(R.id.loginButton);
        usernameTextInputEditText = findViewById(R.id.usernameTextInputEditText);
        passwordTextInputEditText = findViewById(R.id.passwordTextInputEditText);

    }

    @Override
    protected void initializeListeners() {
        loginButton.setOnClickListener(loginButtonOnClickListener);

    }

    @Override
    protected void initialize() {
        super.initialize();


        //15 minutes
        new CountDownTimer(900000, 1000) {

            public void onTick(long millisUntilFinished) {

                int seconds = (int) ((millisUntilFinished / 1000) % 60);
                int minutes = (int) ((millisUntilFinished / 1000) / 60);

                System.out.println("minutes: " + minutes);
                System.out.println("seconds: " + seconds);
            }

            public void onFinish() {

            }
        }.start();

        return;
    }

    private View.OnClickListener loginButtonOnClickListener = new View.OnClickListener() {
        @SuppressLint("MissingPermission")
        @Override
        public void onClick(View view) {
            loginButton.setEnabled(false);
            HttpConnection.Param[] params = new HttpConnection.Param[3];
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

            params[0] = new HttpConnection.Param("email", usernameTextInputEditText.getText().toString());
            params[1] = new HttpConnection.Param("password", passwordTextInputEditText.getText().toString());
            params[2] = new HttpConnection.Param("imei", 1);


            HttpConnection.doPost(params, Constants.API_URL + "login", new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            loginButton.setEnabled(true);
                        }
                    });
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            loginButton.setEnabled(true);

                        }
                    });
                    final String responseBody = response.body().string();

                    System.out.println(responseBody);
                    MResponse<User> responseObject = gson.fromJson(responseBody,
                            new TypeToken<MResponse<User>>(){}.getType());

                    if ( responseObject.isError() ) {

//                        showMessage();
                    } else {

                        User user = responseObject.getResult();

                        sharedPreferencesEditor.putInt(Constants.BRANCH_ID, user.getBranchId());
                        sharedPreferencesEditor.putInt(Constants.KEY_DRIVER_ID, user.getId());
                        sharedPreferencesEditor.apply();
//                        startActivity(intent);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                initLocationService();
                            }
                        });
                    }


                }
            });
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.OVERLAY_PERMISSION_REQ_CODE_CHATHEAD) {

            System.out.println("TTTEESTT");
            if (!Utils.canDrawOverlays(MainActivity.this)) {
                System.out.println("TTTEESTT2");

                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, Constants.OVERLAY_PERMISSION_REQ_CODE_CHATHEAD);
            } else {

                startService(new Intent(getApplicationContext(), UploadService.class));
            }


        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);



        if ( grantResults.length > 0 ) {
            switch(requestCode) {
                case Constants.REQUEST_PERMISSION_CODE:
                    if ( grantResults.length > 0 ) {

                        if ( grantResults[0] == PackageManager.PERMISSION_DENIED && permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION  ){
                            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_PERMISSION_CODE);
                        }

                        else if ( grantResults[1] == PackageManager.PERMISSION_DENIED && permissions[1] == Manifest.permission.CAMERA  ){
                            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, Constants.REQUEST_PERMISSION_CODE);
                        }



                    }
                    break;




            }
        }
    }

    public void onclick_face_recognition(View view) {

    }




    @SuppressLint("MissingPermission")
    void initLocationService() {

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                HttpConnection.Param[] params = new HttpConnection.Param[5];



                MainActivity.LAT = (float) location.getLatitude();
                MainActivity.LNG = (float) location.getLongitude();

                params[0] = new HttpConnection.Param("lat", location.getLatitude());
                params[1] = new HttpConnection.Param("lng", location.getLongitude());
                params[2] = new HttpConnection.Param("actual_time", Utils.getStringifyCurrentDate());
                params[3] = new HttpConnection.Param("driver_id", sharedPreferences.getInt(Constants.KEY_DRIVER_ID, 0));
                params[4] = new HttpConnection.Param("trip_id", sharedPreferences.getInt(Constants.KEY_ORDER_ID, -1));

                HttpConnection.doPost(params, Constants.API_URL + "location/send", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        System.out.println(response.body().string());
                    }
                });
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

