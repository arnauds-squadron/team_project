package com.arnauds_squadron.eatup.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.chat.dashboard.ChatDashboardFragment;
import com.arnauds_squadron.eatup.chat.messaging.MessengerFragment;
import com.arnauds_squadron.eatup.models.Chat;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Main messaging fragment that holds the dashboard with a list of the chats and also can switch
 * to another fragment containing the selected chat's messages
 */
public class ChatFragment extends Fragment implements
        ChatDashboardFragment.OnFragmentInteractionListener,
        MessengerFragment.OnFragmentInteractionListener {

    @BindView(R.id.flMessenger)
    FrameLayout flMessenger;

    @BindView(R.id.flDashboard)
    FrameLayout flDashboard;

    private OnFragmentInteractionListener mListener;

    private MessengerFragment messengerFragment;
    private ChatDashboardFragment dashboardFragment;

    private Chat chat;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, view);

        dashboardFragment = (ChatDashboardFragment)
                getChildFragmentManager().findFragmentById(R.id.dashboardFragment);

        messengerFragment = (MessengerFragment)
                getChildFragmentManager().findFragmentById(R.id.messengerFragment);

        return view;
    }

    /**
     * Attaches the listener to the MainActivity
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement the interface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Either opens the chat selected in the HomeFragment, or opens the chat selected
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(mListener != null) {
            Chat newChat = mListener.getSelectedChat();
            if(newChat != null)
                openChatFragment(newChat);
        }
    }

    /**
     * Overrides the ChatDashboardFragment interface
     * @param chat The chat object that the messenger fragment might display
     */
    @Override
    public void openChatFragment(Chat chat) {
        messengerFragment.setChat(chat);
        showMessengerFragment();
    }

    /**
     * Overrides the MessengerFragment interface
     */
    @Override
    public void goToDashboard() {
        showDashboardFragment();
    }

    private void showMessengerFragment() {
        flMessenger.setVisibility(View.VISIBLE);
        flDashboard.setVisibility(View.INVISIBLE);
    }

    private void showDashboardFragment() {
        flDashboard.setVisibility(View.VISIBLE);
        flMessenger.setVisibility(View.INVISIBLE);
    }

    public interface OnFragmentInteractionListener {
        Chat getSelectedChat();
    }
}
