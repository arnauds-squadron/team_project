package com.arnauds_squadron.eatup.local.setup.tags;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.chat.dashboard.ChatDashboardAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment that asks the user to input the cuisine of the restaurant they are visiting or of the
 * food they are cooking.
 */
public class ChatDashboardFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    @BindView(R.id.rvChats)
    RecyclerView rvChats;

    // List of the different active conversations
    private List<String> chatList;

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
        chatAdapter = new ChatDashboardAdapter(getActivity(), chatList);
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

    /**
     * Adds the item typed
     */
    @OnClick(R.id.btnAddTag)
    public void addTagOnClick() {
        addTag();
    }

    @OnClick(R.id.btnNext)
    public void goToNextFragment() {
        // TODO: validate food type, no bad words?
        mListener.updateTags(tagList);
    }

    /**
     * Adds the tag selected by the user in the Autocomplete TextView to the tag list
     * Only adds a tag if the value is not empty and has not already been selected
     */
    private void addTag() {
        String newTag = tvEventFoodType.getText().toString().trim();
        if (newTag.isEmpty()) {
            Toast.makeText(activity, "Select a tag or make your own!", Toast.LENGTH_SHORT)
                    .show();
        } else if (tagList.contains(newTag)) {
            Toast.makeText(activity, String.format("Can't add the '%s' tag again", newTag),
                    Toast.LENGTH_SHORT).show();
        } else {
            tagList.add(newTag);
            tagAdapter.notifyDataSetChanged();
            tvEventFoodType.setText("");
        }
    }

    public interface OnFragmentInteractionListener {
        /**
         * When called by the parent fragment, it should switch to the next fragment in the
         * setup queue
         */
        void updateTags(List<String> tags);
    }
}
