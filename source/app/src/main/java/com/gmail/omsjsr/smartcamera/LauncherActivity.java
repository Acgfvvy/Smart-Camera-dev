/*
 * Copyright 2020 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.omsjsr.smartcamera;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Collections;
import java.util.List;

public class LauncherActivity extends com.google.androidbrowserhelper.trusted.LauncherActivity {

    private ConsentInformation consentInformation;
    private AdView adView; // keep as field to destroy in onDestroy

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fix orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        // Root layout: FrameLayout to overlay banner on PWA
        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        setContentView(rootLayout);

        // Start UMP consent flow
        requestConsent(rootLayout);
    }

    private void requestConsent(FrameLayout rootLayout) {
        ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();
        consentInformation = UserMessagingPlatform.getConsentInformation(this);

        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                () -> {
                    if (consentInformation.isConsentFormAvailable()) {
                        loadConsentForm(rootLayout);
                    } else {
                        initAfterConsent(rootLayout, false);
                    }
                },
                formError -> {
                    Log.e("UMP", "Consent form error: " + formError.getMessage());
                    initAfterConsent(rootLayout, false);
                }
        );
    }

    private void loadConsentForm(FrameLayout rootLayout) {
        UserMessagingPlatform.loadConsentForm(
                this,
                consentForm -> {
                    boolean consentGiven = consentInformation.getConsentStatus()
                            == ConsentInformation.ConsentStatus.OBTAINED;
                    if (consentInformation.getConsentStatus()
                            == ConsentInformation.ConsentStatus.REQUIRED) {
                        consentForm.show(this, formError -> {
                            if (formError != null) {
                                Log.e("UMP", "Consent form error: " + formError.getMessage());
                            }
                            initAfterConsent(rootLayout, false);
                        });
                    } else {
                        initAfterConsent(rootLayout, consentGiven);
                    }
                },
                formError -> {
                    Log.e("UMP", "Consent form error: " + formError.getMessage());
                    initAfterConsent(rootLayout, false);
                }
        );
    }

    private void initAfterConsent(FrameLayout rootLayout, boolean consentGiven) {
        // Initialize Firebase after consent
        FirebaseApp.initializeApp(this);
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(consentGiven);

        // ✅ Set your device as test device BEFORE initializing MobileAds
        List<String> testDeviceIds = Collections.singletonList("4A71A01E21FF673E3BA3E28391D474B1"); // from logcat
        RequestConfiguration configuration = new RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build();
        MobileAds.setRequestConfiguration(configuration);

        // Initialize AdMob
        MobileAds.initialize(this, initializationStatus -> {
            adView = new AdView(this);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(getString(R.string.admob_banner_id));

            // Layout params to align banner at the bottom
            FrameLayout.LayoutParams adParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            adParams.gravity = Gravity.BOTTOM;

            rootLayout.addView(adView, adParams);

            AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
            if (!consentGiven) {
                // Non-personalized ads if consent not given
                android.os.Bundle extras = new android.os.Bundle();
                extras.putString("npa", "1");
                adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
            }

            // ✅ Debug listener for ads
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    Log.d("AdMob", "Ad loaded successfully ✅");
                }

                @Override
                public void onAdFailedToLoad(@NonNull com.google.android.gms.ads.LoadAdError adError) {
                    Log.e("AdMob", "Ad failed to load ❌: " + adError.getMessage());
                }

                @Override
                public void onAdOpened() {
                    Log.d("AdMob", "Ad opened");
                }

                @Override
                public void onAdClosed() {
                    Log.d("AdMob", "Ad closed");
                }
            });

            // Load the ad
            adView.loadAd(adRequestBuilder.build());

            // Launch TWA after a short delay
            new Handler(Looper.getMainLooper()).postDelayed(this::launchTwaWrapper, 500);

        });
    }

    // Wrapper to avoid clash with superclass
    private void launchTwaWrapper() {
        super.launchTwa(); // calls the original TWA launcher
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }
}
