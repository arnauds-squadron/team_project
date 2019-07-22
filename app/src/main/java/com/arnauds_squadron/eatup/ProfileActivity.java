package com.arnauds_squadron.eatup;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.arnauds_squadron.eatup.models.Event;
import com.parse.ParseImageView;
import com.bumptech.glide.Glide;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.io.File;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arnauds_squadron.eatup.utils.Constants.AVERAGE_RATING;
import static com.arnauds_squadron.eatup.utils.Constants.BIO;
import static com.arnauds_squadron.eatup.utils.Constants.KEY_PROFILE_PICTURE;
import static com.arnauds_squadron.eatup.utils.Constants.NO_RATING;
import static com.arnauds_squadron.eatup.utils.Constants.NUM_RATINGS;

public class ProfileActivity extends AppCompatActivity {

    private String photoFileName = "photo.jpg";
    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public final String APP_TAG = "Eat Up";
    ParseUser user;
    File photoFile;
    @BindView(R.id.ivProfile)
    ImageView ivProfile;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));

        // load user rating
        Number rating = user.getNumber(AVERAGE_RATING);
        Number numRatings = user.getNumber(NUM_RATINGS);
        if (rating != null) {
            ratingBar.setRating(rating.floatValue());
        }
        else {
            ratingBar.setRating(NO_RATING);
        }
        tvRatings.setText(String.format(Locale.getDefault(),"(%s)", numRatings.toString()));

        // load user profileImage
        ParseFile profileImage = user.getParseFile(KEY_PROFILE_PICTURE);
        if (profileImage != null) {
            Glide.with(getApplicationContext())
                    .load(profileImage.getUrl())
                    .centerCrop()
                    .into(ivProfile);
        }

        if(user.getUsername().equals(ParseUser.getCurrentUser().toString())) {
            btnNewProfileImage.setVisibility(View.VISIBLE);
            btnNewProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onLaunchCamera();
                }
            });
        } else {
            btnNewProfileImage.setVisibility(View.GONE);
        }
        String username = user.getUsername();
        if(user.getUsername() != null) {
            tvUsername.setText(username);
            tvUsername2.setText(username);
        }

        // load user bio
        String bio = user.getString(BIO);
        if (bio != null) {
            tvBio.setText(bio);
        }
        else {
            tvBio.setText(R.string.no_bio);
        }
    }
    public void onLaunchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference to access to future access
        photoFile = getPhotoFileUri(photoFileName);
        Uri fileProvider = FileProvider.getUriForFile(ProfileActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }
        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
        return file;
    }
}