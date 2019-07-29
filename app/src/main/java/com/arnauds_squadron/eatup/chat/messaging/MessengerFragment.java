package com.arnauds_squadron.eatup.chat.messaging;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Chat;
import com.arnauds_squadron.eatup.models.Message;
import com.arnauds_squadron.eatup.utils.Constants;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment to display a single chat with messages from different users. Also allows sending of
 * messages and periodically updates the messages in real time
 */
//TODO: typing a message in chat hides the top part of the fragment
public class MessengerFragment extends Fragment {
    // 1 second
    private static final int CHAT_UPDATE_SPEED_MILLIS = 1000;

    @BindView(R.id.tvChatName)
    TextView tvChatName;

    @BindView(R.id.etMessage)
    EditText etMessage;

    @BindView(R.id.rvMessages)
    RecyclerView rvMessages;

    private OnFragmentInteractionListener mListener;
    // The current chat that is open
    private Chat chat;
    private List<Message> messages;
    private MessageAdapter messageAdapter;
    // Total message count, including other chats
    private int totalMessageCount;
    // Boolean to ensure we only have one refresh runnable
    private boolean refreshRunnableNotStarted = false;
    // Handler to post the runnable on the Looper's queue every second
    private Handler chatUpdateHandler = new Handler();
    // Refresh runnable that refreshes the messages every second
    private Runnable refreshMessageRunnable = new Runnable() {
        @Override
        public void run() {
            refreshMessages();
            // TODO: remove
            Log.i("refresh thread", Thread.currentThread().toString());
            chatUpdateHandler.postDelayed(this, CHAT_UPDATE_SPEED_MILLIS);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());

        Bundle bundle = this.getArguments();

        if (bundle != null) // Get the data saved in the newInstance() method
            chat = bundle.getParcelable("chat");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messenger, container, false);
        ButterKnife.bind(this, view);

        Context context = getContext();
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(context, Constants.CURRENT_USER, messages);
        rvMessages.setAdapter(messageAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setReverseLayout(true);
        rvMessages.setLayoutManager(layoutManager);

        // If the user hits the send button on the soft keyboard, send the message
        etMessage.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    /**
     * Called in onCreate to bind the this child fragment to its parent, so the listener
     * can be used
     *
     * @param parent The parent fragment
     */
    public void onAttachToParentFragment(Fragment parent) {
        try {
            mListener = (OnFragmentInteractionListener) parent;
        } catch (ClassCastException e) {
            throw new ClassCastException(parent.toString() + " must implement the interface");
        }
    }

    /**
     * Communicates the the parent ChatFragment to go back to the dashboard when the user
     * hits the back button
     */
    @OnClick(R.id.ibBack)
    public void goBackToDashboard() {
        mListener.goToDashboard();
    }

    /**
     * Method called by the parent fragment to set the new chat that the user is in. Also
     * clears the original message list in case we are replacing an old chat.
     *
     * @param chat The new chat object
     */
    public void setChat(Chat chat) {
        if (!chat.equals(this.chat)) { // only clear if there was a chat loaded before
            this.chat = chat;
            resetMessages();
            refreshMessages();

            chat.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    tvChatName.setText(object.getString("name"));
                }
            });
        }

        if (!refreshRunnableNotStarted) { // only one runnable
            refreshMessageRunnable.run();
            refreshRunnableNotStarted = true;
        }
    }

    /**
     * Saves the message typed by the user to the Parse Server and appends the message to the
     * current list.
     * Does not allow for empty spaces as a message.
     */
    private void sendMessage() {
        String data = etMessage.getText().toString();

        if (!data.trim().isEmpty()) {
            final Message message = new Message();
            message.setSender(Constants.CURRENT_USER);
            message.setContent(data);
            message.setChatId(chat.getObjectId());
            message.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        totalMessageCount++;
                        appendMessage(message);
                        etMessage.setText(null);
                    } else {
                        Toast.makeText(getContext(), "Could not send message",
                                Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * Searches the Messages table for a0ny new messages that match the current chat's objectId.
     * Appends messages, does not clear and addAll()
     */
    private void refreshMessages() {
        Message.Query messageQuery = new Message.Query();
        messageQuery.newestFirst().matchesChat(chat).findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> objects, ParseException e) {
                if (e == null) {
                    int newMessageCount = objects.size() - totalMessageCount - 1;
                    for (int i = newMessageCount; i >= 0; i--) {
                        Message message = objects.get(i);
                        appendMessage(message);
                    }
                    totalMessageCount = objects.size();
                }
            }
        });
    }

    /**
     * Helper method to append the message to the bottom of the chat's RecyclerView. Also scrolls
     * to the bottom of the chat.
     * // TODO: scroll to position on new message?
     *
     * @param message The message to append
     */
    private void appendMessage(Message message) {
        messages.add(0, message);
        messageAdapter.notifyItemInserted(0);
        rvMessages.scrollToPosition(0);
    }

    /**
     * Helper method to clear the messages from the message list. Also notifies the adapter.
     */
    private void resetMessages() {
        messages.clear();
        messageAdapter.notifyDataSetChanged();
        totalMessageCount = 0;
    }

    /**
     * Listener interface to tell the ChatFragment to switch to the dashboard when the user
     * hits the back button
     */
    public interface OnFragmentInteractionListener {
        void goToDashboard();
    }
}
