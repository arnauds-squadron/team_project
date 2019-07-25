package com.arnauds_squadron.eatup.chat.messaging;

import android.content.Context;
import android.os.AsyncTask;
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
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
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

    private static final int CHAT_UPDATE_SPEED_MILLIS = 1000;

    @BindView(R.id.tvChatName)
    TextView tvChatName;

    @BindView(R.id.etMessage)
    EditText etMessage;

    @BindView(R.id.rvMessages)
    RecyclerView rvMessages;

    private OnFragmentInteractionListener mListener;

    private Chat chat;
    private List<Message> messages;
    private MessageAdapter messageAdapter;

    private Handler chatUpdateHandler = new Handler();
    private Runnable refreshMessageRunnable = new Runnable() {
        @Override
        public void run() {
            refreshMessages();
            chatUpdateHandler.postDelayed(this, CHAT_UPDATE_SPEED_MILLIS);
        }
    };

    public static MessengerFragment newInstance(Chat chat) {
        Bundle args = new Bundle();
        args.putParcelable("chat", chat);
        MessengerFragment fragment = new MessengerFragment();
        fragment.setArguments(args);
        return fragment;
    }

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
        //TODO move to new thread?
        messageAdapter = new MessageAdapter(context, ParseUser.getCurrentUser(), messages);
        rvMessages.setAdapter(messageAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setReverseLayout(true);
        rvMessages.setLayoutManager(layoutManager);

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

    @OnClick(R.id.ibBack)
    public void goBackToDashboard() {
        mListener.goToDashboard();
    }

    public void setChat(Chat chat) {
        this.chat = chat;
        new FetchChatNameAsyncTask(this).execute(chat);
        resetChat();
        refreshMessageRunnable.run();
    }

    private void resetChat() {
        messages.clear();
        messageAdapter.notifyDataSetChanged();
    }

    /**
     * Saves the message typed by the user to the chat and saves the Message object to the
     * Parse server
     */
    private void sendMessage() {
        String data = etMessage.getText().toString();

        if (!data.isEmpty()) {
            final Message message = new Message();
            message.setSender(ParseUser.getCurrentUser());
            message.setContent(data);
            message.setChat(chat);
            message.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        chat.addMessageId(message.getObjectId(), chat.getMessages() == null);
                        chat.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    messages.add(0, message);
                                    messageAdapter.notifyItemInserted(0);
                                    rvMessages.scrollToPosition(0);
                                    etMessage.setText(null);

                                    Toast.makeText(getContext(), "Created message on Parse",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Could not send message",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /**
     * Re-queries the Parse server to obtain the newest messages
     * // TODO: scroll to position on new message?
     * // TODO: don't always clear, just append new messages?
     */
    private void refreshMessages() {
        chat.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject object, ParseException e) {
                if (e == null) {
                    JSONArray messageArray = object.getJSONArray("messages");
                    List<String> idList = new ArrayList<>();
                    if (messageArray != null) {

                        for (int i = 0; i < messageArray.length(); i++) {
                            try {
                                idList.add(messageArray.getString(i));
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }

                        Message.Query messageQuery = new Message.Query();
                        messageQuery
                                .newestFirst()
                                .whereContainedIn("objectId", idList);

                        messageQuery.findInBackground(new FindCallback<Message>() {
                            @Override
                            public void done(List<Message> objects, ParseException e) {
                                if (e == null && objects.size() > messages.size()) {
                                    messages.clear();
                                    messages.addAll(objects);
                                    messageAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public interface OnFragmentInteractionListener {
        void goToDashboard();
    }

    /**
     * AsyncTask to get the chat name from the ParseServer
     */
    private static class FetchChatNameAsyncTask extends AsyncTask<Chat, Void, Void> {

        private WeakReference<MessengerFragment> context;
        private String chatName;

        FetchChatNameAsyncTask(MessengerFragment context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Chat... chats) {
            Chat chat = chats[0];
            try {
                chatName = chat.fetchIfNeeded().get("name").toString();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            MessengerFragment fragment = context.get();

            if (fragment != null)
                fragment.tvChatName.setText(chatName);
        }
    }
}
