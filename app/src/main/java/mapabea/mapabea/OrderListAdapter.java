package mapabea.mapabea;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class OrderListAdapter extends RecyclerView.Adapter {

    private List<Order> orders;

    private OnClickedItem onClickeditem;

    private Context context;

    public OrderListAdapter(Context context, List<Order> orders) {
        this.orders = orders;
        this.context = context;
        System.out.println("ORDER SIZE ORDERLISTADAPTER: " + orders.size());
    }


    public void updateOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_item_layout, viewGroup, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        OrderViewHolder orderViewHolder = (OrderViewHolder) viewHolder;

        Order order = orders.get(i);


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

        orderViewHolder.orderNumberTextView.setText(order.getOrderNumber());
        orderViewHolder.customerAddressTextView.setText(order.getOrderConsigneeAddress());
        orderViewHolder.customerNameTextView.setText(order.getOrderOriginalConsigneeName());
        orderViewHolder.orderAmountTextView.setText(order.getAmount() + "");
        orderViewHolder.orderDeliveryTimeTextView.setText(order.getDeliveryTime());
        orderViewHolder.orderPaymentOptionTextView.setText(paymentModeText);
        orderViewHolder.storeAddressTextView.setText(order.getBranchName());

        orderViewHolder.riderOutButton.setEnabled(order.getStoreInTimestamp() != null);


        orderViewHolder.riderOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClickeditem.onClickItem(order.getId());
            }
        });


    }

    public void setOnClickeditem(OnClickedItem onClickeditem) {
        this.onClickeditem = onClickeditem;
    }


    public interface OnClickedItem {
        void onClickItem(int orderId);
    }
    @Override
    public int getItemCount() {
        return orders.size();
    }




    public static class OrderViewHolder extends RecyclerView.ViewHolder {


        private TextView orderNumberTextView;
        private TextView storeAddressTextView;
        private TextView customerNameTextView;
        private TextView customerAddressTextView;
        private TextView orderAmountTextView;
        private TextView orderDeliveryTimeTextView;
        private TextView orderPaymentOptionTextView;

        private Button riderOutButton;
        public TextView ticketNumberTextView;
        private LinearLayout ticketNumberRootLinearLayout;

        public OrderViewHolder(View itemView) {
            super(itemView);

            orderNumberTextView = itemView.findViewById(R.id.orderNumberTextView);
            customerAddressTextView = itemView.findViewById(R.id.customerAddressTextView);
            customerNameTextView = itemView.findViewById(R.id.customerNameTextView);
            storeAddressTextView = itemView.findViewById(R.id.storeAddressTextView);
            orderAmountTextView = itemView.findViewById(R.id.orderAmountTextView);
            orderDeliveryTimeTextView = itemView.findViewById(R.id.orderDeliveryTimeTextView);
            orderPaymentOptionTextView = itemView.findViewById(R.id.orderPaymentOptionTextView);
            riderOutButton = itemView.findViewById(R.id.riderOutButton);

        }
    }

}
