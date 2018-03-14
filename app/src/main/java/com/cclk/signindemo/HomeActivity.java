package com.cclk.signindemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity {

    TextView tvFirstName, tvLastName, email, gender;
    ImageView ivProfilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // data bundle from login screen
        Bundle bundle = getIntent().getExtras();

        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        uiInitialize();

        if (bundle!= null) {
            String stringPicUrl = bundle.getString("profile_pic").toString();
            String stringFirstName = bundle.getString("first_name").toString();
            String stringLastName = bundle.getString("last_name").toString();
            String stringEmail = bundle.getString("email").toString();
            String stringGender = bundle.getString("gender").toString();

            Picasso.with(HomeActivity.this).load(stringPicUrl).fit().into(ivProfilePic);
            tvFirstName.setText(stringFirstName);
            tvLastName.setText(stringLastName);
            email.setText(stringEmail);
            gender.setText(stringGender);
        }

    }

    /**
     * Initialise UI controls
     */
    private void uiInitialize() {

        ivProfilePic = (ImageView) findViewById(R.id.image_profile_fb);
        tvFirstName = (TextView) findViewById(R.id.first_name);
        tvLastName = (TextView) findViewById(R.id.last_name);
        email = (TextView) findViewById(R.id.email);
        gender = (TextView) findViewById(R.id.gender);
    }

}
