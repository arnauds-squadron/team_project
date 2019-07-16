package com.arnauds_squadron.eatup.local;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.arnauds_squadron.eatup.R;

import butterknife.BindView;

public class LocalConfirmationActivity extends AppCompatActivity {

    @BindView(R.id.btAccept)
    Button btAccept;
    @BindView(R.id.btAccept)
    Button btDeny;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_confirmation);

        btAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO add database update action
            }
        });

        btDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO add database update action
            }
        });
    }
}
