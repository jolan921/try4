package mapabea.mapabea;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;

import static android.content.Context.MODE_PRIVATE;

public class ToolBox {


    public Gson gson;
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor sharedPreferencesEditor;
    public DatabaseManager databaseManager;
    public SimpleDateFormat dateFormat;

    public ToolBox(Context context) {

        gson = new Gson();
        sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
        databaseManager = new DatabaseManager(context);
        dateFormat = new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss");
    }
}
