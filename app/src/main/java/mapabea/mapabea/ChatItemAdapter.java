package mapabea.mapabea;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class ChatItemAdapter extends RecyclerView.Adapter {

    private List<Message> messages;

    private Context context;

    public ChatItemAdapter(Context context, List<Message> messages) {
        this.messages = messages;
        this.context = context;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_item_layout, viewGroup, false);
        return new ChatItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        ChatItemHolder orderViewHolder = (ChatItemHolder) viewHolder;

        Message message = messages.get(i);

        orderViewHolder.messageTextview.setText(message.getMessage());

        String timestamp = message.getTimestamp();

        orderViewHolder.messageTimestampTextview.setText(timestamp);
//        orderViewHolder.nameTextview.setText(timestamp);

        BaseActivity baseActivity =(BaseActivity) context;

        if ( message.getSender_id() == baseActivity.sharedPreferences.getInt(Constants.KEY_DRIVER_ID, -1) ) {

            orderViewHolder.rootLinearLayout.setGravity(Gravity.RIGHT);
            orderViewHolder.messageLinearLayout.setBackgroundColor(context.getResources().getColor(R.color.message_send));
            orderViewHolder.nameTextview.setText("You");

        } else {
            orderViewHolder.rootLinearLayout.setGravity(Gravity.LEFT);
            orderViewHolder.messageLinearLayout.setBackgroundColor(context.getResources().getColor(R.color.message_receive));
            orderViewHolder.nameTextview.setText("Dispatcher");

        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }




    public static class ChatItemHolder extends RecyclerView.ViewHolder {

        public LinearLayout messageLinearLayout;
        public LinearLayout rootLinearLayout;
        public TextView messageTextview;
        public TextView messageTimestampTextview;
        public TextView nameTextview;
        public CardView messageCardview;

        public ChatItemHolder(View itemView) {
            super(itemView);


            rootLinearLayout = itemView.findViewById(R.id.rootLinearLayout);
            messageLinearLayout = itemView.findViewById(R.id.messageLinearLayout);
            messageTextview = itemView.findViewById(R.id.messageTextview);
            messageTimestampTextview = itemView.findViewById(R.id.messageTimestampTextview);
            nameTextview = itemView.findViewById(R.id.nameTextview);
            messageCardview = itemView.findViewById(R.id.messageCardview);


        }
    }
    public void updateMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }
}
