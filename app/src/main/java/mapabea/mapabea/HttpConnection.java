package mapabea.mapabea;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpConnection {


    private static String HTTP_TAG = "HTTP_TAG";
    public static void doPost(Param[] params, String url, Callback callback) {

        OkHttpClient client = new OkHttpClient();


        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        if ( params == null ) {
            multipartBodyBuilder.addFormDataPart("", "");
        } else {
            for (Param param : params) {
                System.out.println(param.key + " gago " + param.value.toString());
                if ( param.requestBody == null ) {
                    multipartBodyBuilder.addFormDataPart(param.key, param.value.toString());
                } else {
                    multipartBodyBuilder.addFormDataPart(param.key, param.value.toString(), param.requestBody);
                }
            }
        }
        RequestBody requestBody = multipartBodyBuilder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        if ( callback == null ) {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(HTTP_TAG, response.body().string());
                }
            });
        } else {
            client.newCall(request).enqueue(callback);
        }
    }

    public static class Param {
        public Param(String key, Object value) {
            this.key = key;
            this.value = value;
            this.requestBody = null;
        }
        public Param(String key, Object value, RequestBody requestBody) {
            this.key = key;
            this.value = value;
            this.requestBody = requestBody;
        }
        public String key;
        public Object value;
        public RequestBody requestBody;
    }
}
