package mapabea.mapabea;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.reflect.TypeToken;

import java.net.URISyntaxException;
import java.util.List;

public class MessengerActivity extends BaseActivity {

    private RecyclerView messageRecyclerView;
    private ChatItemAdapter chatItemAdapter;
    private EditText messageEditText;
    private ImageView sendButton;
    List<Message> messages;
    private Socket socket;
    {
        try {
            socket = IO.socket("http://210.14.16.68:1235");
        } catch (URISyntaxException e) {}
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initialize() {
        super.initialize();

        socket.connect();
        socket.on("history_broadcast", onNewMessage);
        socket.on("message_broadcast", onMessageBroadcast);
        socket.emit("history_receive", sharedPreferences.getInt(Constants.KEY_DRIVER_ID, -1));

        messages = databaseManager.getMessages(Constants.MESSAGE_TYPE_CODE.SMS);

        chatItemAdapter = new ChatItemAdapter(this, messages);

        messageRecyclerView.setLayoutManager(new LinearLayoutManager(MessengerActivity.this));
        messageRecyclerView.setAdapter(chatItemAdapter);

    }

    @Override
    protected void initializeViews() {

        messageRecyclerView = findViewById(R.id.messageRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
    }

    @Override
    public void onBackPressed() {
        super.superOnBackPressed();
    }

    @Override
    protected void initializeListeners() {
        sendButton.setOnClickListener(sendButtonOnClickListener);
    }

    private View.OnClickListener sendButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Message message = new Message();
            message.setTimestamp(Utils.getStringifyCurrentDate());
            message.setMessage(messageEditText.getText().toString());
            message.setHub_recepient_id(sharedPreferences.getInt(Constants.KEY_HUB_ID, -1));
            message.setSender_id(sharedPreferences.getInt(Constants.KEY_DRIVER_ID, -1));

            messages.add(message);
            chatItemAdapter.notifyDataSetChanged();
            messageRecyclerView.scrollToPosition(messages.size() - 1);

            socket.emit("message_send", gson.toJson(message));
            messageEditText.setText("");
        }
    };
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    System.out.println(args[0]);
                    List<Message> responseObject = gson.fromJson(args[0].toString(), new TypeToken<List<Message>>(){}.getType());

                    messages.addAll(responseObject);
                    chatItemAdapter.notifyDataSetChanged();
                    messageRecyclerView.scrollToPosition(messages.size() - 1);

                }
            });
        }
    };

    private Emitter.Listener onMessageBroadcast = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Message responseObject = gson.fromJson(args[0].toString(), new TypeToken<Message>(){}.getType());

                    if ( responseObject.getRider_recepient_id() == sharedPreferences.getInt(Constants.KEY_DRIVER_ID, -1) ) {

                        messages.add(responseObject);
                        chatItemAdapter.notifyDataSetChanged();
                        messageRecyclerView.scrollToPosition(messages.size() - 1);
                    }

                }
            });
        }
    };
}
