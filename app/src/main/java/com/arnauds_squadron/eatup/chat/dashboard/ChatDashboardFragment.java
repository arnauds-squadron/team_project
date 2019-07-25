package com.arnauds_squadron.eatup.chat.dashboard;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Chat;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment that displays the list of active chats the user is part of
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
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));

        getChats();

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

    // TODO: caching
    private void getChats() {
        Chat.Query query = new Chat.Query();
        query.setQueryLimit().getTop();
        query.findInBackground(new FindCallback<Chat>() {
            @Override
            public void done(List<Chat> objects, ParseException e) {
                if (e == null) {
                    String userId = ParseUser.getCurrentUser().getObjectId();
                    for (int i = 0; i < objects.size(); i++) {
                        Chat chat = objects.get(i);
                        JSONArray members = chat.getMembers();

                        for (int j = 0; j < members.length(); j++) {
                            try {
                                // TODO: move to new thread?
                                if (chat.getMembers().getJSONObject(j).getString("objectId")
                                        .equals(userId)) {
                                    chatList.add(chat);
                                    chatAdapter.notifyItemInserted(chatList.size() - 1);
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                } else {
                    Toast.makeText(getActivity(), "Could not load chats",
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
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
