package com.johnberry.assignment3walkingtour;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
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
    private TextView addressTextView, descriptText;
    private ImageView buildingImage;
    private Typeface myCustomFont;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        myCustomFont = Typeface.createFromAsset(getAssets(), "fonts/Acme-Regular.ttf");

        Intent i = getIntent();
        selectedFence = (FenceData) i.getSerializableExtra("FD");



        titleTextView = findViewById(R.id.titleText);
        titleTextView.setText(selectedFence.getId());

        addressTextView = findViewById(R.id.addrTextView);
        addressTextView.setText(selectedFence.getAddress());

        descriptText = findViewById(R.id.descripText);
        descriptText.setText(selectedFence.getDescription());

        buildingImage = findViewById(R.id.buildingImage);

        titleTextView.setTypeface(myCustomFont);
        addressTextView.setTypeface(myCustomFont);
        descriptText.setTypeface(myCustomFont);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notif_menu, menu);
        return true;
    }
}