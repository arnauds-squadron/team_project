package com.arnauds_squadron.eatup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.arnauds_squadron.eatup.models.Event;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    Event event;
    @BindView(R.id.etUsername)
    EditText etUsername;
    @BindView(R.id.etPassword)
    EditText etPassword;
    @BindView(R.id.btnLogin)
    Button btnLogin;
    @BindView(R.id.btnSignup)
    Button btnSignup;
    public static String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            //do stuff with the user
            Log.d("LoginActivity", "Login Successful");
            final Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();
                login(username, password);
            }
        });
    }

    private void login(final String username, String password) {
        ParseUser.becomeInBackground("session-token-here", new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    user = ParseUser.getCurrentUser();
                    event.setHost(user);

                } else {
                    Log.e("LoginActivity", "Unknown user");
                }
            }
        });
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    if (currentUser != null) {
                        name = username;
                        user.setUsername(username);
                        event.setHost(currentUser);
                        //do stuff with the user
                        Log.d("LoginActivity", "Login Successful");
                        final Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                } else {
                    Log.e("LoginActivty", "Login failure");
                    e.printStackTrace();
                }
            }
        });
        final Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
    }

}
