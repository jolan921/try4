package mapabea.mapabea;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class OrderHistoryActivity extends BaseActivity {

    private RecyclerView historyRecyclerView;
    private OrderHistoryAdapter orderHistoryAdapter;
    private ImageView backButtonImageView;
    private TextView headerTextView;
    private List<Order> cachedOrders;
    private int historyType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void initialize() {
        super.initialize();

        Bundle bundle = getIntent().getExtras();

        if ( bundle != null && bundle.containsKey(Constants.KEY_HISTORY_TYPE) ) {

            historyType = bundle.getInt(Constants.KEY_HISTORY_TYPE);

            if ( historyType == Constants.HISTORY_TYPE_CODE.CLOSED_TICKET.getValue() ) {
                headerTextView.setText("Order History");
                cachedOrders = databaseManager.getHistoryOrders();
                //displayTickets(Constants.API_URL + "/trips/history/closed");

            } else if ( historyType == Constants.HISTORY_TYPE_CODE.FOR_REMITTANCE.getValue() ){
                headerTextView.setText("For remittance ticket");
                cachedOrders = databaseManager.getForRemittanceOrders();
                //displayTickets(Constants.API_URL + "/trips/history/forRemittance");

            } else if ( historyType == Constants.HISTORY_TYPE_CODE.ARCHIVE_TICKETS.getValue() ) {

                headerTextView.setText("Archived Ticket");

                cachedOrders = databaseManager.getHistoryOrders();
                displayTickets(Constants.API_URL + "trips/history/archived");
            }


            orderHistoryAdapter = new OrderHistoryAdapter(OrderHistoryActivity.this, cachedOrders, historyType);

            orderHistoryAdapter.setOnClickedRemit(orderHistoryAdapterOnClickedRemit);

            historyRecyclerView.setLayoutManager(new LinearLayoutManager(OrderHistoryActivity.this));

            historyRecyclerView.setAdapter(orderHistoryAdapter);
        }
    }

    @Override
    protected void initializeViews() {

        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        backButtonImageView = findViewById(R.id.backButtonImageView);
        headerTextView = findViewById(R.id.headerTextView);

    }

    @Override
    protected void initializeListeners() {
        backButtonImageView.setOnClickListener(backButtonImageViewOnClickListener);
    }

    private View.OnClickListener backButtonImageViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    @SuppressLint("MissingPermission")
    public void displayTickets(String url) {
        String cachedOrderIds = "-1";
        for ( Order order : cachedOrders ) {
            cachedOrderIds += "," + order.getId();
        }

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        HttpConnection.doPost(new HttpConnection.Param[]{
                                new HttpConnection.Param("imei", telephonyManager.getDeviceId()),
                                new HttpConnection.Param("cached_order_ids", cachedOrderIds)
                },
                url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                        orderHistoryAdapter = new OrderHistoryAdapter(OrderHistoryActivity.this, cachedOrders, historyType);

                        orderHistoryAdapter.setOnClickedRemit(orderHistoryAdapterOnClickedRemit);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                historyRecyclerView.setLayoutManager(new LinearLayoutManager(OrderHistoryActivity.this));

                                historyRecyclerView.setAdapter(orderHistoryAdapter);
                            }
                        });

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        String responseBody = response.body().string();

                        System.out.println(responseBody);
                        MResponse<List<OrderHistoryItem>> responseObject = gson.fromJson(responseBody, new TypeToken<MResponse<List<OrderHistoryItem>>>(){}.getType());

                        if ( responseObject.isError() ) {

                            orderHistoryAdapter = new OrderHistoryAdapter(OrderHistoryActivity.this, cachedOrders, historyType);


                            orderHistoryAdapter.setOnClickedRemit(orderHistoryAdapterOnClickedRemit);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    historyRecyclerView.setLayoutManager(new LinearLayoutManager(OrderHistoryActivity.this));

                                    historyRecyclerView.setAdapter(orderHistoryAdapter);
                                }
                            });
                        } else {
                            List<OrderHistoryItem> orderHistoryItems = responseObject.getResult();

                            List<Order> orders = new ArrayList<>();

                            Order order = null;

                            for ( OrderHistoryItem orderHistoryItem : orderHistoryItems ) {

                                if ( order == null ) {
                                    order = new Order();
                                } else if (  orderHistoryItem.getOrderId() != order.getId() ) {
                                    orders.add(order);

                                    order = new Order();
                                }

                                order.setId(orderHistoryItem.getOrderId());
                                order.setOrderNumber(orderHistoryItem.getTicket());
                                int statusId = orderHistoryItem.getStatusId();
                                if ( statusId == 3 ) {
                                    order.setStoreInTimestamp(orderHistoryItem.getTimestamp());

                                } else if ( statusId == 4 ) {
                                    order.setStoreOutTimestamp(orderHistoryItem.getTimestamp());

                                } else if ( statusId == 5 ) {
                                    order.setVicinityInTimestamp(orderHistoryItem.getTimestamp());

                                } else if ( statusId == 6 ) {
                                    order.setServedTimestamp(orderHistoryItem.getTimestamp());

                                } else if ( statusId == 7 ) {
                                    order.setRemittedTimestamp(orderHistoryItem.getTimestamp());

                                }
                            }

                            if ( order != null ) {
                                orders.add(order);
                            }
                            orders.addAll(cachedOrders);

                            orderHistoryAdapter = new OrderHistoryAdapter(OrderHistoryActivity.this, orders, historyType);

                            orderHistoryAdapter.setOnClickedRemit(orderHistoryAdapterOnClickedRemit);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    historyRecyclerView.setLayoutManager(new LinearLayoutManager(OrderHistoryActivity.this));

                                    historyRecyclerView.setAdapter(orderHistoryAdapter);
                                }
                            });
                        }

                    }
                });
    }

    private OrderHistoryAdapter.OnClickedRemit orderHistoryAdapterOnClickedRemit = new OrderHistoryAdapter.OnClickedRemit() {
        @Override
        public void onClickRemit(int orderId, String code, OrderHistoryAdapter.OnInputValid onInputValid) {

            Order order = databaseManager.getOrder(orderId);
            HttpConnection.doPost(new HttpConnection.Param[]{
                    new HttpConnection.Param("code", code),
                    new HttpConnection.Param("branch_id", order.getBranchId())
//                    new HttpConnection.Param("trip_id", order.getId())
            }, Constants.API_URL + "cashier/check_code", new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String responseBody = response.body().string();

                    System.out.println(responseBody);
                    MResponse<Integer> responseObject = gson.fromJson(responseBody,
                            new TypeToken<MResponse<Integer>>(){}.getType());

                    if ( !responseObject.isError() ) {
                        if ( responseObject.getResult() == 1 ) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onInputValid.onInputValid();
                                }
                            });
                            databaseManager.updateStatusOrder(orderId, Constants.ORDER_STATUS_CODE.REMITTED, true);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new AlertDialog.Builder(OrderHistoryActivity.this).setMessage("wrong cashier's code")
                                            .show();
                                }
                            });
                        }
                    }

                }
            });
        }
    };
}
