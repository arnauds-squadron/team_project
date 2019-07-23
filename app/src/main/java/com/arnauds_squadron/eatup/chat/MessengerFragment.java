package com.arnauds_squadron.eatup.chat;

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
        ChatDashboardFragment.OnFragmentInteractionListener,
        ChatFragment.OnFragmentInteractionListener {

    @BindView(R.id.viewPager)
    NoSwipingPagerAdapter viewPager;

    private Chat selectedChat;

    public static MessengerFragment newInstance() {
        return new MessengerFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_dashboard, container, false);
        ButterKnife.bind(this, view);
        viewPager.setAdapter(new ChatFragmentPagerAdapter(getChildFragmentManager()));
        return view;
    }

    @Override
    public void setSelectedChat(Chat chat) {
        this.selectedChat = chat;
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    @Override
    public Chat getSelectedChat() {
        return selectedChat;
    }
}
