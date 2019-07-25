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
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
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
        rvMessages.setLayoutManager(layoutManager);

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
        initializeChat();
    }

    // TODO: update messages periodically
    private static final int POLL_INTERVAL_MILLIS = 1000;
    Handler myHandler = new Handler();
    Runnable mRefreshMessagesRunnable = new Runnable() {
        @Override
        public void run() {
            refreshMessages();
            myHandler.postDelayed(this, POLL_INTERVAL_MILLIS);
        }
    };

    private void sendMessage() {
        String data = etMessage.getText().toString();

        if (!data.isEmpty()) {
            Message message = new Message();
            //TODO: move to new thread
            message.setSender(ParseUser.getCurrentUser());
            message.setContent(data);
            message.setChat(chat);
            message.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Toast.makeText(getContext(), "Successfully created message on Parse",
                            Toast.LENGTH_SHORT).show();
                }
            });

            messages.add(message);
            messageAdapter.notifyItemInserted(messages.size() - 1);
            etMessage.setText(null);
        }
    }

    /**
     * Re-queries the Parse server to obtain the newest messages
     * // TODO: scroll to position on new message?
     * // TODO: don't always clear, just append new messages?
     */
    private void refreshMessages() {
        // Construct query to execute
        Message.Query query = new Message.Query();

        query.setQueryLimit()
                .inOrder()
                .inChat(chat);

        query.findInBackground(new FindCallback<Message>() {
            public void done(List<Message> results, ParseException e) {
                if (e == null) {
                    messages.clear();
                    messages.addAll(results);
                    messageAdapter.notifyDataSetChanged(); // update adapter
                    rvMessages.scrollToPosition(0);
                } else {
                    Log.e("message", "Error Loading Messages" + e);
                }
            }
        });
    }

    /**
     * Initializes all the views in the MessengerFragment after the Chat object is received
     */
    private void initializeChat() {
        tvChatName.setText(chat.getName());

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

        refreshMessages();
    }

    public interface OnFragmentInteractionListener {
        void goToDashboard();
    }
}
