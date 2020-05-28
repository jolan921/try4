package mapabea.mapabea;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter {

    private List<Order> orders;

    private OnClickedRemit onClickedRemit;

    private Context context;

    private int historyType;

    public OrderHistoryAdapter(Context context, List<Order> orders, int historyType) {
        this.orders = orders;
        this.context = context;
        this.historyType = historyType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.history_item_layout, viewGroup, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        OrderViewHolder orderViewHolder = (OrderViewHolder) viewHolder;

        Order order = orders.get(i);

        if ( order.getStoreInTimestamp() == null ) {
            orderViewHolder.storeinTimestampTextView.setText("N/A");
        } else {
            orderViewHolder.storeinTimestampTextView.setText(order.getStoreInTimestamp());
        }
        if ( order.getStoreOutTimestamp() == null ) {
            orderViewHolder.storeoutTimestampTextView.setText("N/A");
        } else {
            orderViewHolder.storeoutTimestampTextView.setText(order.getStoreOutTimestamp());
        }
        if ( order.getServedTimestamp() == null ) {
            orderViewHolder.servedTimestampTextView.setText("N/A");
        } else {
            orderViewHolder.servedTimestampTextView.setText(order.getServedTimestamp());
        }
        if ( order.getRemittedTimestamp() == null ) {
            orderViewHolder.remittanceTimestampTextView.setText("N/A");
        } else {
            orderViewHolder.remittanceTimestampTextView.setText(order.getRemittedTimestamp());
        }
        if ( order.getVicinityInTimestamp() == null ) {
            orderViewHolder.vicinityTimestampTextView.setText("N/A");
        } else {
            orderViewHolder.vicinityTimestampTextView.setText(order.getVicinityInTimestamp());
        }

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

        orderViewHolder.ticketTextView.setText(order.getOrderNumber());
        orderViewHolder.storeNameTextView.setText(order.getBranchName());
        orderViewHolder.amountEditText.setText(order.getAmount());
        orderViewHolder.paymentModeTextView.setText(paymentModeText);

        if ( historyType == Constants.HISTORY_TYPE_CODE.CLOSED_TICKET.getValue() ) {
            orderViewHolder.remittanceLinearLayout.setVisibility(View.GONE);

        } else if ( historyType == Constants.HISTORY_TYPE_CODE.FOR_REMITTANCE.getValue() ) {

//            if ( order.getPaymentMode() == 2 ) {
//                orderViewHolder.amountEditText.setVisibility(View.GONE);
//                orderViewHolder.remitButton.setText("CLOSE TICKET");
//            }
        } else if ( historyType == Constants.HISTORY_TYPE_CODE.ARCHIVE_TICKETS.getValue() ) {
            orderViewHolder.remittanceLinearLayout.setVisibility(View.GONE);
        }



        orderViewHolder.remitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder remittedMessage = new AlertDialog.Builder(context)
                        .setMessage("Remitted")
                        .setPositiveButton("OK", null);

                AlertDialog.Builder errorMessage =
                        new AlertDialog.Builder(context)
                                .setMessage("Please fill the code")
                                .setPositiveButton("OK", null);

                if (orderViewHolder.codeEditText.getText().length() > 0 && orderViewHolder.amountEditText.getText().length() > 0) {
                    onClickedRemit.onClickRemit(order.getId(), orderViewHolder.codeEditText.getText().toString(), new OnInputValid() {
                        @Override
                        public void onInputValid() {
                            remittedMessage.show();

                            orderViewHolder.remittanceLinearLayout.setVisibility(View.GONE);
                            orderViewHolder.remittanceTimestampTextView.setText(Utils.getStringifyCurrentDate());

                        }
                    });

                } else {
                    errorMessage.show();
                }
//                if ( order.getPaymentMode() == 1 ) {
//                    if (orderViewHolder.codeEditText.getText().length() > 0 && orderViewHolder.amountEditText.getText().length() > 0) {
//                        onClickedRemit.onClickRemit(order.getId(), orderViewHolder.codeEditText.getText().toString(), new OnInputValid() {
//                            @Override
//                            public void onInputValid() {
//                                remittedMessage.show();
//
//                                orderViewHolder.remittanceLinearLayout.setVisibility(View.GONE);
//                                orderViewHolder.remittanceTimestampTextView.setText(Utils.getStringifyCurrentDate());
//
//                            }
//                        });
//
//                    } else {
//                        errorMessage.show();
//                    }
//                } else {
//
//                    if (orderViewHolder.codeEditText.getText().length() > 0) {
//
//                        onClickedRemit.onClickRemit(order.getId(), orderViewHolder.codeEditText.getText().toString(), new OnInputValid() {
//                            @Override
//                            public void onInputValid() {
//
//                                orderViewHolder.remittanceLinearLayout.setVisibility(View.GONE);
//                                orderViewHolder.remittanceTimestampTextView.setText(Utils.getStringifyCurrentDate());
//                            }
//                        });
//
//                    } else {
//                        errorMessage.show();
//                    }
//                }
            }
        });


    }

    public OnClickedRemit getOnClickedRemit() {
        return onClickedRemit;
    }

    public void setOnClickedRemit(OnClickedRemit onClickedRemit) {
        this.onClickedRemit = onClickedRemit;
    }


    public interface OnInputValid {
        void onInputValid();
    }
    public interface OnClickedRemit {
        void onClickRemit(int orderId, String code, OnInputValid onInputValid);
    }
    @Override
    public int getItemCount() {
        return orders.size();
    }




    public static class OrderViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout remittanceLinearLayout;
        public Button remitButton;
        public TextView storeinTimestampTextView;
        public TextView storeoutTimestampTextView;
        public TextView vicinityTimestampTextView;
        public TextView servedTimestampTextView;
        public TextView remittanceTimestampTextView;
        public TextView storeNameTextView;
        public TextView ticketTextView;
        public EditText amountEditText;
        public EditText codeEditText;
        public TextView paymentModeTextView;
        public TextView amountTextView;

        public OrderViewHolder(View itemView) {
            super(itemView);


            remittanceLinearLayout = itemView.findViewById(R.id.remittanceLinearLayout);
            storeinTimestampTextView = itemView.findViewById(R.id.storeinTimestampTextView);
            storeoutTimestampTextView = itemView.findViewById(R.id.storeoutTimestampTextView);
            vicinityTimestampTextView = itemView.findViewById(R.id.vicinityTimestampTextView);
            remittanceTimestampTextView = itemView.findViewById(R.id.remittanceTimestampTextView);
            storeNameTextView = itemView.findViewById(R.id.storeNameTextView);
            servedTimestampTextView = itemView.findViewById(R.id.servedTimestampTextView);
            ticketTextView = itemView.findViewById(R.id.ticketTextView);
            amountEditText = itemView.findViewById(R.id.amountEditText);
            codeEditText = itemView.findViewById(R.id.codeEditText);
            remitButton = itemView.findViewById(R.id.remitButton);
            paymentModeTextView = itemView.findViewById(R.id.paymentModeTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);


        }
    }
}
