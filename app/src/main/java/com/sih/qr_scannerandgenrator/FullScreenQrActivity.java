package com.sih.qr_scannerandgenrator;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class FullScreenQrActivity extends AppCompatActivity {
    private ImageView qrImageView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_qr);

        qrImageView = findViewById(R.id.fullScreenQrImage);

        // Get the Base64 string passed from the HistoryActivity
        String qrBitmapBase64 = getIntent().getStringExtra("qrBitmapBase64");
        if (qrBitmapBase64 != null) {
            Bitmap qrBitmap = decodeBase64ToBitmap(qrBitmapBase64);
            qrImageView.setImageBitmap(qrBitmap); // Set the large QR code
        }
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedByteArray = Base64.decode(base64String, Base64.DEFAULT);
        InputStream inputStream = new ByteArrayInputStream(decodedByteArray);
        return BitmapFactory.decodeStream(inputStream);
    }
}
