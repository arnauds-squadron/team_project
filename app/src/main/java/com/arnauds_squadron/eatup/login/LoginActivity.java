package com.arnauds_squadron.eatup.login;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.arnauds_squadron.eatup.MainActivity;
import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.utils.Constants;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.etUsername)
    TextInputEditText etUsername;

    @BindView(R.id.etPassword)
    TextInputEditText etPassword;

    @BindView(R.id.btnLogin)
    Button btnLogin;

    @BindView(R.id.btnSignup)
    Button btnSignup;

    @BindView(R.id.tvError)
    TextView tvError;

    private Handler messageHandler;
    private Runnable messageRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.LoginTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        // Persist user if possible
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            Constants.CURRENT_USER = currentUser;
            goToMainActivity();
        }

        // Set the hidden password behavior
        etPassword.setTypeface(Typeface.DEFAULT);
        etPassword.setTransformationMethod(new PasswordTransformationMethod());
        // Clicking on the check mark in the soft keyboard will attempt to log you in
        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login();
                    return true;
                }
                return false;
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        messageHandler = new Handler();
        messageRunnable = new Runnable() {
            @Override
            public void run() {
                tvError.setAlpha(tvError.getAlpha() - 0.05f);
                if (tvError.getAlpha() > 0)
                    messageHandler.postDelayed(this, 20);
            }
        };
    }

    /**
     * Attempts to login the user through the Parse server with the given username and password
     */
    private void login() {
        final String username = etUsername.getText().toString();
        final String password = etPassword.getText().toString();

        if (username.trim().isEmpty()) {
            showMessage("Enter a username");
        } else if (password.trim().isEmpty()) {
            showMessage("Enter a password");
        } else {
            ParseUser.logInInBackground(username, password, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e == null) {
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        if (currentUser != null) {
                            showMessage("Logging in...");
                            user.setUsername(username);
                            Constants.CURRENT_USER = currentUser;
                            goToMainActivity();
                        } else {
                            showMessage("Something went wrong");
                        }

                    } else {
                        showMessage("Error logging in");
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * Sets the error TextView to show the text provided, and starts a runnable that slowly fades
     * the message out
     *
     * @param message The message to display
     */
    private void showMessage(String message) {
        tvError.setText(message);
        tvError.setAlpha(1);
        messageHandler.removeCallbacks(messageRunnable);
        messageHandler.postDelayed(messageRunnable, 1000);
    }

    /**
     * Goes to the MainActivity if the user is logged in successfully
     */
    private void goToMainActivity() {
        Log.d("LoginActivity", "Login Successful");
        final Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
