package mapabea.mapabea;
import android.app.Activity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MyDialog extends Activity {
	public static boolean active = false;
	public static Activity myDialog;
	
	EditText edt;
	Button btn;
	View top;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.dialog);
		
		edt = (EditText) findViewById(R.id.dialog_edt);
		btn = (Button) findViewById(R.id.dialog_btn);
		top = (View)findViewById(R.id.dialog_top);
				
		myDialog = MyDialog.this;
		
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String str = edt.getText().toString();
				if(str.length() > 0){
					try{


						DatabaseManager databaseManager = new DatabaseManager(MyDialog.this);
//						Order currentOrder = databaseManager.getCurrentOrder(getIntent().getExtras().getString(Constants.COLUMN_ORDER_ID));
						SmsManager smgr = SmsManager.getDefault();
						smgr.sendTextMessage("09088751787",null, str,null,null);
						Toast.makeText(MyDialog.this, "SMS Sent Successfully", Toast.LENGTH_SHORT).show();
					}
					catch (Exception e){
						Toast.makeText(MyDialog.this, "SMS Failed to Send, Please try again", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		
		
		top.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}




	@Override
	protected void onResume() {
		super.onResume();
		active = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		active = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		active = false;
	}

	
	
}
