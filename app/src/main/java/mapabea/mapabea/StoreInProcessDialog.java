package mapabea.mapabea;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class StoreInProcessDialog extends Dialog implements android.view.View.OnClickListener {

    public OnClickProcess getOnClickProcess() {
        return onClickProcess;
    }

    public void setOnClickProcess(OnClickProcess onClickProcess) {
        this.onClickProcess = onClickProcess;
    }

    interface OnClickProcess {
        void onClickProcess(String input);
    }
    private OnClickProcess onClickProcess;

    private Button processButton;
    private EditText inputEditText;
    private TextView error1TextView;

    public StoreInProcessDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cashier_input);

        processButton = findViewById(R.id.processButton);
        inputEditText = findViewById(R.id.inputEditText);
        error1TextView = findViewById(R.id.error1TextView);

        processButton.setOnClickListener(this);
    }
    public void setErrorMode() {
        error1TextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        onClickProcess.onClickProcess(inputEditText.getText().toString());

    }
}
