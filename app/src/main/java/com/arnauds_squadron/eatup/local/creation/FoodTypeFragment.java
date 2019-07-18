package com.arnauds_squadron.eatup.local.creation;

import android.content.Context;
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
 * Use the {@link AddressFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FoodTypeFragment extends SetupFragment {

    private final static String TAG = "LocalFragment";

    @BindView(R.id.etEventFoodType)
    EditText etEventFoodType;

    public static FoodTypeFragment newInstance() {
        Bundle args = new Bundle();
        FoodTypeFragment fragment = new FoodTypeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_type, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @OnClick(R.id.btnNext)
    public void goToNextFragment() {
        mListener.showNextFragment(1);
    }
}
