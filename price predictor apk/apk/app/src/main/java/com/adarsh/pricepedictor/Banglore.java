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

public class Banglore extends AppCompatActivity {

    Spinner spinnerb;
    EditText etAreab, etBHKb, etBathroomb;
    Button btnPrice;
    String  local;
    int indexLocal;

    double []arr = new double[243];
    Interpreter interpreter;
    TextView priceb;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "Banglore";
    private AdView mAdView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banglore);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-7848184254803724~4921572549");
// TODO: Add adView to your view hierarchy.
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

        spinnerb = findViewById(R.id.spinnerb);
        priceb = findViewById(R.id.priceb);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        FirebaseCustomRemoteModel remoteModel =
                new FirebaseCustomRemoteModel.Builder("BanglorePrice").build();//////////
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
                                InputStream inputStream = getAssets().open("BanglorePrice.tflite");///////
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

        etAreab = findViewById(R.id.etAreab);
        etBathroomb = findViewById(R.id.etBathroomb);
        etBHKb = findViewById(R.id.etBHKb);
        btnPrice = findViewById(R.id.btnPrice);

        for(int i=0;i<243;i++){///////////
            arr[i] = 0d;
        }

        ArrayList<String> locality = new ArrayList<>();
        locality.add("Select locality");
        locality.add("1st Block Jayanagar");
        locality.add("1st Phase JP Nagar");
        locality.add("2nd Phase Judicial Layout");
        locality.add("2nd Stage Nagarbhavi");
        locality.add("5th Block Hbr Layout");
        locality.add("5th Phase JP Nagar");
        locality.add("6th Phase JP Nagar");
        locality.add("7th Phase JP Nagar");
        locality.add("8th Phase JP Nagar");
        locality.add("9th Phase JP Nagar");
        locality.add("AECS Layout");
        locality.add("Abbigere");
        locality.add("Akshaya Nagar");
        locality.add("Ambalipura");
        locality.add("Ambedkar Nagar");
        locality.add("Amruthahalli");
        locality.add("Anandapura");
        locality.add("Ananth Nagar");
        locality.add("Anekal");
        locality.add("Anjanapura");
        locality.add("Ardendale");
        locality.add("Arekere");
        locality.add("Attibele");
        locality.add("BEML Layout");
        locality.add("BTM 2nd Stage");
        locality.add("BTM Layout");
        locality.add("Babusapalaya");
        locality.add("Badavala Nagar");
        locality.add("Balagere");
        locality.add("Banashankari");
        locality.add("Banashankari Stage II");
        locality.add("Banashankari Stage III");
        locality.add("Banashankari Stage V");
        locality.add("Banashankari Stage VI");
        locality.add("Banaswadi");
        locality.add("Banjara Layout");
        locality.add("Bannerghatta");
        locality.add("Bannerghatta Road");
        locality.add("Basavangudi");
        locality.add("Basaveshwara Nagar");
        locality.add("Battarahalli");
        locality.add("Begur");
        locality.add("Begur Road");
        locality.add("Bellandur");
        locality.add("Benson Town");
        locality.add("Bharathi Nagar");
        locality.add("Bhoganhalli");
        locality.add("Billekahalli");
        locality.add("Binny Pete");
        locality.add("Bisuvanahalli");
        locality.add("Bommanahalli");
        locality.add("Bommasandra");
        locality.add("Bommasandra Industrial Area");
        locality.add("Bommenahalli");
        locality.add("Brookefield");
        locality.add("Budigere");
        locality.add("CV Raman Nagar");
        locality.add("Chamrajpet");
        locality.add("Chandapura");
        locality.add("Channasandra");
        locality.add("Chikka Tirupathi");
        locality.add("Chikkabanavar");
        locality.add("Chikkalasandra");
        locality.add("Choodasandra");
        locality.add("Cooke Town");
        locality.add("Cox Town");
        locality.add("Cunningham Road");
        locality.add("Dasanapura");
        locality.add("Dasarahalli");
        locality.add("Devanahalli");
        locality.add("Devarachikkanahalli");
        locality.add("Dodda Nekkundi");
        locality.add("Doddaballapur");
        locality.add("Doddakallasandra");
        locality.add("Doddathoguru");
        locality.add("Domlur");
        locality.add("Dommasandra");
        locality.add("EPIP Zone");
        locality.add("Electronic City");
        locality.add("Electronic City Phase II");locality.add("Electronics City Phase 1");
        locality.add("Frazer Town");locality.add("GM Palaya");
        locality.add("Garudachar Palya");locality.add("Giri Nagar");
        locality.add("Gollarapalya Hosahalli");locality.add("Gottigere");
        locality.add("Green Glen Layout");locality.add("Gubbalala");
        locality.add("Gunjur");locality.add("HAL 2nd Stage");
        locality.add("HBR Layout");locality.add("HRBR Layout");
        locality.add("HSR Layout");locality.add("Haralur Road");
        locality.add("Harlur");locality.add("Hebbal");
        locality.add("Hebbal Kempapura");locality.add("Hegde Nagar");
        locality.add("Hennur");locality.add("Hennur Road");
        locality.add("Hoodi");locality.add("Horamavu Agara");
        locality.add("Horamavu Banaswadi");locality.add("Hormavu");
        locality.add("Hosa Road");locality.add("Hosakerehalli");
        locality.add("Hoskote");locality.add("Hosur Road");
        locality.add("Hulimavu");locality.add("ISRO Layout");
        locality.add("ITPL");locality.add("Iblur Village");
        locality.add("Indira Nagar");
        locality.add("JP Nagar");locality.add("Jakkur");
        locality.add("Jalahalli");locality.add("Jalahalli East");
        locality.add("Jigani");locality.add("Judicial Layout");
        locality.add("KR Puram");locality.add("Kadubeesanahalli");
        locality.add("Kadugodi");locality.add("Kaggadasapura");locality.add("Kaggalipura");
        locality.add("Kaikondrahalli");locality.add("Kalena Agrahara");
        locality.add("Kalyan nagar");locality.add("Kambipura");
        locality.add("Kammanahalli");locality.add("Kammasandra");
        locality.add("Kanakapura");locality.add("Kanakpura Road");
        locality.add("Kannamangala");locality.add("Karuna Nagar");
        locality.add("Kasavanhalli");locality.add("Kasturi Nagar");
        locality.add("Kathriguppe");locality.add("Kaval Byrasandra");
        locality.add("Kenchenahalli");locality.add("Kengeri");
        locality.add("Kengeri Satellite Town");locality.add("Kereguddadahalli");
        locality.add("Kodichikkanahalli");locality.add("Kodigehaali");
        locality.add("Kodigehalli");locality.add("Kodihalli");
        locality.add("Kogilu");
        locality.add("Konanakunte");locality.add("Koramangala");
        locality.add("Kothannur");
        locality.add("Kothanur");
        locality.add("Kudlu");
        locality.add("Kudlu Gate");
        locality.add("Kumaraswami Layout");
        locality.add("Kundalahalli");
        locality.add("LB Shastri Nagar");
        locality.add("Laggere");locality.add("Lakshminarayana Pura");
        locality.add("Lingadheeranahalli");
        locality.add("Magadi Road");locality.add("Mahadevpura");
        locality.add("Mahalakshmi Layout");locality.add("Mallasandra");
        locality.add("Malleshpalya");locality.add("Malleshwaram");
        locality.add("Marathahalli");locality.add("Margondanahalli");
        locality.add("Marsur");locality.add("Mico Layout");
        locality.add("Munnekollal");locality.add("Murugeshpalya");
        locality.add("Mysore Road");locality.add("NGR Layout");
        locality.add("NRI Layout");locality.add("Nagarbhavi");
        locality.add("Nagasandra");locality.add("Nagavara");
        locality.add("Nagavarapalya");locality.add("Narayanapura");
        locality.add("Neeladri Nagar");locality.add("Nehru Nagar");
        locality.add("OMBR Layout");locality.add("Old Airport Road");
        locality.add("Old Madras Road");locality.add("Padmanabhanagar");
        locality.add("Pai Layout");locality.add("Panathur");
        locality.add("Parappana Agrahara");locality.add("Pattandur Agrahara");
        locality.add("Poorna Pragna Layout");locality.add("Prithvi Layout");
        locality.add("R.T. Nagar");locality.add("Rachenahalli");
        locality.add("Raja Rajeshwari Nagar");locality.add("Rajaji Nagar");
        locality.add("Rajiv Nagar");locality.add("Ramagondanahalli");
        locality.add("Ramamurthy Nagar");locality.add("Rayasandra");
        locality.add("Sahakara Nagar");locality.add("Sanjay nagar");
        locality.add("Sarakki Nagar");locality.add("Sarjapur");
        locality.add("Sarjapur  Road");locality.add("Sarjapura - Attibele Road");
        locality.add("Sector 2 HSR Layout");locality.add("Sector 7 HSR Layout");
        locality.add("Seegehalli");locality.add("Shampura");
        locality.add("Shivaji Nagar");locality.add("Singasandra");
        locality.add("Somasundara Palya");locality.add("Sompura");
        locality.add("Sonnenahalli");locality.add("Subramanyapura");
        locality.add("Sultan Palaya");locality.add("TC Palaya");
        locality.add("Talaghattapura");locality.add("Thanisandra");
        locality.add("Thigalarapalya");locality.add("Thubarahalli");
        locality.add("Tindlu");locality.add("Tumkur Road");
        locality.add("Ulsoor");locality.add("Uttarahalli");
        locality.add("Varthur");locality.add("Varthur Road");
        locality.add("Vasanthapura");locality.add("Vidyaranyapura");
        locality.add("Vijayanagar");locality.add("Vishveshwarya Layout");
        locality.add("Vishwapriya Layout");locality.add("Vittasandra");
        locality.add("Whitefield");locality.add("Yelachenahalli");
        locality.add("Yelahanka");locality.add("Yelahanka New Town");
        locality.add("Yelenahalli");locality.add("Yeshwanthpur");



        spinnerb.setAdapter(new ArrayAdapter<>(Banglore.this,
                android.R.layout.simple_spinner_dropdown_item,locality));

        spinnerb.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                local = parent.getItemAtPosition(position).toString();
                indexLocal = position+2;
                arr[indexLocal] = 1;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(spinnerb.getSelectedItemPosition()==0|| etAreab.getText().toString().isEmpty() ||
                        etBathroomb.getText().toString().isEmpty()|| etBHKb.getText().toString().isEmpty()){
                    Toast.makeText(Banglore.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }else{
                    String a = etAreab.getText().toString().trim();
                    double area = Double.parseDouble(a);
                    area /= 1000;
                    arr[0] = area;
                    String bhk = etBHKb.getText().toString().trim();
                    double b = Double.parseDouble(bhk);
                    arr[2]=b;
                    String bathroom = etBathroomb.getText().toString();
                    double bath = Double.parseDouble(bathroom);
                    arr[1] = bath;

                    double [] input =new double[243];////////
                    for(int i=0;i<243;i++){//////
                        input[i] = arr[i];
                    }
                    float prediction = calculate(input);
                    prediction = prediction*100000;
                    String firstNumberAsString = String.format("%.0f", prediction);
                    priceb.setText(firstNumberAsString);
                    etBHKb.setText("");
                    etBathroomb.setText("");
                    etAreab.setText("");
                    spinnerb.setSelection(0);
                }
            }
        });
    }

    private float calculate(double[] input) {
        float[] inputVal = new float[243];/////
        for(int i=0;i<243;i++){
            inputVal[i] = (float) input[i];
        }
        float[][] outputVal = new float[1][1];
        interpreter.run(inputVal,outputVal);
        float res = outputVal[0][0];
        return res;
    }

    private MappedByteBuffer loadModelFile() throws IOException{
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("BanglorePrice.tflite");/////
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