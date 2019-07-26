package com.arnauds_squadron.eatup.visitor;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.EndlessRecyclerViewScrollListener;
import com.arnauds_squadron.eatup.yelp_api.YelpApiResponse;
import com.arnauds_squadron.eatup.yelp_api.YelpService;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.arnauds_squadron.eatup.utils.Constants.NO_SEARCH;
import static com.arnauds_squadron.eatup.utils.Constants.USER_SEARCH;
import static com.arnauds_squadron.eatup.utils.Constants.CUISINE_SEARCH;
import static com.arnauds_squadron.eatup.utils.Constants.LOCATION_SEARCH;
import static com.arnauds_squadron.eatup.utils.Constants.SEARCH_CATEGORY;


public class VisitorSearchActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // initialize adapter, views, scroll listener
    private SearchEventAdapter eventAdapter;
    private ArrayList<Event> mEvents;
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;
    private ProgressBar progressBar;
    private ParseUser currentUser;

    // variables to keep track of current query
    private int searchCategory;
    private ParseUser queriedUser;
    private String queriedCuisine;
    private ParseGeoPoint queriedGeoPoint;

    private final static Double DEFAULT_COORD = 0.0;

    String[] LOCATION_SEARCH_SUGGEST_COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
                                SearchManager.SUGGEST_COLUMN_INTENT_DATA
    };

    String[] CATEGORY_SEARCH_SUGGEST_COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA
    };

    private String CURRENT_LOCATION_ID = "currentLocation";

    @BindView(R.id.rvSearchResults)
    RecyclerView rvEvents;
    @BindView(R.id.resultsSearchView)
    SearchView resultsSearchView;
    @BindView(R.id.searchSpinner)
    Spinner searchSpinner;

    // adapter for search suggestions
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_visitor_search);
        ButterKnife.bind(this);

        Places.initialize(this, getString(R.string.google_api_key));
        placesClient = Places.createClient(this);



//        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        // initialize data source
        mEvents = new ArrayList<>();
        // construct adapter from data source
        eventAdapter = new SearchEventAdapter(this, mEvents);
        // RecyclerView setup
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvEvents.setLayoutManager(linearLayoutManager);
        rvEvents.setAdapter(eventAdapter);

        // initialize search services
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        // Assumes current activity is the searchable activity
        resultsSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        resultsSearchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        // initialize spinner for search filtering
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.search_categories, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //c Apply the adapter to the spinner
        searchSpinner.setAdapter(adapter);
        searchSpinner.setOnItemSelectedListener(this);

        // handle search intent
        handleIntent(getIntent());

        // load data entries
        // retain instance so can call "resetStates" for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Date maxEventId = getMaxDate();
                Log.d("DATE", maxEventId.toString());
                handleRecyclerEvents(getMaxDate());
            }
        };
        // add endless scroll listener to RecyclerView and load items
        rvEvents.addOnScrollListener(scrollListener);

        // set up refresh listener that triggers new data loading
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handleRecyclerEvents(new Date(0));
            }
        });
        // configure refreshing colors
        swipeContainer.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light));

    }

    private void handleRecyclerEvents(Date maxDate) {
        switch(searchCategory) {
            case USER_SEARCH:
                loadTopEvents(queriedUser, maxDate);
                break;
            case CUISINE_SEARCH:
                loadTopEvents(queriedCuisine, maxDate);
                break;
            case LOCATION_SEARCH:
                break;
        }
    }

    // Get the intent, verify the action and get the query
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, query, Toast.LENGTH_SHORT).show();
            int newSearchCategory = intent.getIntExtra(SEARCH_CATEGORY, 0);
            Log.d("VisitorSearchActivity", "spinner position: " + newSearchCategory);
            switch(newSearchCategory) {
                case USER_SEARCH:
                    userSearch(query);
                    break;
                case CUISINE_SEARCH:
                    queriedCuisine = query;
                    loadTopEvents(queriedCuisine, new Date(0));
                    // TODO get search suggestions of cuisines from the Yelp Search API
                    // handled in the automatic search
                    break;
                case LOCATION_SEARCH:
                    // handled in the automatic search suggestion
                    break;
            }
        }
        // otherwise called by a click on something in VisitorFragment
        else {
            searchCategory = intent.getIntExtra(SEARCH_CATEGORY, 0);
            // if not any of the categories, user clicked on current/previous location
            switch(searchCategory) {
                case NO_SEARCH:
                    Double latitude = intent.getDoubleExtra("latitude", DEFAULT_COORD);
                    Double longitude = intent.getDoubleExtra("longitude", DEFAULT_COORD);
                    queriedGeoPoint = new ParseGeoPoint(latitude, longitude);
                    locationSearch(queriedGeoPoint, new Date(0));
                    break;
                case LOCATION_SEARCH:
                    setLocationSuggestions(resultsSearchView);
                    break;
                case CUISINE_SEARCH:
                    setCategorySuggestions(resultsSearchView);
                    break;
            }
            searchSpinner.setSelection(searchCategory);
        }
    }

    // Methods for search category spinner
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        mEvents.clear();
        eventAdapter.notifyDataSetChanged();
        searchCategory = pos;

        if(searchCategory == LOCATION_SEARCH) {
            setLocationSuggestions(resultsSearchView);
        }
        else if (searchCategory == CUISINE_SEARCH) {
            setCategorySuggestions(resultsSearchView);
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // TODO what does this entail
    }

    // Methods to query parse server
    private void userSearch(String userQuery) {
        // query for user, then query for events containing user
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("displayName", userQuery);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    if(objects.size() != 0) {
                        Log.d("VisitorSearchActivity", "found user");
                        queriedUser = objects.get(0);
                        loadTopEvents(queriedUser, new Date(0));
                    } else {
                        Toast.makeText(getApplicationContext(), "No users found with that username.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
        resultsSearchView.clearFocus();
    }

    private void locationSearch(ParseGeoPoint geoPoint, Date maxDate) {
        final Event.Query eventsQuery = new Event.Query();
        if (maxDate.equals(new Date(0))) {
            eventAdapter.clear();
            eventsQuery.getClosest(geoPoint).getTop().withHost();
        } else {
            eventsQuery.getOlder(maxDate).getClosest(geoPoint).getTop().withHost();
        }

        eventsQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    if(objects.size() != 0) {
                        for (int i = 0; i < objects.size(); ++i) {
                            mEvents.add(objects.get(i));
                            eventAdapter.notifyItemInserted(mEvents.size() - 1);
                            // on successful reload, signal that refresh has completed
                            swipeContainer.setRefreshing(false);
                        }
                    }
                    else {
                        // only notify user if no events to show
                        if(mEvents.size() == 0) {
                            Toast.makeText(getApplicationContext(), "No events found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    e.printStackTrace();
                }
//                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    protected void loadTopEvents(ParseUser user, Date maxDate) {
        final Event.Query eventsQuery = new Event.Query();
        // if opening app for the first time, get top 20 and clear old items
        // otherwise, query for posts older than the oldest
        if (maxDate.equals(new Date(0))) {
            eventAdapter.clear();
            eventsQuery.getTop().withHost().whereEqualTo("host", user);
        } else {
            eventsQuery.getOlder(maxDate).getTop().withHost().whereEqualTo("host", user);
        }
        eventsQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    if(objects.size() != 0) {
                        for (int i = 0; i < objects.size(); ++i) {
                            mEvents.add(objects.get(i));
                            eventAdapter.notifyItemInserted(mEvents.size() - 1);
                            // on successful reload, signal that refresh has completed
                            swipeContainer.setRefreshing(false);
                        }
                    }
                    else {
                        // only notify user if no events to show
                        if(mEvents.size() == 0) {
                            Toast.makeText(getApplicationContext(), "No events found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void loadTopEvents(String cuisineQuery, Date maxDate) {
        final Event.Query eventsQuery = new Event.Query();
        // if opening app for the first time, get top 20 and clear old items
        // otherwise, query for events older than the oldest
        if (maxDate.equals(new Date(0))) {
            eventAdapter.clear();
            eventsQuery.getTop().withHost().whereEqualTo("foodType", cuisineQuery);
        } else {
            eventsQuery.getOlder(maxDate).getTop().withHost().whereEqualTo("foodType", cuisineQuery);
        }
        eventsQuery.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    if(objects.size() != 0) {
                        for (int i = 0; i < objects.size(); ++i) {
                            mEvents.add(objects.get(i));
                            eventAdapter.notifyItemInserted(mEvents.size() - 1);
                            // on successful reload, signal that refresh has completed
                            swipeContainer.setRefreshing(false);
                        }
                    }
                    else {
                        // only notify user if no events to show
                        if(mEvents.size() == 0) {
                            Toast.makeText(getApplicationContext(), "No events found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    // get date of oldest post
    protected Date getMaxDate() {
        int eventsSize = mEvents.size();
        if (eventsSize == 0) {
            return (new Date(0));
        } else {
            Event oldest = mEvents.get(mEvents.size() - 1);
            return oldest.getCreatedAt();
        }
    }

    private void setLocationSuggestions(final SearchView searchView) {
        // adapter for search suggestions
        final CursorAdapter locationSuggestionAdapter = new SimpleCursorAdapter(this,
                R.layout.location_search_suggestion_item,
                null,
                new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_DATA},
                new int[]{R.id.tvLocationPrimary, R.id.tvLocationSecondary},
                0);

        searchView.setSuggestionsAdapter(locationSuggestionAdapter);

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                // if user scrolls through results using some sort of trackpad
                Cursor cursor = (Cursor) locationSuggestionAdapter.getItem(position);
                String queryText = String.format(Locale.getDefault(),
                        "%s, %s",
                        cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)),
                        cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2)));
                searchView.setQuery(queryText, false);
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) locationSuggestionAdapter.getItem(position);
                String locationQueryId = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_DATA));
                String queryText;
                if (locationQueryId.equals(CURRENT_LOCATION_ID)) {
                    // TODO get users current location again
                    Toast.makeText(getApplicationContext(), "search by user current location", Toast.LENGTH_SHORT).show();
                    queryText = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                } else {
                    queryText = String.format(Locale.getDefault(),
                            "%s, %s",
                            cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)),
                            cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2)));
                }
                searchView.setQuery(queryText, false);

                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);
                FetchPlaceRequest request = FetchPlaceRequest.builder(locationQueryId, placeFields).build();
                placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse response) {
                        Place place = response.getPlace();
                        Double latitude = place.getLatLng().latitude;
                        Double longitude = place.getLatLng().longitude;
                        Log.i("Fetch Place Request", "Successful call");
                        locationSearch(new ParseGeoPoint(latitude, longitude), new Date(0));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            int statusCode = apiException.getStatusCode();
                            // Handle error with given status code.
                            Log.e("Fetch Place Request", "Place not found: " + exception.getMessage());
                        }
                    }
                });
                searchView.clearFocus();
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String incompleteQuery) {
                // TODO set different search suggestions for cuisine and location

                AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

                // Use the builder to create a FindAutocompletePredictionsRequest.
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        //.setLocationRestriction(bounds)
                        .setCountry("us")
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(incompleteQuery)
                        .build();

                placesClient.findAutocompletePredictions(request).addOnSuccessListener(new OnSuccessListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onSuccess(FindAutocompletePredictionsResponse response) {
                        MatrixCursor cursor = new MatrixCursor(LOCATION_SEARCH_SUGGEST_COLUMNS);
                        cursor.addRow(new String[]{
                                "1",
                                "Current Location",
                                "Use my current location",
                                CURRENT_LOCATION_ID
                        });

                        int predictionCounter = 2;
                        for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                            Log.i("findPlaceSuggestions", prediction.getPlaceId());
                            Log.i("findPlaceSuggestions", prediction.getPrimaryText(null).toString());
                            cursor.addRow(new String[]{
                                    Integer.toString(predictionCounter),
                                    prediction.getPrimaryText(null).toString(),
                                    prediction.getSecondaryText(null).toString(),
                                    prediction.getPlaceId()
                            });
                            predictionCounter++;
                        }
                        locationSuggestionAdapter.swapCursor(cursor);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                        MatrixCursor cursor = new MatrixCursor(LOCATION_SEARCH_SUGGEST_COLUMNS);
                        cursor.addRow(new String[]{
                                "2",
                                "No places found",
                                "Try searching again",
                                "No results"
                        });

                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            Log.e("findPlaceSuggestions", "Place not found: " + apiException.getStatusCode());
                        }
                        locationSuggestionAdapter.swapCursor(cursor);
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                mEvents.clear();
                eventAdapter.notifyDataSetChanged();
                searchCategory = searchSpinner.getSelectedItemPosition();
                if (searchCategory != NO_SEARCH) {
                    if (searchCategory == LOCATION_SEARCH) {
                        Toast.makeText(getApplicationContext(), "Select an address.", Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        Intent searchIntent = new Intent(getApplicationContext(), VisitorSearchActivity.class);
                        searchIntent.putExtra(SearchManager.QUERY, query);
                        searchIntent.putExtra(SEARCH_CATEGORY, searchSpinner.getSelectedItemPosition());
                        searchIntent.setAction(Intent.ACTION_SEARCH);
                        startActivity(searchIntent);
                        // clear focus so search doesn't fire twice
                        searchView.clearFocus();
                        searchView.setQuery(query, false);
                        return true;
                    }
                } else {
                    // prevent submission if no category selected
                    searchView.setQuery(query, false);
                    Toast.makeText(getApplicationContext(), "Select a search category.", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        });
    }

    private void setCategorySuggestions(final SearchView searchView) {
        // adapter for search suggestions
        final CursorAdapter categorySuggestionAdapter = new SimpleCursorAdapter(this,
                R.layout.category_search_suggestion_item,
                null,
                new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA},
                new int[]{R.id.tvCategory},
                0);

        searchView.setSuggestionsAdapter(categorySuggestionAdapter);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                // if user scrolls through results using some sort of trackpad
                Cursor cursor = (Cursor) categorySuggestionAdapter.getItem(position);
                String queryText = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                searchView.setQuery(queryText, false);
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) categorySuggestionAdapter.getItem(position);
                String categoryQueryId = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_DATA));
                String queryText = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                searchView.setQuery(queryText, false);
                // TODO modify loadTopEvents to search by categoryQueryId
                loadTopEvents(queryText, new Date(0));
                searchView.clearFocus();
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String incompleteQuery) {

                if(incompleteQuery.equals("")) {
                    MatrixCursor cursor = new MatrixCursor(CATEGORY_SEARCH_SUGGEST_COLUMNS);
                    categorySuggestionAdapter.swapCursor(cursor);
                } else{
                    final String yelpKey = getString(R.string.yelp_api_key);
                    //Getting Authentication through OkHttpClient
                    OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
                            .addInterceptor(new Interceptor() {
                                @NotNull
                                @Override
                                public Response intercept(@NotNull Chain chain) throws IOException {
                                    Request request = chain.request();
                                    Request.Builder requestBuilder = request.newBuilder()
                                            .header("Authorization", "Bearer " + yelpKey)
                                            .method(request.method(), request.body());
                                    return chain.proceed(requestBuilder.build());
                                }
                            });
                    Retrofit retrofit = new Retrofit.Builder()
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .baseUrl("https://api.yelp.com/v3/")
                            .client(okHttpClient.build())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    YelpService service = retrofit.create(YelpService.class);
                    Call<YelpApiResponse> queryResponse = service.getSuggestion(incompleteQuery);

                    queryResponse.enqueue(new Callback<YelpApiResponse>() {
                        @Override
                        public void onResponse(Call<YelpApiResponse> call, retrofit2.Response<YelpApiResponse> response) {
                            if (response.isSuccessful()) {
                                YelpApiResponse yelpApiResponse = response.body();

                                MatrixCursor cursor = new MatrixCursor(CATEGORY_SEARCH_SUGGEST_COLUMNS);
                                int categoryListSize = yelpApiResponse.categoryList.size();
                                int predictionCounter = 1;
                                while ((predictionCounter - 1) < (categoryListSize - 1)) {
                                    String categoryTitle = yelpApiResponse.categoryList.get(predictionCounter - 1).title;
                                    String categoryAlias = yelpApiResponse.categoryList.get(predictionCounter - 1).alias;
                                    Log.i("findCategorySuggestions", categoryTitle + categoryAlias);
                                    cursor.addRow(new String[]{
                                            Integer.toString(predictionCounter),
                                            categoryTitle,
                                            categoryAlias
                                    });
                                    predictionCounter++;
                                }
                                categorySuggestionAdapter.swapCursor(cursor);
                            }
                        }

                        @Override
                        public void onFailure(Call<YelpApiResponse> call, Throwable t) {
                            MatrixCursor cursor = new MatrixCursor(LOCATION_SEARCH_SUGGEST_COLUMNS);
                            cursor.addRow(new String[]{
                                    "1",
                                    "No categories found",
                                    "No results"
                            });
                            categorySuggestionAdapter.swapCursor(cursor);
                        }
                    });
                }
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                mEvents.clear();
                eventAdapter.notifyDataSetChanged();
                searchCategory = searchSpinner.getSelectedItemPosition();
                if (searchCategory != NO_SEARCH) {
                    if (searchCategory == LOCATION_SEARCH) {
                        Toast.makeText(getApplicationContext(), "Select an address.", Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        Intent searchIntent = new Intent(getApplicationContext(), VisitorSearchActivity.class);
                        searchIntent.putExtra(SearchManager.QUERY, query);
                        searchIntent.putExtra(SEARCH_CATEGORY, searchSpinner.getSelectedItemPosition());
                        searchIntent.setAction(Intent.ACTION_SEARCH);
                        startActivity(searchIntent);
                        // clear focus so search doesn't fire twice
                        searchView.clearFocus();
                        searchView.setQuery(query, false);
                        return true;
                    }
                } else {
                    // prevent submission if no category selected
                    searchView.setQuery(query, false);
                    Toast.makeText(getApplicationContext(), "Select a search category.", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        });
    }
}
