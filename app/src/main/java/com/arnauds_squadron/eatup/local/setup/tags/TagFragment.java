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

    private Activity activity;

    private List<String> tagList;
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
     * @param fragment The parent fragment
     */
    public void onAttachToParentFragment(Fragment fragment) {
        try {
            mListener = (OnFragmentInteractionListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement the interface");
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

    private void addTag() {
        String newTag = tvEventFoodType.getText().toString().trim();
        if (!tagList.contains(newTag)) {
            tagList.add(newTag);
            tagAdapter.notifyDataSetChanged();
            tvEventFoodType.setText("");
        } else {
            Toast.makeText(getActivity(), String.format("Can't add the '%s' tag again", newTag),
                    Toast.LENGTH_SHORT).show();
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
