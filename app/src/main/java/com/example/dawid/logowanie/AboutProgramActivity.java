package com.example.dawid.logowanie;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Dawid on 22.04.2017.
 */

public class AboutProgramActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about_program);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
