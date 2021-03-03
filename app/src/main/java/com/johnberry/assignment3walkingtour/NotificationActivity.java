package com.johnberry.assignment3walkingtour;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class NotificationActivity extends Activity {
    private FenceData selectedFence;
    private TextView titleTextView;
    private TextView addressTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Intent i = getIntent();
        selectedFence = (FenceData) i.getSerializableExtra("FD");

        titleTextView = findViewById(R.id.titleText);
        titleTextView.setText(selectedFence.getId());

        System.out.println("Here is the title: " + selectedFence.getId());


    }
}