package com.sih.qr_scannerandgenrator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private RewardedAd mRewardedAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize Mobile Ads SDK
        MobileAds.initialize(this, initializationStatus -> {});

        // Load Banner Ad
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                Log.d("AdMob", "Banner Ad failed to load: " + adError.getMessage());
            }
        });

        // Load Interstitial Ad
        loadInterstitialAd();

        EditText inputText = findViewById(R.id.inputText);
        Button generateButton = findViewById(R.id.generateButton);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        generateButton.startAnimation(slideUp);
        ImageView qrCodeImage = findViewById(R.id.qrCodeImage);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        qrCodeImage.startAnimation(fadeIn);
        Button scanButton = findViewById(R.id.scanButton);

        // Generate QR Code
        // Inside the generateButton click listener
        generateButton.setOnClickListener(v -> {
            String text = inputText.getText().toString();
            if (!text.isEmpty()) {
                try {
                    // Generate QR Bitmap
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 300, 300);
                    qrCodeImage.setImageBitmap(bitmap);
                    // Save history to Firebase Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    // Convert QR bitmap to Base64 string
                    String qrBitmapBase64 = encodeBitmapToBase64(bitmap);

                    Map<String, Object> qrData = new HashMap<>();
                    qrData.put("text", text);
                    qrData.put("type", "generated");
                    qrData.put("timestamp", new Date());
                    qrData.put("qrBitmap", qrBitmapBase64); // Save Base64 QR code image

                    if (user != null) {
                        db.collection("users")
                                .document(user.getUid())
                                .collection("qr_history")
                                .add(qrData);
                    }

                    // Send Base64 string to next screen if needed
                    File cachePath = new File(getCacheDir(), "images");
                    cachePath.mkdirs();
                    File file = new File(cachePath, "qr_code.png");
                    FileOutputStream stream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.close();

                    Intent intent = new Intent(this, generator.class);
                    intent.putExtra("qr_image_path", file.getAbsolutePath());
                    startActivity(intent);

                } catch (Exception e) {
                    Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Input cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });



        // Open Scanner Activity
        scanButton.setOnClickListener(v -> {
            // Show Interstitial Ad before opening Scanner Activity
            if (mInterstitialAd != null) {
                mInterstitialAd.show(MainActivity.this);
            } else {
                openScannerActivity();
            }
        });
    }

    private String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, "ca-app-pub-4681171705756049/4401969345", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.d("AdMob", "Interstitial ad loaded");

                        // Set the AdListener here after the ad is loaded
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Once the interstitial ad is dismissed, open the Scanner Activity
                                openScannerActivity();
                                // Reload the interstitial ad for the next time
                                loadInterstitialAd();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                Log.d("AdMob", "Interstitial ad failed to show: " + adError.getMessage());
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // This is triggered when the ad is shown
                                Log.d("AdMob", "Interstitial ad showed.");
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        Log.d("AdMob", "Failed to load interstitial ad: " + loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
    }

    // Open the Scanner Activity
    private void openScannerActivity() {
        Intent intent = new Intent(MainActivity.this, Scnanner.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdView != null) {
            mAdView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdView != null) {
            mAdView.destroy();
        }
    }
}
