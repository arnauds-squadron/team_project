package com.arnauds_squadron.eatup.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.login.LoginActivity;
import com.arnauds_squadron.eatup.models.Rating;
import com.arnauds_squadron.eatup.utils.Constants;
import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.arnauds_squadron.eatup.utils.Constants.BIO;
import static com.arnauds_squadron.eatup.utils.Constants.KEY_PROFILE_PICTURE;
import static com.arnauds_squadron.eatup.utils.Constants.NO_RATING;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    @BindView(R.id.etBio)
    EditText etBio;
    @BindView(R.id.etUsername)
    EditText etUsername;
    @BindView(R.id.tvUsername)
    TextView tvUsername;
    @BindView(R.id.tvUsername2)
    TextView tvUsername2;
    @BindView(R.id.tvBio)
    TextView tvBio;
    @BindView(R.id.tvRatings)
    TextView tvRatings;
    @BindView(R.id.ratingBar)
    RatingBar ratingBar;
    @BindView(R.id.btnNewProfileImage)
    Button btnNewProfileImage;
    @BindView(R.id.btnSave)
    Button btnSave;
    @BindView(R.id.btnCancel)
    Button btnCancel;
    @BindView(R.id.ivLogout)
    ImageView ivLogout;
    @Nullable
    @BindView(R.id.ivImage)
    ImageView ivProfile;
    @BindView(R.id.ivEditUsername)
    ImageView ivEditUsername;
    public static ProfileFragment newInstance() {
        Bundle args = new Bundle();
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        etBio.setVisibility(View.INVISIBLE);
        etUsername.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);
        btnSave.setVisibility(View.INVISIBLE);
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ParseUser user = Constants.CURRENT_USER;

        tvUsername.setClickable(false);
        tvBio.setClickable(false);

        // load user rating
        ParseQuery<Rating> query = new Rating.Query();
        query.whereEqualTo("user", Constants.CURRENT_USER);
        query.findInBackground(new FindCallback<Rating>() {
            public void done(List<Rating> ratings, ParseException e) {
                if (e == null) {
                    if(ratings.size() != 0) {
                        Rating rating = ratings.get(0);
                        float averageRating = rating.getAvgRatingHost().floatValue();
                        int numRatings = rating.getNumRatingsHost().intValue();
                        ratingBar.setRating(averageRating);
                        tvRatings.setText(String.format(Locale.getDefault(),"(%s)", numRatings));
                    }
                    else {
                        ratingBar.setRating(NO_RATING);
                    }
                } else {
                    Toast.makeText(getContext(),"Query for rating not successful",Toast.LENGTH_LONG).show();
                }
            }
        });

        // load user profileImage
        ParseFile profileImage = user.getParseFile(KEY_PROFILE_PICTURE);
        if (profileImage != null) {
            Glide.with(this)
                    .load(profileImage.getUrl())
                    .into(ivProfile);
        }
        String username = user.getUsername();
        if(user.getUsername() != null) {
            tvUsername.setText(username);
            tvUsername2.setText(username);
            etUsername.setText(username);
        }

        // load user bio
        String bio = user.getString(BIO);
        if (bio != null) {
            tvBio.setText(bio);
            etBio.setText(bio);
        }
        else {
            tvBio.setText(R.string.no_bio);
        }
    }
    //todo update user profile photo
//    @OnClick(R.id.btnNewProfileImage)
//    public void setNewProfileImage () {
//        int permissionState = ActivityCompat.checkSelfPermission(getActivity(),
//                Manifest.permission.CAMERA);
//        if (permissionState != PackageManager.PERMISSION_GRANTED) {
//            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA);
//
//        }
//        Intent cInt = new Intent(getActivity(), ProfileImageActivity.class);
//        startActivity(cInt);
//    }
    @OnClick(R.id.ivEditUsername)
    public void editUsername () {
        tvBio.setVisibility(View.INVISIBLE);
        tvUsername.setVisibility(View.INVISIBLE);
        etUsername.setVisibility(View.VISIBLE);
        etBio.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.VISIBLE);
    }
    @OnClick(R.id.btnSave)
    public void saveChanges () {
        //todo make changes update to ParseDashboard
        ParseUser user = Constants.CURRENT_USER;
        if (!user.getUsername().equals(etUsername.toString())) {
            if (etUsername.toString().isEmpty()) {
                user.setUsername(tvUsername.getText().toString());
            } else {
                user.setUsername(etUsername.getText().toString());
            }
        }
        if (!etBio.toString().equals(user.getString(BIO))) {
            if (etBio.toString().isEmpty()) {
                user.put(BIO, tvBio.getText().toString());
            } else {
                user.put(BIO, etBio.getText().toString());
            }
        }
        tvUsername.setText(etUsername.getText());
        tvBio.setText(etBio.getText());
        tvBio.setVisibility(View.VISIBLE);
        tvUsername.setVisibility(View.VISIBLE);
        etBio.setVisibility(View.INVISIBLE);
        etUsername.setVisibility(View.INVISIBLE);
        btnSave.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);
    }
    @OnClick(R.id.btnCancel)
    public void cancelChanges () {
        tvBio.setVisibility(View.VISIBLE);
        tvUsername.setVisibility(View.VISIBLE);
        etUsername.setVisibility(View.INVISIBLE);
        etUsername.setText(tvUsername.getText());
        etBio.setVisibility(View.INVISIBLE);
        etBio.setText(tvBio.getText());
        btnCancel.setVisibility(View.INVISIBLE);
        btnSave.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.ivLogout)
    public void logout () {
        logoutUser();
        gotoLoginActivity();
    }

    private void logoutUser() {
        ParseUser.logOut();
        Constants.CURRENT_USER = null;
    }

    private void gotoLoginActivity() {
        Intent i = new Intent(getActivity(), LoginActivity.class);
        startActivity(i);
        getActivity().finish();
    }
}
