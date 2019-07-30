package com.arnauds_squadron.eatup.chat.messaging;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.arnauds_squadron.eatup.utils.EndlessRecyclerViewScrollListener;
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
public class MessengerFragment extends Fragment {

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

    // The newest message obtained by the Message queries
    private Message newestMessage;
    // Boolean to ensure we only have one refresh runnable
    private boolean refreshRunnableNotStarted = false;
    // Handler to post the runnable on the Looper's queue every second
    private Handler updateHandler = new Handler();
    // Refresh runnable that refreshes the messages every second
    private Runnable refreshMessageRunnable = new Runnable() {
        @Override
        public void run() {
            refreshMessagesAsync();
            updateHandler.postDelayed(this, Constants.CHAT_UPDATE_SPEED_MILLIS);
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

        EndlessRecyclerViewScrollListener scrollListener =
                new EndlessRecyclerViewScrollListener(layoutManager) {
                    @Override
                    public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                        Message.Query messageQuery = new Message.Query();
                        messageQuery.newestFirst().matchesChat(chat).getPrevious(messages.size());

                        messageQuery.findInBackground(new FindCallback<Message>() {
                            @Override
                            public void done(List<Message> objects, ParseException e) {
                                if (e == null && objects != null) {
                                    for (int i = 0; i < objects.size(); i++) {
                                        Message message = objects.get(i);
                                        addMessage(message);
                                    }
                                } else if (e != null) {
                                    Toast.makeText(getActivity(), "Could not load more messages",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                };

        rvMessages.addOnScrollListener(scrollListener);

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
        stopRefreshingMessages();
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
            refreshMessagesAsync();

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
     * Called by the parent fragment to stop the runnable so the messages aren't being refreshed
     * after the fragment is closed.
     */
    public void stopRefreshingMessages() {
        updateHandler.removeCallbacks(refreshMessageRunnable);
        refreshRunnableNotStarted = false;
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
                        appendMessage(message);
                        etMessage.setText(null);
                    } else {
                        Toast.makeText(getContext(), "Could not send message",
                                Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });

            chat.justUpdated();
            chat.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        mListener.updateDashboardChats();
                    } else {
                        Toast.makeText(getContext(), "Could not update in chat",
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
    private void refreshMessagesAsync() {
        Message.Query messageQuery = new Message.Query();
        messageQuery.newestFirst().matchesChat(chat);

        messageQuery.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> objects, ParseException e) {
                if (e == null && objects != null && objects.size() > 0) {
                    String newestId = newestMessage != null ? newestMessage.getObjectId() : null;
                    newestMessage = objects.get(0);
                    // get the index of the newest message
                    int i = objects.size() - 1;
                    if (newestId != null) {
                        boolean newestFound = false;
                        while (i >= 0 && !newestFound) {
                            if (objects.get(i).getObjectId().equals(newestId))
                                newestFound = true;
                            i--;
                        }
                    }
                    // append any messages found after the newest message
                    for (int j = i; j >= 0; j--) {
                        Message message = objects.get(j);
                        appendMessage(message);
                    }
                } else if (e != null) {
                    Toast.makeText(getActivity(), "Could not load messages",
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Helper method to append the message to the bottom of the chat's RecyclerView. Also scrolls
     * to the bottom of the chat.
     *
     * @param message The message to append
     */
    private void addMessage(Message message) {
        messages.add(message);
        messageAdapter.notifyItemInserted(messages.size() - 1);
    }

    /**
     * Helper method to append the message to the bottom of the chat's RecyclerView. Also scrolls
     * to the bottom of the chat.
     *
     * @param message The message to append
     */
    private void appendMessage(Message message) {
        messages.add(0, message);
        messageAdapter.notifyItemInserted(0);
        rvMessages.scrollToPosition(0);
        mListener.handleNotification();
    }

    /**
     * Helper method to clear the messages from the message list. Also notifies the adapter.
     */
    private void resetMessages() {
        messages.clear();
        messageAdapter.notifyDataSetChanged();
        newestMessage = null;
    }

    /**
     * Listener interface to tell the ChatFragment to switch to the dashboard when the user
     * hits the back button
     */
    public interface OnFragmentInteractionListener {
        /**
         * Notifies the parent ChatFragment to switch to the ChatDashboardFragment
         */
        void goToDashboard();

        /**
         * Updates the chats in the ChatDashboardFragment to have the correct updated time. Updates
         * the chats whenever a message is sent.
         */
        void updateDashboardChats();

        /**
         * Notifies the parent ChatFragment to update the number of new messages next to the chat
         * icon
         */
        void handleNotification();
    }
}
