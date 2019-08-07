package com.arnauds_squadron.eatup.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.models.Chat;
import com.arnauds_squadron.eatup.models.Event;
import com.arnauds_squadron.eatup.utils.Constants;
import com.parse.FindCallback;
import com.parse.ParseException;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment that displays the user's RSVP'd events and is the fragment to accept or deny guests.
 * Serves as an agenda and constantly refreshes to stay up to date.
 */
public class HomeFragment extends Fragment implements
        NoEventsScheduledFragment.OnFragmentInteractionListener {

    @BindView(R.id.rvAgenda)
    RecyclerView rvAgenda;

    @BindView(R.id.flNoEventsScheduled)
    FrameLayout flNoEventsScheduled;

    @BindView(R.id.tvNoEventsScheduled)
    TextView tvNoEventsScheduled;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.spinner)
    Spinner spinner;

    private OnFragmentInteractionListener mListener;
    private List<Event> agenda;
    private HomeAdapter homeAdapter;

    private boolean refreshRunnableNotStarted = false;
    // Handler to post the runnable on the Looper's queue every second
    private Handler updateHandler = new Handler();
    // Refresh runnable that refreshes the messages every second
    private Runnable refreshEventsRunnable = new Runnable() {
        @Override
        public void run() {
            //refreshEventsAsync(0);
            updateHandler.postDelayed(this, Constants.EVENT_UPDATE_SPEED_MILLIS);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        flNoEventsScheduled.setVisibility(View.INVISIBLE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        agenda = new ArrayList<>();
        // construct adapter from data source
        homeAdapter = new HomeAdapter(getContext(), this, agenda);
        // RecyclerView setup
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        rvAgenda.setLayoutManager(layoutManager);
        rvAgenda.setAdapter(homeAdapter);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        rvAgenda.addItemDecoration(itemDecoration);
        //swipe to delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rvAgenda);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getContext(), R.array.host, R.layout.spinner_item1);
        spinner.setAdapter(adapter);
        String value = spinner.getSelectedItem().toString();
        setSpinnerToValue(spinner, value);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshEventsAsync(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        startUpdatingEvents();
    }

    public void setSpinnerToValue(Spinner spinner, String value) {
        int index = 0;
        SpinnerAdapter spinnerAdapter = spinner.getAdapter();
        for (int i = 0; i < spinnerAdapter.getCount(); i++) {
            if (spinnerAdapter.getItem(i).equals(value)) {
                index = i;
                break; // terminate loop
            }
        }
        spinner.setSelection(index);
    }

    /**
     * Attaches the listener to the MainActivity
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement the interface");
        }
    }

    /**
     * // TODO: also show pending events?
     * Fetches all events from the Parse server and filters if the current user is the host
     * or is an accepted guest
     */
    public void refreshEventsAsync(int filterType) {
        // List that represents the timeline if it were to be updates. The timeline only updates
        // when this list is different from the current timeline.
        final List<Event> tempEvents = new ArrayList<>();

        final String userId = Constants.CURRENT_USER.getObjectId();
        final Event.Query query = new Event.Query();

        if (filterType == 1) {
            query.withHost().ownEvent(Constants.CURRENT_USER);
        } else if (filterType == 2) {
            query.withHost().notOwnEvent(Constants.CURRENT_USER);
        }
        query.orderByDescending("date");
        query.findInBackground(new FindCallback<Event>() {
            @Override
            public void done(List<Event> objects, ParseException e) {
                if (e == null) {
                    for (final Event event : objects) {
                        final JSONArray guests = event.getAcceptedGuests();
                        String hostId = event.getHost().getObjectId();
                        if (userId.equals(hostId) || (guests != null &&
                                guests.toString().contains(userId)))
                            tempEvents.add(event);
                    }
                    if (tempEvents.size() != agenda.size()) { // Only update if events changed
                        agenda.clear();
                        agenda.addAll(tempEvents);
                        homeAdapter.notifyDataSetChanged();
                    }
                    if (agenda.size() == 0) {
                        tvNoEventsScheduled.setVisibility(View.VISIBLE);
                        flNoEventsScheduled.setVisibility(View.VISIBLE);
                    } else {
                        tvNoEventsScheduled.setVisibility(View.INVISIBLE);
                        flNoEventsScheduled.setVisibility(View.INVISIBLE);
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }
    //swipe to delete


    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

        ColorDrawable background = new ColorDrawable(Color.RED);

        //        private Drawable icon = ContextCompat.getDrawable(homeAdapter.getContext(),
//                R.drawable.ic_home_light);
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            Toast.makeText(getContext(), "on Move", Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder target, int swipeDir) {
//            Toast.makeText(getContext(), "on Swiped ", Toast.LENGTH_SHORT).show();
            //Remove swiped item from list and notify the RecyclerView
            final int position = target.getAdapterPosition();
            final Event item = agenda.get(position);
            agenda.remove(position);
            homeAdapter.notifyDataSetChanged();
            Snackbar snackbar = Snackbar.make(rvAgenda, "DELETED!", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            agenda.add(position, item);
                            homeAdapter.notifyDataSetChanged();
                        }
                    });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX,
                    dY, actionState, isCurrentlyActive);
            View itemView = viewHolder.itemView;
//            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
//            int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
//            int iconBottom = iconTop + icon.getIntrinsicHeight();
            int backgroundCornerOffset = 20;
            if (dX > 0) { // Swiping to the right
//                int iconLeft = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
//                int iconRight = itemView.getLeft() + iconMargin;
//                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                background.setBounds(itemView.getLeft(), itemView.getTop(),
                        itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                        itemView.getBottom());

            } else if (dX < 0) { // Swiping to the left
//                int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
//                int iconRight = itemView.getRight() - iconMargin;
//                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                        itemView.getTop(), itemView.getRight(), itemView.getBottom());
            } else { // view is unSwiped
//                icon.setBounds(0, 0, 0, 0);
                background.setBounds(0, 0, 0, 0);
            }
            background.draw(c);
//            icon.draw(c);
        }
    };

    /**
     * Called by the HomeAdapter to open an event's chat
     */
    public void openChat(Chat chat) {
        mListener.switchToChatFragment(chat);
    }

    /**
     * Called by the parent Activity to stop updating the events when the user is logged out.
     */
    public void stopUpdatingEvents() {
        updateHandler.removeCallbacks(refreshEventsRunnable);
        refreshRunnableNotStarted = false;
    }

    /**
     * Method called to start the runnable so the events are constantly refreshing.
     */
    private void startUpdatingEvents() {
        // TODO: change to progress bar in the middle?
//        refreshEventsAsync(0);

        if (!refreshRunnableNotStarted) { // only one runnable
            refreshEventsRunnable.run();
            refreshRunnableNotStarted = true;
        }
    }

    @Override
    public void navigateToFragment(int index) {
        mListener.navigateToFragment(index);
    }

    public interface OnFragmentInteractionListener {
        /**
         * When the chat button is clicked it goes to that event's chat in the ChatDashboardFragment
         *
         * @param chat The details of the chat to open
         */
        void switchToChatFragment(Chat chat);

        /**
         * Simply navigates to the fragment at the given index in the MainActivity's ViewPager
         *
         * @param index The index of the fragment in the ViewPager
         */
        void navigateToFragment(int index);
    }
}
