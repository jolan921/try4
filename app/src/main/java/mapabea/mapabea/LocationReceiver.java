package mapabea.mapabea;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.mapbox.geojson.Point;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LocationReceiver implements LocationListener {


    public static Point latestLocation;
    public static Point latestTrip;
    private HttpConnection.Param[] params;

    private ToolBox toolBox;



    public void setDataToSend(HttpConnection.Param[] params) {
        this.params = params;
    }

    @Override
    public void onLocationChanged(Location location) {

        System.out.println("result: onLocationChanged: called" + location.getLatitude() + ", " + location.getLongitude());

        HttpConnection.Param[] params = new HttpConnection.Param[5];

       // latestLocation = Point.fromLngLat(location.getLongitude(), location.getLatitude());

        MainActivity.LAT = (float) location.getLatitude();
        MainActivity.LNG = (float) location.getLongitude();

        params[0] = new HttpConnection.Param("lat", location.getLatitude());
        params[1] = new HttpConnection.Param("lng", location.getLongitude());
        params[2] = new HttpConnection.Param("actual_time", Utils.getStringifyCurrentDate());
        params[3] = this.params[0];
        params[4] = this.params[1];

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
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }



}
