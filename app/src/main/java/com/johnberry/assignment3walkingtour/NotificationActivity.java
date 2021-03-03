package com.johnberry.assignment3walkingtour;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class NotificationActivity extends Activity {
    private FenceData selectedFence;
    private TextView titleTextView;
    private TextView addressTextView;
    private ImageView buildingImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Intent i = getIntent();
        selectedFence = (FenceData) i.getSerializableExtra("FD");

        titleTextView = findViewById(R.id.titleText);
        titleTextView.setText(selectedFence.getId());

        buildingImage = findViewById(R.id.buildingImage);

        System.out.println("Here is the title: " + selectedFence.getId());

        loadImage();
    }


    public void loadImage() {
        Picasso.get().load(selectedFence.getImageURL())
                .into(buildingImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        BitmapDrawable drawable = (BitmapDrawable) buildingImage.getDrawable();
                        Bitmap bitmap = drawable.getBitmap();
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });

    }
}