package sg.edu.np.mad.greencycle.Fragments.Chatbot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.*;
import sg.edu.np.mad.greencycle.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatBotFragment extends Fragment {

    private RecyclerView chatRecyclerView;
    private EditText userInputEditText;
    private Button sendButton;
    private OkHttpClient client;
    private ChatAdapter chatAdapter;
    private static final String URL = "https://bd27-119-74-171-109.ngrok-free.app/ask"; // Change if needed

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public ChatBotFragment() {
        // Required empty public constructor
    }

    public static ChatBotFragment newInstance(String param1, String param2) {
        ChatBotFragment fragment = new ChatBotFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Handle fragment arguments if needed
        }

        client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_bot, container, false);

        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        userInputEditText = view.findViewById(R.id.userInputEditText);
        sendButton = view.findViewById(R.id.sendButton);

        chatAdapter = new ChatAdapter();
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(v -> {
            String userInput = userInputEditText.getText().toString().trim();
            if (!userInput.isEmpty()) {
                sendMessage(userInput);
            }
        });

        return view;
    }

    private void sendMessage(String message) {
        chatAdapter.addMessage(new ChatMessage(message, true));
        userInputEditText.setText("");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("input", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonBody.toString()
        );

        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> chatAdapter.addMessage(new ChatMessage("Error: " + e.getMessage(), false)));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String botResponse = jsonResponse.getString("response");
                        getActivity().runOnUiThread(() -> chatAdapter.addMessage(new ChatMessage(botResponse, false)));
                    } catch (JSONException e) {
                        getActivity().runOnUiThread(() -> chatAdapter.addMessage(new ChatMessage("Error parsing response: " + e.getMessage(), false)));
                    }
                } else {
                    getActivity().runOnUiThread(() -> chatAdapter.addMessage(new ChatMessage("Error: " + response.message(), false)));
                }
            }
        });
    }

    private static class ChatMessage {
        String message;
        boolean isUser;

        ChatMessage(String message, boolean isUser) {
            this.message = message;
            this.isUser = isUser;
        }
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
        private List<ChatMessage> messages = new ArrayList<>();

        @Override
        public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_item, parent, false);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ChatViewHolder holder, int position) {
            ChatMessage message = messages.get(position);
            holder.messageTextView.setText(message.message);
            if (message.isUser) {
                holder.messageTextView.setBackgroundResource(R.drawable.chatbot_bubble);
                holder.messageTextView.setLayoutParams(new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                ));
                ((RelativeLayout.LayoutParams) holder.messageTextView.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_END);
            } else {
                holder.messageTextView.setBackgroundResource(R.drawable.bot_bubble);
                holder.messageTextView.setLayoutParams(new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                ));
                ((RelativeLayout.LayoutParams) holder.messageTextView.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_START);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        void addMessage(ChatMessage message) {
            messages.add(message);
            notifyItemInserted(messages.size() - 1);
            chatRecyclerView.scrollToPosition(messages.size() - 1);
        }

        class ChatViewHolder extends RecyclerView.ViewHolder {
            TextView messageTextView;

            ChatViewHolder(View itemView) {
                super(itemView);
                messageTextView = itemView.findViewById(R.id.messageTextView);
            }
        }
    }
}
