package com.sih.qr_scannerandgenrator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class generator extends AppCompatActivity {
    private RewardedAd mRewardedAd;
    private ImageView qrCodeImage;
    private Button saveButton;
    private Bitmap qrCodeBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generator);

        qrCodeImage = findViewById(R.id.qrCodeImage);
        saveButton = findViewById(R.id.saveButton);
        saveButton.setEnabled(false); // disable initially until ad loads

        // Receive the Bitmap from the MainActivity
        qrCodeBitmap = getIntent().getParcelableExtra("QR_CODE_BITMAP");
        String imagePath = getIntent().getStringExtra("qr_image_path");
        if (qrCodeBitmap != null) {
            qrCodeBitmap = BitmapFactory.decodeFile(imagePath);
            qrCodeImage.setImageBitmap(qrCodeBitmap);
        }

        // Set up the save button logic
        saveButton.setOnClickListener(v -> {
            if (mRewardedAd != null) {
                mRewardedAd.show(generator.this, rewardItem -> {
                    runOnUiThread(() -> {
                        saveQRCodeToGallery(qrCodeBitmap);
                    });
                });

            } else {
                Toast.makeText(this, "Ad not loaded yet. Please try again later.", Toast.LENGTH_SHORT).show();
                loadRewardedAd(); // Try reloading the ad
            }
        });

        loadRewardedAd(); // Load ad on start
    }

    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, "ca-app-pub-4681171705756049/4729180510", adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedAd rewardedAd) {

                       mRewardedAd = rewardedAd;
                        saveButton.setEnabled(true);
                        Log.d("AdMob", "Rewarded ad loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        Log.d("AdMob", "Rewarded ad failed to load: " + loadAdError.getMessage());
                        mRewardedAd = null;
                        saveButton.setEnabled(false);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRewardedAd == null) {
            loadRewardedAd();
        }
    }

    private void saveQRCodeToGallery(Bitmap bitmap) {
        String savedImageURL = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                bitmap,
                "QR_Code_" + System.currentTimeMillis(),
                "Generated QR Code"
        );

        if (savedImageURL != null) {
            Log.d("GallerySave", "Image saved at: " + savedImageURL);
            Toast.makeText(this, "QR Code saved to gallery", Toast.LENGTH_LONG).show();
        } else {
            Log.d("GallerySave", "Failed to save image.");
            Toast.makeText(this, "Failed to save QR code", Toast.LENGTH_SHORT).show();
        }
    }

}
