package mapabea.mapabea;

import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class MResponse<T> {

    private boolean error;
    private String message;
    private T result;

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }


}
