package mapabea.mapabea.interfaces;

import mapabea.mapabea.Constants;
import mapabea.mapabea.MResponse;
import mapabea.mapabea.User;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface LoginService {

    @FormUrlEncoded
    @POST("driver/status")
    Call<MResponse<User>> getDriverStatus(@Field("imei") String imei);
}
