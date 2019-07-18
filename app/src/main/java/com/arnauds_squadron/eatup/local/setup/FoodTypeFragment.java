package com.arnauds_squadron.eatup.local.setup;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.arnauds_squadron.eatup.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FoodTypeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FoodTypeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    @BindView(R.id.etEventFoodType)
    EditText etEventFoodType;

    public static FoodTypeFragment newInstance() {
        Bundle args = new Bundle();
        FoodTypeFragment fragment = new FoodTypeFragment();
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
        View view = inflater.inflate(R.layout.fragment_food_type, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    /**
     * Called in onCreate to bind the this child fragment to its parent, so the listener
     * can be used
     * @param fragment The parent fragment
     */
    public void onAttachToParentFragment(Fragment fragment)
    {
        try {
            mListener = (OnFragmentInteractionListener) fragment;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement the interface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @OnClick(R.id.btnNext)
    public void goToNextFragment() {
        String foodType = etEventFoodType.getText().toString();
        mListener.updateFoodType(foodType);
    }

    public interface OnFragmentInteractionListener {
        /**
         * When called by the parent fragment, it should switch to the next fragment in the
         * setup queue
         */
        void updateFoodType(String foodType);
    }
}
