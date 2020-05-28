package mapabea.mapabea;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.flipkart.chatheads.ui.ChatHead;
import com.flipkart.chatheads.ui.ChatHeadContainer;
import com.flipkart.chatheads.ui.ChatHeadViewAdapter;
import com.google.gson.reflect.TypeToken;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Response;
import retrofit2.Callback;


public class Navigation extends BaseActivity {

    private Bundle _savedInstanceState;

    private ChatHeadContainer chatHead;

    private NavigationView nav_view;

    private Button submitButton;

    private int currentOrderId = 0;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Mapbox.getInstance(this, "pk.eyJ1IjoiaGltZWppbWExMiIsImEiOiJjam1ucWNxcHQwd2Q1M3ZqcGEwNDV2Z2R4In0.AoX5UXEBvtFWJVO6RhYG7Q");

        super.onCreate(savedInstanceState);

        _savedInstanceState = savedInstanceState;

    }


    @Override
    protected void initialize() {
        super.initialize();


        Bundle bundle = getIntent().getExtras();

        if ( bundle != null ) {
            if ( bundle.containsKey(Constants.KEY_ORDER_ID) ) {

                currentOrderId = bundle.getInt(Constants.KEY_ORDER_ID);
            }
        }

        chatHead.setViewAdapter(chatHeadViewAdapter);

        chatHead.addChatHead("chatHead", true, false);

        //startService(new Intent(Navigation.this, ChatHeadService.class));

        nav_view.onCreate(_savedInstanceState);
    }

    @Override
    protected void initializeViews() {

        chatHead = findViewById(R.id.chatHead);
        nav_view = findViewById(R.id.nav_view);
        submitButton = findViewById(R.id.submitButton);
    }

    @Override
    protected void initializeListeners() {
        nav_view.initialize(wazeOnNavigationCallback);

        submitButton.setOnClickListener(submitButtonOnClickListener);
    }




    void launch(Point dest) {

        if ( MainActivity.LNG == 0 || MainActivity.LAT == 0 ) {
            showMessage("Latitude: " + MainActivity.LAT + "longitude: " + MainActivity.LNG + "\nGPS SIGNAL IS WEAK");
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    timer.cancel();
                    launch(null);
                }
            }, 3000);

            return;
        } else {
            showMessage("Latitude: " + MainActivity.LAT + "longitude: " + MainActivity.LNG + "\nWAZE IS STARTING....");
        }
        NavigationRoute.builder(this).accessToken("pk.eyJ1IjoiaGltZWppbWExMiIsImEiOiJjam1ucWNxcHQwd2Q1M3ZqcGEwNDV2Z2R4In0.AoX5UXEBvtFWJVO6RhYG7Q")
                .origin(Point.fromLngLat(MainActivity.LNG, MainActivity.LAT))

                .destination(Point.fromLngLat(MainActivity.DEST_LNG, MainActivity.DEST_LAT)).build().getRoute(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(retrofit2.Call<DirectionsResponse> call, retrofit2.Response<DirectionsResponse> response) {

                DirectionsRoute route = response.body().routes().get(0);

                NavigationViewOptions options = NavigationViewOptions.builder()
                        .directionsRoute(route)
                        .progressChangeListener(navigationOnProgressChangeListener)
                        .build();
                try {
                    nav_view.startNavigation(options);
                }
                catch(Exception e) {}
            }

            @Override
            public void onFailure(retrofit2.Call<DirectionsResponse> call, Throwable t) {

            }
        });

    }

    boolean computing = false;
    private ProgressChangeListener navigationOnProgressChangeListener = new ProgressChangeListener() {

        @Override
        public void onProgressChange(Location location, RouteProgress routeProgress) {

            submitButton.setVisibility(View.GONE);

            if ( !computing ) {
                computing = true;
                double meters = Utils.distance(MainActivity.LAT, MainActivity.DEST_LAT, MainActivity.LNG, MainActivity.DEST_LNG);
                if (meters <= 200) {


                    HttpConnection.doPost(new HttpConnection.Param[]{
                            new HttpConnection.Param("trip_id", currentOrderId),
                            new HttpConnection.Param("timestamp", Utils.getStringifyCurrentDate()),
                            new HttpConnection.Param("driver_id", getIntFromPref(Constants.KEY_DRIVER_ID))

                    }, Constants.API_URL + "trip/vicinity_in", new okhttp3.Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {
                            vicinityIn(true);
                            showSavingOfflineMessage();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            String responseBody = response.body().string();

                            MResponse responseObject = gson.fromJson(responseBody,
                                    new TypeToken<MResponse>() {
                                    }.getType());

                            if (responseObject.isError()) {

                                vicinityIn(true);
                                showSavingOfflineMessage();
                            } else {

                                vicinityIn(false);
                            }
                        }


                    });

                    //stopService(new Intent(Navigation.this, ChatHeadService.class));
                    Intent intent = new Intent(Navigation.this, ProofActivity.class);
                    intent.putExtra(Constants.KEY_ORDER_ID, currentOrderId);

                    startActivity(intent);
                    finish();


                } else {
                    computing = false;
                }
            }
        }
    };


    private View.OnClickListener submitButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            AlertDialog.Builder builder = new AlertDialog.Builder(Navigation.this);
            builder.setMessage("Are you sure you want to submit?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            vicinityIn(true);
                            //stopService(new Intent(Navigation.this, ChatHeadService.class));
                            Intent intent = new Intent(Navigation.this, ProofActivity.class);
                            intent.putExtra(Constants.KEY_ORDER_ID, currentOrderId);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("No", null).show();
        }
    };


    private OnNavigationReadyCallback wazeOnNavigationCallback = new OnNavigationReadyCallback() {
        @Override
        public void onNavigationReady(boolean b) {


            System.out.println("asfdafdadsfadsfas");
            launch(LocationReceiver.latestLocation);
        }

    };


    @Override
    public void onStart() {
        super.onStart();
        nav_view.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        nav_view.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        nav_view.onLowMemory();
    }

    @Override
    public void onBackPressed() {

    }

    public void vicinityIn(boolean offline) {

        List<Order> assignedOrders = databaseManager.getAssignedOrders();

        for ( Order order : assignedOrders ) {
            databaseManager.updateStatusOrder(order.getId(), Constants.ORDER_STATUS_CODE.VICINITY_IN, offline);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        nav_view.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        nav_view.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        nav_view.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        nav_view.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nav_view.onDestroy();
    }

    private ChatHeadViewAdapter chatHeadViewAdapter = new ChatHeadViewAdapter() {
        @Override
        public FragmentManager getFragmentManager() {
            return getSupportFragmentManager();
        }

        @Override
        public Fragment instantiateFragment(Object key, ChatHead chatHead) {
            return null;
        }



        @Override
        public Drawable getChatHeadDrawable(Object key) {
            return getResources().getDrawable(R.drawable.silhouette);
        }

        @Override
        public Drawable getPointerDrawable() {
            return null;
        }

        @Override
        public View getTitleView(Object key, ChatHead chatHead) {
            return null;
        }
    };
}
