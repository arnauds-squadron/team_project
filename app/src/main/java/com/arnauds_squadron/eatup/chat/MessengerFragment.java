package com.arnauds_squadron.eatup.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.chat.dashboard.ChatDashboardFragment;
import com.arnauds_squadron.eatup.models.Chat;
import com.arnauds_squadron.eatup.navigation.NoSwipingPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Main messaging fragment that holds the dashboard with a list of the chats and also can switch
 * to another fragment containing the selected chat's messages
 */
public class MessengerFragment extends Fragment implements
        ChatDashboardFragment.OnFragmentInteractionListener {

    @BindView(R.id.viewPager)
    NoSwipingPagerAdapter viewPager;

    private OnFragmentInteractionListener mListener;

    private ChatFragmentPagerAdapter adapter;

    private Chat selectedChat;

    public static MessengerFragment newInstance() {
        return new MessengerFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messenger, container, false);
        ButterKnife.bind(this, view);

        adapter = new ChatFragmentPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        setActiveChatFragment();

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
     * Sets the active chat fragment if the user selected one from the HomeFragment, otherwise it
     * goes to the DashboardFragment
     */
    public void setActiveChatFragment() {
        Chat selectedChat = mListener.getSelectedChat();

        if (selectedChat != null) {
            adapter.setChat(selectedChat);
            viewPager.setCurrentItem(1);
        } else {
            viewPager.setCurrentItem(0);
        }
    }
    @Override
    public void openChatFragment(Chat chat) {
        this.selectedChat = chat;
        adapter.setChat(chat);
        viewPager.setCurrentItem(1);
    }

    public interface OnFragmentInteractionListener {
        Chat getSelectedChat();
    }
}
