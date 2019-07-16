package com.arnauds_squadron.eatup.local;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LocalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocalFragment extends Fragment {

    private final static String TAG = "LocalFragment";

    @BindView(R.id.etEventAddress)
    EditText etEventAddress;

    @BindView(R.id.etEventFoodType)
    EditText etEventFoodType;

    @BindView(R.id.etEventTime)
    EditText etEventTime;

    private Activity activity;

    public static LocalFragment newInstance() {
        Bundle args = new Bundle();
        LocalFragment fragment = new LocalFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local, container, false);
        // Binds the views with butterknife
        ButterKnife.bind(this, view);
        activity = getActivity();
        return view;
    }

    @OnClick(R.id.btnConfirm)
    public void confirmEvent() {
        String address = etEventAddress.getText().toString();
        String food = etEventFoodType.getText().toString();
        String time = etEventTime.getText().toString();

        // TODO: validate data
        // TODO: upload event to parse server
        // TODO: only return to home screen within onsuccess

        Log.i(TAG, "confirming");
        if(getFragmentManager() != null) {
            Log.i(TAG, "confirming not null");

            try {
               TabLayout tabLayout = activity.findViewById(R.id.sliding_tabs);
               tabLayout.getTabAt(1).select();
               Toast.makeText(activity, "Event created", Toast.LENGTH_SHORT).show();
            }
            catch (NullPointerException e) {
                Log.e(TAG, "Activity, tab layout, or home tab is null");
                Toast.makeText(activity, "Could not create event", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
