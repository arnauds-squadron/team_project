package com.arnauds_squadron.eatup.local.setup.yelp_selection;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Business;
import com.arnauds_squadron.eatup.models.Category;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.yelp_api.YelpApiResponse;
import com.arnauds_squadron.eatup.yelp_api.YelpData;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Fragment that takes the user inputted address and finds the nearby restaurants by querying the
 * Yelp API, and displays them in a list that the user selects from
 */
public class YelpBusinessFragment extends Fragment {

    @BindView(R.id.rvYelpBusinesses)
    RecyclerView rvYelpBusinesses;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.tvErrorLoadingRestaurants)
    TextView tvErrorLoading;

    private OnFragmentInteractionListener mListener;
    private List<Business> mBusiness;
    private YelpBusinessAdapter yelpBusinessAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_yelp_business, container, false);
        ButterKnife.bind(this, view);

        mBusiness = new ArrayList<>();
        yelpBusinessAdapter = new YelpBusinessAdapter(getContext(), mBusiness, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvYelpBusinesses.setLayoutManager(layoutManager);
        rvYelpBusinesses.setAdapter(yelpBusinessAdapter);

        return view;
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
     * Notifies the parent adapter to go to the next fragment when the user selects a yelp
     * restaurant
     *
     * @param position The index of the restaurant selected by the user
     */
    public void goToNextFragment(int position) {
        String businessId = mBusiness.get(position).id;
        String businessName = mBusiness.get(position).name;
        List<Category> categories = mBusiness.get(position).categories;
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            tags.add(categories.get(i).alias);
        }
        mListener.updateYelpBusiness(businessId, businessName, tags);
    }

    public void findNearbyRestaurants(Event event) {
        mBusiness.clear();
        yelpBusinessAdapter.notifyDataSetChanged();

        Call<YelpApiResponse> meetUp = YelpData.retrofit(getContext()).getLocation(
                event.getAddress().getLatitude(), event.getAddress().getLongitude(),
                "restaurant", "distance");

        progressBar.setVisibility(View.VISIBLE);
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
                    tvErrorLoading.setVisibility(View.INVISIBLE);
                } else {
                    tvErrorLoading.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(@NonNull Call<YelpApiResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public interface OnFragmentInteractionListener {
        /**
         * updates the Id for the Business the user
         * clicks on
         */
        void updateYelpBusiness(String id, String name, List<String> tags);
    }
}
