package com.arnauds_squadron.eatup.chat.dashboard;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Chat;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment that asks the user to input the cuisine of the restaurant they are visiting or of the
 * food they are cooking.
 */
public class ChatDashboardFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    @BindView(R.id.rvChats)
    RecyclerView rvChats;

    // List of the different active conversations
    private List<Chat> chatList;

    // Adapter instance to handle adding tag items to the ListView
    private ChatDashboardAdapter chatAdapter;

    public static ChatDashboardFragment newInstance() {
        Log.i("Dashboard", "dashbaord new instance");
        Bundle args = new Bundle();
        ChatDashboardFragment fragment = new ChatDashboardFragment();
        fragment.setArguments(args);
        return fragment;
    }

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

        chatList = new ArrayList<>();
        chatAdapter = new ChatDashboardAdapter(this, chatList);
        rvChats.setAdapter(chatAdapter);

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

    public void openChat(Chat chat) {
        mListener.openChatFragment(chat);
    }

    public interface OnFragmentInteractionListener {
        /**
         * Opens the selected chat in a newly created ChatActivity
         */
        void openChatFragment(Chat chat);
    }
}
