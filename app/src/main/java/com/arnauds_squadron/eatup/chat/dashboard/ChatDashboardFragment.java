package com.arnauds_squadron.eatup.chat.dashboard;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Chat;
import com.arnauds_squadron.eatup.utils.Constants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment that displays the list of active chats the user is part of
 */
public class ChatDashboardFragment extends Fragment {

    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;

    @BindView(R.id.rvChats)
    RecyclerView rvChats;

    @BindView(R.id.tvNoChats)
    TextView tvNoChats;

    private OnFragmentInteractionListener mListener;
    private List<Chat> chatList;
    private ChatDashboardAdapter chatAdapter;

    // Variable to keep track of the newest updated chat so we don't always reload the views
    private Date lastUpdated;
    // Boolean to ensure we only have one refresh runnable
    private boolean refreshRunnableNotStarted = false;
    // Handler to post the runnable on the Looper's queue every second
    private Handler updateHandler = new Handler();
    // Refresh runnable that refreshes the messages every second
    private Runnable refreshChatsRunnable = new Runnable() {
        @Override
        public void run() {
            getChatsAsync();
            updateHandler.postDelayed(this, Constants.CHAT_UPDATE_SPEED_MILLIS);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chat_dashboard, container, false);
        ButterKnife.bind(this, view);

        swipeContainer.setColorSchemeResources(R.color.toast_red);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getChatsAsync();
            }
        });
        chatList = new ArrayList<>();
        chatAdapter = new ChatDashboardAdapter(this, chatList);
        rvChats.setAdapter(chatAdapter);
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));
        startChatRefresh();

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
     * Called by the parent fragment to stop the runnable so the messages aren't being refreshed
     * after the user is logged out
     */
    public void stopRefreshingMessages() {
        updateHandler.removeCallbacks(refreshChatsRunnable);
        refreshRunnableNotStarted = false;
    }

    /**
     * Communicates with the parent ChatFragment through the listener to open the selected Chat
     */
    public void openChat(Chat chat) {
        mListener.openChatFragment(chat);
    }

    /**
     * Queries the ParseServer for all the chats that the current user is a member of, adding
     * them to the chatList and notifying the adapter
     */
    public void getChatsAsync() {
        Chat.Query query = new Chat.Query();
        ParseUser user = Constants.CURRENT_USER;
        query.newestFirst().matchesUser(user).findInBackground(new FindCallback<Chat>() {
            @Override
            public void done(List<Chat> objects, ParseException e) {
                if (e == null && objects != null && objects.size() > 0) {
                    if (!objects.get(0).getUpdatedAt().equals(lastUpdated)) {
                        chatList.clear();
                        chatList.addAll(objects);
                        chatAdapter.notifyDataSetChanged();
                        lastUpdated = objects.get(0).getUpdatedAt();
                        tvNoChats.setVisibility(View.INVISIBLE);
                    }
                } else if (e != null) {
                    e.printStackTrace();
                } else {
                    tvNoChats.setVisibility(View.VISIBLE);
                }
                swipeContainer.setRefreshing(false);
            }
        });
    }

    /**
     * Queries the user's chats once and starts the runnable to continuously update the chat list
     */
    private void startChatRefresh() {
        getChatsAsync();
        if (!refreshRunnableNotStarted) { // only one runnable
            refreshChatsRunnable.run();
            refreshRunnableNotStarted = true;
        }
    }

    public interface OnFragmentInteractionListener {
        /**
         * Opens the selected chat in the MessengerFragment
         */
        void openChatFragment(Chat chat);
    }
}
