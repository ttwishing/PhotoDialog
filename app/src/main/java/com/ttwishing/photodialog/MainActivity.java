package com.ttwishing.photodialog;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.ttwishing.photodialog.library.PhotoDialogFragment;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView imageView = (ImageView) findViewById(R.id.image_view);
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.aaa_test);
        imageView.setImageBitmap(bitmap);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoDialogFragment.showDialog(MainActivity.this, new Bundle(), imageView, new BitmapDrawable(getResources(), bitmap));
            }
        });
    }
}
