package mapabea.mapabea;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;

import androidx.cardview.widget.CardView;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.gson.reflect.TypeToken;
import com.hsalf.smilerating.SmileRating;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProofActivity extends BaseActivity {

    private ImageView emptyPhotoImageView;
    private ImageView emptySignatureImageView;
    private ImageView backImageView;
    private ImageView signatureImageView;
    private ImageView photoImageView;
    private RelativeLayout digitalSignatureRelativeLayout;
    private RelativeLayout loadingRelativeLayout;
    private LinearLayout photoRootLinearLayout;
    private LinearLayout signatureRootLinearLayout;
    private LinearLayout formLinearLayout;
    private SignaturePad signaturePad;
    private Button digitalSignatureDoneButton;
    private Button submitButton;
    private Button sendMessage;
    private CardView signatureCardView;

    private String photoFilename;
    private String signatureFilename;
    private SmileRating smileRating;
    private SmileRating signatureViewSmileRating;

    private int currentOrderId;

    private Bitmap photoBitmap;


    private TextView customerNameTextView;
    private TextView customerAddressTextView;
    private TextView orderAmountTextView;
    private TextView orderPaymentOptionTextView;
    private TextView ticketNoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




    }

    @Override
    protected void initialize() {
        super.initialize();


        Bundle bundle = getIntent().getExtras();

        if ( bundle != null ) {

            if (bundle.containsKey(Constants.KEY_ORDER_ID)) {


                currentOrderId = bundle.getInt(Constants.KEY_ORDER_ID);
                Order order = databaseManager.getOrder(currentOrderId);
                photoFilename = "photo_" + order.getOrderNumber() + ".png";
                signatureFilename = "sig_" + order.getOrderNumber() + ".png";


                int paymentMode = order.getPaymentMode();
                String paymentModeText = "";

                if ( paymentMode == 1 ) {
                    paymentModeText = "CASH";
                } else if ( paymentMode == 2 ) {
                    paymentModeText = "CREDIT CARD";
                } else if ( paymentMode == 3 ) {
                    paymentModeText = "DEBIT CARD";
                } else if ( paymentMode == 4 ) {
                    paymentModeText = "PESO PAY";
                }
                ticketNoTextView.setText(order.getOrderNumber());

                customerAddressTextView.setText(order.getOrderConsigneeAddress());
                customerNameTextView.setText(order.getOrderOriginalConsigneeName());
                orderAmountTextView.setText(order.getAmount() + "");
                orderPaymentOptionTextView.setText(paymentModeText);
            }
        }
    }

    @Override
    protected void initializeViews() {

        emptyPhotoImageView = findViewById(R.id.emptyPhotoImageView);
        emptySignatureImageView = findViewById(R.id.emptySignatureImageView);
        backImageView = findViewById(R.id.backImageView);
        photoRootLinearLayout = findViewById(R.id.photoRootLinearLayout);
        signatureRootLinearLayout = findViewById(R.id.signatureRootLinearLayout);

        digitalSignatureRelativeLayout = findViewById(R.id.digitalSignatureRelativeLayout);
        signaturePad = findViewById(R.id.signaturePad);
        formLinearLayout = findViewById(R.id.formLinearLayout);
        digitalSignatureDoneButton = findViewById(R.id.digitalSignatureDoneButton);
        signatureImageView = findViewById(R.id.signatureImageView);
        photoImageView = findViewById(R.id.photoImageView);
        smileRating = findViewById(R.id.smileRating);
        loadingRelativeLayout = findViewById(R.id.loadingRelativeLayout);
        signatureViewSmileRating = findViewById(R.id.signatureViewSmileRating);
        signatureCardView = findViewById(R.id.signatureCardView);


        customerAddressTextView = findViewById(R.id.customerAddressTextView);
        customerNameTextView = findViewById(R.id.customerNameTextView);
        orderAmountTextView = findViewById(R.id.orderAmountTextView);
        orderPaymentOptionTextView = findViewById(R.id.orderPaymentOptionTextView);
        ticketNoTextView = findViewById(R.id.ticketNoTextView);

    }

    @Override
    protected void initializeListeners() {
        backImageView.setOnClickListener(backImageViewOnClickListener);
        photoRootLinearLayout.setOnClickListener(photoRootLinearLayoutOnClickListener);
        signatureRootLinearLayout.setOnClickListener(signatureRootLinearLayoutOnClickListener);
        digitalSignatureDoneButton.setOnClickListener(digitalSignatureDoneButtonOnClickListener);

        smileRating.setOnRatingSelectedListener(smileRatingOnRatingSelectedListener);
        signatureViewSmileRating.setOnRatingSelectedListener(signatureViewSmileRatingOnRatingSelectedListener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == Constants.CAMERA_CODE ) {
            if ( resultCode == RESULT_OK ) {
                photoBitmap = (Bitmap) data.getExtras().get("data");
                photoImageView.setImageBitmap(photoBitmap);
                emptyPhotoImageView.setVisibility(View.GONE);
                photoImageView.setVisibility(View.VISIBLE);
                saveImage(photoBitmap, photoFilename);
                loadingRelativeLayout.setVisibility(View.VISIBLE);
                completeOrdersOnline();

            } else {
                photoRootLinearLayout.performClick();
            }

        }

    }



    @SuppressLint("MissingPermission")
    void completeOrdersOnline() {


        final String ePhotoTemp = "";
        final String eSigTemp = "";

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);


                        HttpConnection.Param[] params = new HttpConnection.Param[9];


                        params[0] = new HttpConnection.Param("trip_actual_end_time", Utils.getStringifyCurrentDate());


                        final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
                        Order order = databaseManager.getOrder(currentOrderId);
//                        signatureFilename = "photo_" + order.getOrderNumber();
                        params[1] = new HttpConnection.Param("trip_consignee_img", ePhotoTemp);
                        params[2] = new HttpConnection.Param("trip_consignee_signature", eSigTemp);
                        params[3] = new HttpConnection.Param("trip_id", currentOrderId);
                        params[4] = new HttpConnection.Param("rating", smileRating.getRating());
                        params[5] = new HttpConnection.Param("driver_id", sharedPreferences.getInt(Constants.KEY_DRIVER_ID, 0));
                        params[6] = new HttpConnection.Param("imei", telephonyManager.getDeviceId());
                        params[7] = new HttpConnection.Param("signature",  signatureFilename,  RequestBody.create(MEDIA_TYPE_PNG, Utils.bitmapToRaw(((BitmapDrawable)signatureImageView.getDrawable()).getBitmap())));
                        params[8] = new HttpConnection.Param("photo", photoFilename, RequestBody.create(MEDIA_TYPE_PNG, Utils.bitmapToRaw(photoBitmap)));

                        HttpConnection.doPost(params, Constants.API_URL + "trip/complete", new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                                databaseManager.updateStatusOrder(currentOrderId, Constants.ORDER_STATUS_CODE.SERVED, true);
                                databaseManager.updateForDelivered(currentOrderId, photoFilename, signatureFilename, smileRating.getRating());

                                showSavingOfflineMessage();

                                _finish();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String responseBody = response.body().string();

                                System.out.println(responseBody);
                                MResponse responseObject = gson.fromJson(responseBody, new TypeToken<MResponse>(){}.getType());

                                if ( responseObject.isError() ) {
                                    databaseManager.updateStatusOrder(currentOrderId, Constants.ORDER_STATUS_CODE.SERVED, true);
                                    databaseManager.updateForDelivered(currentOrderId, photoFilename, signatureFilename
                                            , smileRating.getRating());

                                    showSavingOfflineMessage();

                                    _finish();
                                } else {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            databaseManager.updateStatusOrder(currentOrderId, Constants.ORDER_STATUS_CODE.SERVED, false);
                                            databaseManager.updateForDelivered(currentOrderId, photoFilename, signatureFilename, smileRating.getRating());

                                            _finish();
                                        }
                                    });
                                }
                            }
                        });
    }
    private void saveImage(Bitmap finalBitmap, String filename) {


        File file = new File(Environment.getExternalStorageDirectory() + "/riderAppFiles", filename);

        FileOutputStream out = null;

        try {
            out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void _finish() {

        List<Order> orders = databaseManager.getAssignedOrders();

        if ( orders.size() <= 0 ) {
            sharedPreferencesEditor.remove(Constants.KEY_ORDER_ID);
            sharedPreferencesEditor.apply();
            Intent intent = new Intent(ProofActivity.this, StatusActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        } else {
            Intent intent = new Intent(ProofActivity.this, ProofActivity.class);
            intent.putExtra(Constants.KEY_ORDER_ID, orders.get(0).getId());
            startActivity(intent);
            finish();

        }


    }



    private View.OnClickListener backImageViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };



    private SmileRating.OnRatingSelectedListener smileRatingOnRatingSelectedListener = new SmileRating.OnRatingSelectedListener() {
        @Override
        public void onRatingSelected(int i, boolean b) {
            signatureViewSmileRating.setSelectedSmile(i - 1);
            signatureRootLinearLayout.performClick();
        }
    };
    private SmileRating.OnRatingSelectedListener signatureViewSmileRatingOnRatingSelectedListener = new SmileRating.OnRatingSelectedListener() {
        @Override
        public void onRatingSelected(int i, boolean b) {

            signatureViewSmileRating.setSelectedSmile(smileRating.getRating() - 1);
        }
    };

    private View.OnClickListener digitalSignatureDoneButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            digitalSignatureRelativeLayout.setVisibility(View.GONE);
            formLinearLayout.setVisibility(View.VISIBLE);
            if ( !signaturePad.isEmpty() ) {
                signatureImageView.setImageBitmap(signaturePad.getSignatureBitmap());
                emptySignatureImageView.setVisibility(View.GONE);
                signatureImageView.setVisibility(View.VISIBLE);
                saveImage(((BitmapDrawable) signatureImageView.getDrawable()).getBitmap(), signatureFilename);
                signaturePad.clear();

                photoRootLinearLayout.performClick();
            }


        }
    };



    private View.OnClickListener photoRootLinearLayoutOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");



            startActivityForResult(intent, Constants.CAMERA_CODE);

        }
    };
    private View.OnClickListener signatureRootLinearLayoutOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            digitalSignatureRelativeLayout.setVisibility(View.VISIBLE);
            formLinearLayout.setVisibility(View.GONE);

        }
    };
}
