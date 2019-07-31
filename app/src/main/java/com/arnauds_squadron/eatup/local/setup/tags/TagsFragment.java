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
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment that asks the user to input the cuisine of the restaurant they are visiting or of the
 * food they are cooking.
 */
public class TagsFragment extends Fragment {

    @BindView(R.id.tvEventFoodType)
    AutoCompleteTextView tvEventFoodType;

    @BindView(R.id.lvTagList)
    ListView lvTagList;

    @BindView(R.id.cbIs21Plus)
    CheckBox cbIs21Plus;

    @BindView(R.id.npMaxGuests)
    NumberPicker npMaxGuests;

    private OnFragmentInteractionListener mListener;
    private Activity activity;
    private List<String> tagList;
    private TagAdapter tagAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_tags, container, false);
        activity = getActivity();
        ButterKnife.bind(this, view);

        setupAutocompleteTextView();
        setupListView();
        setUpNumberPicker();
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

    /**
     * Navigates to the next setup fragment if at least 1 tag is selected for their event
     */
    @OnClick(R.id.btnNext)
    public void goToNextFragment() {
        if (!tagList.isEmpty()) {
            Event event = new Event();
            event.setHost(Constants.CURRENT_USER);
            event.setTags(tagList);
            event.setOver21(cbIs21Plus.isChecked());
            event.setMaxGuests(npMaxGuests.getValue());
            mListener.updateTags(event);
        } else {
            Toast.makeText(activity, "Select at least 1 tag for your event",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Adds the tag selected by the user in the Autocomplete TextView to the tag list
     * Only adds a tag if the value is not empty and has not already been selected
     */
    // TODO: validate food type, no bad words?
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

    /**
     * Sets up the Autocomplete TextView so the user sees likely tags that they might be searching
     * for.
     */
    private void setupAutocompleteTextView() {
        // TODO: move array of food types to a server
        ArrayAdapter adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.food_types));

        tvEventFoodType.setAdapter(adapter);
        tvEventFoodType.setThreshold(1); // show results after one letter

        // Add the item immediately after they select a preset (no button press required)
        tvEventFoodType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addTag();
                tvEventFoodType.showDropDown();
            }
        });
    }

    /**
     * Sets up the ListView so when an item is added to the tagList, the ListView is updated
     */
    private void setupListView() {
        tagList = new ArrayList<>();
        tagAdapter = new TagAdapter(activity, tagList);
        lvTagList.setAdapter(tagAdapter);
    }

    /**
     * Sets up the number picker widget so the user can select the max number of guests
     */
    private void setUpNumberPicker() {
        npMaxGuests.setMinValue(1);
        npMaxGuests.setMaxValue(Constants.MAX_GUESTS);
        npMaxGuests.setWrapSelectorWheel(false);
    }

    public interface OnFragmentInteractionListener {
        /**
         * When called by the parent fragment, it should switch to the next fragment in the
         * setup queue
         */
        void updateTags(Event event);
    }
}
