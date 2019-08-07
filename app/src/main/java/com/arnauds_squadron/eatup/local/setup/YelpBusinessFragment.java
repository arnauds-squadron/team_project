package com.arnauds_squadron.eatup.local.setup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.Printer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.YelpBusinessAdapter;
import com.arnauds_squadron.eatup.home.HomeAdapter;
import com.arnauds_squadron.eatup.models.Business;
import com.arnauds_squadron.eatup.models.Categories;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.yelp_api.YelpApiResponse;
import com.arnauds_squadron.eatup.yelp_api.YelpData;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link YelpBusinessFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class YelpBusinessFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private List<Business> mBusiness;

    @BindView(R.id.rvYelpBusinesses)
    RecyclerView rvYelpBusinesses;

    @BindView(R.id.btnNext)
    Button btnNext;

    FragmentActivity listener;
    YelpBusinessAdapter yelpBusinessAdapter;

    Event event;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_yelp_business, container, false);
        ButterKnife.bind(this, view);
        return  view;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            event = mListener.getCurrentEvent();
            initializeViews();
        }
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

    @OnClick(R.id.btnNext)
    public void goToNextFragment() {
        String businessId = mBusiness.get(yelpBusinessAdapter.getPosition()).id;
        List<Categories> categories = mBusiness.get(yelpBusinessAdapter.getPosition()).categories;
        List<String> tags = new ArrayList<>();
        for(int i = 0; i < categories.size(); i++){
            tags.add(categories.get(i).alias);
        }
        tags.size();
        mListener.updateCategories(tags);
        mListener.updateBusinessId(businessId);
    }

    private void initializeViews() {
        mBusiness = new ArrayList<>();
        // construct adapter from data source
        yelpBusinessAdapter = new YelpBusinessAdapter(getContext(), mBusiness);
        // RecyclerView setup
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        rvYelpBusinesses.setLayoutManager(layoutManager);
        rvYelpBusinesses.setAdapter(yelpBusinessAdapter);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        rvYelpBusinesses.addItemDecoration(itemDecoration);

        Call<YelpApiResponse> meetUp = YelpData.retrofit(getContext()).getLocation(
                event.getAddress().getLatitude(), event.getAddress().getLongitude(),
                "food", 50);

        meetUp.enqueue(new Callback<YelpApiResponse>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<YelpApiResponse> call,
                                   @NonNull retrofit2.Response<YelpApiResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("code met", "Activity");
                    YelpApiResponse yelpApiResponse = response.body();
                    int i = 0;
                    while (yelpApiResponse != null && mBusiness.size() < yelpApiResponse.businessList.size()) {
                        Business restaurant = yelpApiResponse.businessList.get(i);
                        mBusiness.add(restaurant);
                        yelpBusinessAdapter.notifyItemInserted(mBusiness.size() - 1);
                        i++;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<YelpApiResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public interface OnFragmentInteractionListener {
        /** updates the Id for the Business the user
         * clicks on
         */
        void updateBusinessId(String id);

        void updateCategories(List<String> categories);

        Event getCurrentEvent();
    }
}
