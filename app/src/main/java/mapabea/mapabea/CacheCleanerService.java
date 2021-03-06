package mapabea.mapabea;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CacheCleanerService extends Service {

    private DatabaseManager databaseManager;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.println("onStartCommand");

        databaseManager = new DatabaseManager(getApplicationContext());

        restartCleaning();

        return START_STICKY;
    }

    void restartCleaning() {


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timer.cancel();
                databaseManager.deleteOrders();
                restartCleaning();
            }
        }, 3000);


        

    }


}
