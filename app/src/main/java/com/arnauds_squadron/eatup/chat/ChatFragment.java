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

    private boolean isVisibleToUser;
    private int notifications = 0;
    private OnFragmentInteractionListener mListener;
    private ChatDashboardFragment dashboardFragment;
    private MessengerFragment messengerFragment;

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
     * Attaches the MainActivity listener to the MainActivity
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
        this.isVisibleToUser = isVisibleToUser;

        if (mListener != null) {
            Chat newChat = mListener.getSelectedChat();
            if (newChat != null) // open chat otherwise stay on dashboard if its null
                openChatFragment(newChat);
            if (isVisibleToUser) { // handle message notifications
                notifications = 0;
                mListener.updateMessageNotifications(0);
            }
        }
    }

    /**
     * Overrides the ChatDashboardFragment interface
     * <p>
     * If the user selects a chat in the ChatDashboardFragment, it sets the MessengerFragment's
     * open chat to the selected one, and displays the MessengerFragment.
     *
     * @param chat The chat object that the messenger fragment might display
     */
    @Override
    public void openChatFragment(Chat chat) {
        messengerFragment.setChat(chat);
        showMessengerFragment();
    }

    /**
     * Overrides the MessengerFragment interface
     * <p>
     * If the user hits the back button on the MessengerFragment, the ChatDashboardFragment is
     * shown
     */
    @Override
    public void goToDashboard() {
        showDashboardFragment();
        notifications = 0;
    }

    /**
     * Updates the chat list in the ChatDashboardFragment. Called after a new event is created,
     * since a new chat is also created
     */
    @Override
    public void updateDashboardChats() {
        dashboardFragment.getChatsAsync();
    }

    @Override
    public void handleNotification() {
        if (!isVisibleToUser) {
            notifications++;
            mListener.updateMessageNotifications(notifications);
        }
    }

    /**
     * Method to be called by the parent activity to handle back presses. Goes back to the
     * dashboard fragment if the messenger fragment is selected.
     *
     * @return true if we moved back to the dashboard fragment, false if we were already on it
     */
    public boolean onBackPressed() {
        if (flMessenger.getVisibility() == View.VISIBLE) {
            showDashboardFragment();
            return true;
        } else {
            return false;
        }
    }

    public void stopUpdatingMessages() {
        messengerFragment.stopRefreshingMessages();
    }

    /**
     * Sets the FrameLayout holding the MessengerFragment to visible and the FrameLayout holding
     * the DashboardFragment to invisible
     */
    private void showMessengerFragment() {
        flMessenger.setVisibility(View.VISIBLE);
        flDashboard.setVisibility(View.INVISIBLE);
    }

    /**
     * Sets the FrameLayout holding the DashboardFragment to visible and the FrameLayout holding
     * the MessengerFragment to invisible
     */
    private void showDashboardFragment() {
        flDashboard.setVisibility(View.VISIBLE);
        flMessenger.setVisibility(View.INVISIBLE);
    }

    public interface OnFragmentInteractionListener {
        /**
         * Communicates with the MainActivity so when the user clicks on a chat and the ChatFragment
         * is visible, it can receive the selected Chat and open the MessengerFragment with the
         * correct Chat.
         *
         * @return The chat selected by the user in the HomeFragment, null if the user swiped
         * normally to the ChatFragment
         */
        Chat getSelectedChat();

        void updateMessageNotifications(int notifications);
    }
}
