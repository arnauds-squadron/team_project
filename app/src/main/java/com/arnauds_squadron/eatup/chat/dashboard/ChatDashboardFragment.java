package com.arnauds_squadron.eatup.chat.dashboard;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Chat;
import com.arnauds_squadron.eatup.utils.Constants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
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

    private OnFragmentInteractionListener mListener;
    private List<Chat> chatList;
    private ChatDashboardAdapter chatAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_dashboard, container, false);
        ButterKnife.bind(this, view);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                getChatsAsync();
            }
        });

        // TODO: standardize
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        chatList = new ArrayList<>();
        chatAdapter = new ChatDashboardAdapter(this, chatList);
        rvChats.setAdapter(chatAdapter);
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));
        getChatsAsync();

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
                if (e == null) {
                    chatList.clear();
                    chatList.addAll(objects);
                    chatAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(), "Could not load chats",
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                swipeContainer.setRefreshing(false);
            }
        });
    }

    public interface OnFragmentInteractionListener {
        /**
         * Opens the selected chat in the MessengerFragment
         */
        void openChatFragment(Chat chat);
    }
}
