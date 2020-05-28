package mapabea.mapabea;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
public class OrderAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<Order> orders;


    public OnClickedOrder onClickOrder;

    public void setOnClickOrder(OnClickedOrder onClickOrder) {
        this.onClickOrder = onClickOrder;
    }

    interface OnClickedOrder {
        void onClickOrder(Order order);
    }
    public OrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_item, viewGroup, false);
        return new OrderAdapter.OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        OrderAdapter.OrderViewHolder itemViewHolder = (OrderAdapter.OrderViewHolder) viewHolder;

        itemViewHolder.consigneeAddressTextView.setText(orders.get(i).getOrderConsigneeAddress());
        itemViewHolder.branchNameTextView.setText(orders.get(i).getBranchName());
        itemViewHolder.orderNumberTextView.setText(orders.get(i).getOrderNumber());

        int client_id = orders.get(i).getClient_id();

        if ( client_id == 6 ) {



            itemViewHolder.clientImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.chowking));
        }
        else if ( client_id == 5 ) {

            itemViewHolder.clientImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.inasal));
        }
        else if ( client_id == 3 ) {

            itemViewHolder.clientImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.jollibee));
        }

        itemViewHolder.rootRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("testet: " + orders.get(i).getId());
                onClickOrder.onClickOrder(orders.get((i)));
            }
        });
    }


    @Override
    public int getItemCount() {
        return orders.size();
    }




    public static class OrderViewHolder extends RecyclerView.ViewHolder {


        public LinearLayout rootRelativeLayout;
        public TextView consigneeAddressTextView;
        public TextView orderNumberTextView;
        public TextView branchNameTextView;
        public ImageView clientImageView;
        public OrderViewHolder(View itemView) {
            super(itemView);

            rootRelativeLayout = itemView.findViewById(R.id.rootRelativeLayout);
            consigneeAddressTextView = itemView.findViewById(R.id.consigneeAddressTextView);
            orderNumberTextView = itemView.findViewById(R.id.orderNumberTextView);
            clientImageView = itemView.findViewById(R.id.clientImageView);
            branchNameTextView = itemView.findViewById(R.id.branchNameTextView);
        }
    }
}
