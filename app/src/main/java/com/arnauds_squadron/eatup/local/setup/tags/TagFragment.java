package com.arnauds_squadron.eatup.local.setup.tags;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment that asks the user to input the cuisine of the restaurant they are visiting or of the
 * food they are cooking.
 */
public class TagFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    @BindView(R.id.tvEventFoodType)
    AutoCompleteTextView tvEventFoodType;

    @BindView(R.id.lvTagList)
    ListView lvTagList;

    // Helpful variable to access the context
    private Activity activity;
    // List of tags selected by the user
    private List<String> tagList;
    // Adapter instance to handle adding tag items to the ListView
    private TagAdapter tagAdapter;

    public static TagFragment newInstance() {
        Bundle args = new Bundle();
        TagFragment fragment = new TagFragment();
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
        View view = inflater.inflate(R.layout.fragment_event_tags, container, false);
        ButterKnife.bind(this, view);

        activity = getActivity();
        // TODO: move array of food types to a server
        ArrayAdapter adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.food_types));

        tvEventFoodType.setAdapter(adapter);
        tvEventFoodType.setThreshold(1); // show results after one letter

        // Show the dropdown of cuisines once the user selects the view
        tvEventFoodType.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                tvEventFoodType.showDropDown();
            }
        });

        // Add the item if they select a preset
        tvEventFoodType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addTag();
                tvEventFoodType.showDropDown();
            }
        });

        // Set tag list adapter
        tagList = new ArrayList<>();
        tagAdapter = new TagAdapter(activity, tagList);
        lvTagList.setAdapter(tagAdapter);

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
