package com.arnauds_squadron.eatup.visitor;

import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.arnauds_squadron.eatup.MainActivity;
import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.profile.ProfileActivity;
import com.arnauds_squadron.eatup.utils.EndlessRecyclerViewScrollListener;
import com.arnauds_squadron.eatup.utils.FetchAddressIntentService;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.arnauds_squadron.eatup.utils.Constants.CUISINE_SEARCH;
import static com.arnauds_squadron.eatup.utils.Constants.DISPLAY_NAME;
import static com.arnauds_squadron.eatup.utils.Constants.LOCATION_DATA_EXTRA;
import static com.arnauds_squadron.eatup.utils.Constants.LOCATION_SEARCH;
import static com.arnauds_squadron.eatup.utils.Constants.NO_SEARCH;
import static com.arnauds_squadron.eatup.utils.Constants.RECEIVER;
import static com.arnauds_squadron.eatup.utils.Constants.RESULT_DATA_KEY;
import static com.arnauds_squadron.eatup.utils.Constants.SEARCH_CATEGORY;
import static com.arnauds_squadron.eatup.utils.Constants.SUCCESS_RESULT;
import static com.arnauds_squadron.eatup.utils.Constants.USER_SEARCH;


public class VisitorFragment extends Fragment {

    // TODO browsing nearby events - account for scenario in which user doesn't enable current location, use last remembered location or display a random array of events

    @BindView(R.id.tvDisplayName)
    TextView tvDisplayName;
    @BindView(R.id.tvBrowseTitle)
    TextView tvBrowseTitle;
    @BindView(R.id.rvBrowse)
    RecyclerView rvBrowse;
    @BindView(R.id.tvCurrentLocation)
    TextView tvCurrentLocation;
    @BindView(R.id.tvPrevLocation1)
    TextView tvPrevLocation1;
    @BindView(R.id.tvPrevLocation2)
    TextView tvPrevLocation2;
    @BindView(R.id.searchSpinner)
    Spinner searchSpinner;

    private Unbinder unbinder;
    private EndlessRecyclerViewScrollListener scrollListener;
    private BrowseEventAdapter eventAdapter;
    private ArrayList<Event> mEvents;


    private AddressResultReceiver resultReceiver;
    private String addressOutput;

    private int searchCategoryCode;

    public static VisitorFragment newInstance() {
        Bundle args = new Bundle();
        VisitorFragment fragment = new VisitorFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visitor, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // TODO uncomment so can set name of current user
        tvDisplayName.setText(ParseUser.getCurrentUser().getString(DISPLAY_NAME));
        // initialize data source
        mEvents = new ArrayList<>();
        // construct adapter from data source
        eventAdapter = new BrowseEventAdapter(getContext(), mEvents);
        // RecyclerView setup
        SnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(rvBrowse);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false);
        rvBrowse.setLayoutManager(gridLayoutManager);
        rvBrowse.setAdapter(eventAdapter);

        resultReceiver = new AddressResultReceiver(new Handler());

        // load data entries
        // retain instance so can call "resetStates" for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Date maxEventId = getMaxDate();
                Log.d("DATE", maxEventId.toString());
                loadTopEvents(getMaxDate());
            }
        };
        // add endless scroll listener to RecyclerView and load items
        rvBrowse.addOnScrollListener(scrollListener);
        loadTopEvents(new Date(0));

        Location location = MainActivity.getCurrentLocation();
        if (location != null) {

            // TODO - tag current location with coordinates? or just store this information in the Parse database
            // geocoder for translating coordinates to address
            if (!Geocoder.isPresent()) {
//                        Toast.makeText(getActivity(),
//                                R.string.no_geocoder_available,
//                                Toast.LENGTH_LONG).show();
                tvCurrentLocation.setText(String.format(Locale.getDefault(), "%f, %f",
                        location.getLatitude(), location.getLongitude()));

            } else {
                // Start geocoder service and update UI to reflect the new address
                startIntentService(location);
            }
            tvCurrentLocation.setTag(R.id.latitude, location.getLatitude());
            tvCurrentLocation.setTag(R.id.longitude, location.getLongitude());
        }

        // TODO tag previous locations with latitude and longitude. default (0, 0)
        tvPrevLocation1.setTag(String.format(Locale.getDefault(), "%f, %f", 0.0, 0.0));
        tvPrevLocation2.setTag(String.format(Locale.getDefault(), "%f, %f", 0.0, 0.0));

        // initialize spinner_text_view for search filtering
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.search_categories, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner_text_view
        searchSpinner.setAdapter(adapter);
        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // when item selected, bring user to the new search activity with search bar and search category packaged as intent extra
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    // search category hint
                    case 0:
                        break;
                    // user
                    case 1:
                        searchCategoryCode = USER_SEARCH;
                        break;
                    // cuisine
                    case 2:
                        searchCategoryCode = CUISINE_SEARCH;
                        break;
                    // location
                    case 3:
                        searchCategoryCode = LOCATION_SEARCH;
                        break;
                }
                if (searchCategoryCode != 0) {
                    searchSpinner.setSelection(NO_SEARCH);
                    Intent i = new Intent(getContext(), VisitorSearchActivity.class);
                    i.putExtra(SEARCH_CATEGORY, searchCategoryCode);
                    getContext().startActivity(i);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // don't do anything
            }
        });
    }

    @OnClick({R.id.tvCurrentLocation, R.id.tvPrevLocation1, R.id.tvPrevLocation2})
    public void searchLocation(TextView tvLocation) {
        // TODO search the event database by current location. currently sends the lat/long data to SearchActivity
        Intent i = new Intent(getActivity(), VisitorSearchActivity.class);
        i.putExtra("latitude", (Double) tvLocation.getTag(R.id.latitude));
        i.putExtra("longitude", (Double) tvLocation.getTag(R.id.longitude));
        startActivity(i);
    }

    @OnClick(R.id.tvDisplayName)
    public void viewUserProfile() {
        Intent i = new Intent(getActivity(), ProfileActivity.class);
        ParseUser user = ParseUser.getCurrentUser();
        i.putExtra("user", user);
        getActivity().startActivity(i);
    }

    public void updateLocationTextView() {}

    // methods to load posts into the recyclerview based on location
    protected void loadTopEvents(Date maxDate) {
//        progressBar.setVisibility(View.VISIBLE);
        final Event.Query eventsQuery = new Event.Query();
        // if opening app for the first time, get top 20 and clear old items
        // otherwise, query for posts older than the oldest
        if (maxDate.equals(new Date(0))) {
            eventAdapter.clear();
            eventsQuery.getTop().withHost();
        } else {
            eventsQuery.getOlder(maxDate).getTop().withHost();
        }

        eventsQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); ++i) {
                        mEvents.add(objects.get(i));
                        eventAdapter.notifyItemInserted(mEvents.size() - 1);
                        // on successful reload, signal that refresh has completed
//                        swipeContainer.setRefreshing(false);
                    }
                } else {
                    e.printStackTrace();
                }
//                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    // get date of oldest post
    protected Date getMaxDate() {
        int postsSize = mEvents.size();
        if (postsSize == 0) {
            return (new Date(0));
        } else {
            Event oldest = mEvents.get(mEvents.size() - 1);
            return oldest.getCreatedAt();
        }
    }


//    private void showSnackbar(final int mainTextStringId, final int actionStringId,
//                               View.OnClickListener listener) {
//        Snackbar.make(getActivity().findViewById(android.R.id.content),
//                getString(mainTextStringId),
//                Snackbar.LENGTH_INDEFINITE)
//                .setAction(getString(actionStringId), listener).show();
//    }

    // rewrite above method to avoid int errors


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    // start Intent to get address from lat/long coordinates
    protected void startIntentService(Location newLocation) {
        Intent intent = new Intent(getContext(), FetchAddressIntentService.class);
        intent.putExtra(RECEIVER, resultReceiver);
        intent.putExtra(LOCATION_DATA_EXTRA, newLocation);
        getActivity().startService(intent);
    }


    // ResultReceiver to set current location field based on address of lat/long
    class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            // Display the address string
            // or an error message sent from the intent service.
            addressOutput = resultData.getString(RESULT_DATA_KEY);
            if (addressOutput == null) {
                addressOutput = "";
            }

            // display current address to user if found.
            if (resultCode == SUCCESS_RESULT) {
                tvCurrentLocation.setText(addressOutput);
            }

        }
    }
}
