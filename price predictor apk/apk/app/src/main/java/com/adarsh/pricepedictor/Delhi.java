/*
 * Copyright 2020 Adarsh Agrawal. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adarsh.pricepedictor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class Delhi extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private AdView mAdView;
    //private InterstitialAd mInterstitialAd;

    Spinner spinner, furnishSpinner;
    EditText etArea, etBHK, etBathroom;
    Button btnSubmit;
    String fur, local;
    int indexLocal;
    int indexFur;
    double []arr = new double[80];
    Interpreter interpreter;
    TextView price;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delhi);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        //MobileAds.initialize(this, "ca-app-pub-7848184254803724~4921572549");
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-7848184254803724~4921572549");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });

        spinner = findViewById(R.id.spinner);
        price = findViewById(R.id.price);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);



        FirebaseCustomRemoteModel remoteModel =
                new FirebaseCustomRemoteModel.Builder("DelhiPrice").build();
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        // Download complete. Depending on your app, you could enable
                        // the ML feature, or switch from the local model to the remote
                        // model, etc.
                    }
                });

        FirebaseModelManager.getInstance().getLatestModelFile(remoteModel)
                .addOnCompleteListener(new OnCompleteListener<File>() {
                    @Override
                    public void onComplete(@NonNull Task<File> task) {
                        File modelFile = task.getResult();
                        if (modelFile != null) {
                            interpreter = new Interpreter(modelFile);
                        }else{
                            try {
                                InputStream inputStream = getAssets().open("DelhiPrice.tflite");
                                byte[] model = new byte[inputStream.available()];
                                inputStream.read(model);
                                ByteBuffer buffer = ByteBuffer.allocateDirect(model.length)
                                        .order(ByteOrder.nativeOrder());
                                buffer.put(model);
                                interpreter = new Interpreter(buffer);
                            } catch (IOException e) {
                                // File not found?
                            }
                        }
                    }
                });


        etArea = findViewById(R.id.etArea);
        etBathroom = findViewById(R.id.etBathroom);
        etBHK = findViewById(R.id.etBHK);
        furnishSpinner = findViewById(R.id.furnishSpinner);
        btnSubmit = findViewById(R.id.btnSubmit);

        for(int i=0;i<80;i++){
            arr[i] = 0d;
        }

        final ArrayList<String> furnishing = new ArrayList<>();
        furnishing.add("Select furnishing");
        furnishing.add("Furnished");
        furnishing.add("Semi-Furnished");
        furnishing.add("Unfurnished");

        furnishSpinner.setAdapter(new ArrayAdapter<>(Delhi.this,
                android.R.layout.simple_spinner_dropdown_item,furnishing));

        furnishSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                fur = parent.getItemAtPosition(position).toString();
                indexFur = position+2;
                arr[indexFur] = 1d;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayList<String> locality = new ArrayList<>();
        locality.add("Select locality");
        locality.add("Aashirwaad Chowk, Dwarka");
        locality.add("Alaknanda");
        locality.add("Andheria Mor, Mehrauli");
        locality.add("Aravali Apartments, Alaknanda");
        locality.add("Arjun Nagar, Safdarjung Enclave");
        locality.add("Budh Vihar");
        locality.add("Chhattarpur");
        locality.add("Chhattarpur Enclave Phase2");
        locality.add("Chittaranjan Park");
        locality.add("Common Wealth Games Village, Commonwealth Games Village 2010");
        locality.add("Commonwealth Games Village 2010");
        locality.add("DDA Flats Sarita Vihar, Sarita Vihar Pocket K");
        locality.add("DDA Flats Sarita Vihar, Sarita Vihar, Mathura Road");
        locality.add("DDA Flats Sector E Pocket 1, Vasant Kunj");
        locality.add("DDA Lig Flats, Narela");
        locality.add("DLF Capital Greens, New Moti Nagar, Kirti Nagar");
        locality.add("Dilshad Colony, Dilshad Garden");
        locality.add("Geetanjali Enclave, Malviya Nagar");
        locality.add("Godrej South Estate, Okhla");
        locality.add("Godrej South Estate, Okhla Phase 1");
        locality.add("Govindpuri Extension, Kalkaji");
        locality.add("Hauz Khas");
        locality.add("Hauz Khas Enclave, Hauz Khas");
        locality.add("J R Designers Floors, Rohini Sector 24");
        locality.add("Kailash Colony, Greater Kailash");
        locality.add("Kalkaji");
        locality.add("Karol Bagh");
        locality.add("Lajpat Nagar");
        locality.add("Lajpat Nagar 1");
        locality.add("Lajpat Nagar 2");
        locality.add("Lajpat Nagar 3");
        locality.add("Laxmi Nagar");
        locality.add("MTNL Employees House Welfare Society, Dwarka Sector 24");
        locality.add("Maharani Bagh, New Friends Colony");
        locality.add("Mahavir Enclave");
        locality.add("Mahavir Enclave Part 1");
        locality.add("Malviya Nagar");
        locality.add("Mehrauli");
        locality.add("Narela");
        locality.add("Narmada Apartment, Alaknanda");
        locality.add("Naveen Shahdara, Shahdara");
        locality.add("New Friends Colony");
        locality.add("New Manglapuri, Sultanpur");
        locality.add("New Moti Nagar, Kirti Nagar");
        locality.add("Nilgiri Apartment, Alaknanda");
        locality.add("Panchsheel Vihar, Sheikh Sarai");
        locality.add("Paschim Vihar");
        locality.add("Patel Nagar East, Patel Nagar");
        locality.add("Patel Nagar West");
        locality.add("Project Commonwealth Games Village 2010, Commonwealth Games Village 2010");
        locality.add("Punjabi Bagh");
        locality.add("Punjabi Bagh East");
        locality.add("Punjabi Bagh Extension, Punjabi Bagh");
        locality.add("Punjabi Bagh West");
        locality.add("Rohini Sector 20");
        locality.add("Rohini Sector 24");
        locality.add("Safdarjung Development Area, Hauz Khas");
        locality.add("Safdarjung Enclave");
        locality.add("Saket");
        locality.add("Shahdara");
        locality.add("Sheikh Sarai Phase 1");
        locality.add("Shivalik, Malviya Nagar");
        locality.add("Sukhdev Vihar, Okhla");
        locality.add("Sultanpur");
        locality.add("Sultanpur Extension");
        locality.add("The Amaryllis, Karol Bagh");
        locality.add("The Leela Sky Villas, Patel Nagar");
        locality.add("Uttam Nagar");
        locality.add("Vasant Kunj");
        locality.add("Vasant Kunj Sector C");
        locality.add("Vasundhara Enclave");
        locality.add("Vikram Vihar, Lajpat Nagar");
        locality.add("Yamuna Vihar, Shahdara");
        locality.add("other");

        spinner.setAdapter(new ArrayAdapter<>(Delhi.this,
                android.R.layout.simple_spinner_dropdown_item,locality));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                local = parent.getItemAtPosition(position).toString();
                indexLocal = position+5;
                arr[indexLocal] = 1d;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(spinner.getSelectedItemPosition()==0||furnishSpinner.getSelectedItemPosition()==0||
                        etArea.getText().toString().isEmpty() || etBathroom.getText().toString().isEmpty()|| etBHK.getText().toString().isEmpty()){
                    Toast.makeText(Delhi.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }else{
                    String a = etArea.getText().toString().trim();
                    double area = Double.parseDouble(a);
                    area /= 1000;
                    arr[0] = area;
                    String bhk = etBHK.getText().toString().trim();
                    double b = Double.parseDouble(bhk);
                    arr[1]=b;
                    String bathroom = etBathroom.getText().toString();
                    Double bath = Double.parseDouble(bathroom);
                    arr[2] = bath;

                    double [] input =new double[80];
                    for(int i=0;i<80;i++){
                        input[i] = arr[i];
                    }
                    float prediction = calculate(input);
                    String firstNumberAsString = String.format("%.0f", prediction);
                    price.setText(firstNumberAsString);
                    etBHK.setText("");
                    etBathroom.setText("");
                    etArea.setText("");
                    spinner.setSelection(0);
                    furnishSpinner.setSelection(0);
                }
            }
        });
    }
    ////////////////calculations in Machine Learning model////////////////
    private float calculate(double[] input) {

        float[] inputVal = new float[80];
        for(int i=0;i<80;i++){
            inputVal[i] = (float) input[i];
        }
        float[][] outputVal = new float[1][1];
        interpreter.run(inputVal,outputVal);
        float res = outputVal[0][0];
        return  res;
    }

    private MappedByteBuffer loadModelFile() throws IOException{
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("DelhiPrice.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }




}