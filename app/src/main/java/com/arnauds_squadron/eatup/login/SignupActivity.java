package com.arnauds_squadron.eatup.login;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.arnauds_squadron.eatup.MainActivity;
import com.arnauds_squadron.eatup.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {

    @BindView(R.id.etUsername)
    EditText etUsername;
    @BindView(R.id.etPassword)
    EditText etPassword;
    @BindView(R.id.etEmail)
    EditText etEmail;
    @BindView(R.id.btnCreate)
    Button btnCreate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ButterKnife.bind(this);


// other fields can be set just like with ParseObject
        //user.put("phone", "650-253-0000");
        btnCreate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ParseUser user = new ParseUser();
                user.setUsername(etUsername.getText().toString());
                user.setPassword(etPassword.getText().toString());
                user.setEmail(etEmail.getText().toString());
                user.signUpInBackground(new SignUpCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.d("SignupActivity", "Login Successful");
                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e("SignupActivity", "Login failure");
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
