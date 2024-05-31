package com.talk.walk.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.talk.walk.Adapters.PointsAdapter;
import com.talk.walk.Models.Points;
import com.talk.walk.R;
import com.talk.walk.Utils.Constants;
import com.talk.walk.Utils.Controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class PointsActivity extends AppCompatActivity implements PointsAdapter.OnItemClickListener {

    private Context mContext;

    private static final String TAG = PointsActivity.class.getSimpleName();
    private ImageButton ibBack;
    private TextView tvPointsCounter;
    private Button bFreePoints;
    private CardView cvPoints, cvPointsHeader;
    private ProgressBar pbPointsLoader;

    private RecyclerView rvPoints;
    private PointsAdapter pointsAdapter;
    private List<Points> pointsList = new ArrayList<>();


    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private BillingClient billingClient;
    private PurchasesUpdatedListener purchasesUpdatedListener;
    private InterstitialAd mInterstitialAd;
    private AdRequest adRequest;
    private ConsumeResponseListener acknowledgePurchaseResponseListener;
    private ConsumeParams acknowledgePurchaseParams;
    List<Purchase> public_purchases;
    Points points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points);

        mContext = PointsActivity.this;

        ibBack = findViewById(R.id.ibBack);
        tvPointsCounter = findViewById(R.id.tvPointsCounter);
        bFreePoints = findViewById(R.id.bFreePoints);
        cvPoints = findViewById(R.id.cvPoints);
        cvPointsHeader = findViewById(R.id.cvPointsHeader);
        pbPointsLoader = findViewById(R.id.pbPointsLoader);

        mDatabase = FirebaseDatabase.getInstance(Constants.Urls.DATABASE_URL).getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();

        pbPointsLoader.setVisibility(View.VISIBLE);



//        pointsList.add(new Points(100, 80, "sku_rs80"));
//        pointsList.add(new Points(300, 240, "sku_rs240"));
//        pointsList.add(new Points(540, 370, "sku_rs370"));
//        pointsList.add(new Points(1100, 750, "sku_rs750"));
//        pointsList.add(new Points(2900, 1900, "sku_rs1900"));
//        pointsList.add(new Points(6000, 3700, "sku_rs3700"));
        mDatabase.child("constants").child("points").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    pbPointsLoader.setVisibility(View.GONE);
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Points points = ds.getValue(Points.class);
                        pointsList.add(points);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        rvPoints = findViewById(R.id.rvPoints);
        pointsAdapter = new PointsAdapter(mContext, pointsList, this::onItemClick);
        rvPoints.setLayoutManager(new GridLayoutManager(mContext, 3));
        rvPoints.setAdapter(pointsAdapter);


        rvPoints.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        prepareAd();

        purchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                // To be implemented in a later section.
                if (purchases != null) {
                    acknowledgePurchaseParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchases.get(0).getPurchaseToken())
                            .build();
                    public_purchases = purchases;
                    Log.e(TAG, "onPurchasesUpdated: " + billingResult.getDebugMessage() + " " + purchases);
                    mDatabase.child(Constants.Keys.USERS).child(currentUser.getUid()).child("points").setValue(Integer.parseInt(tvPointsCounter.getText().toString()) + points.getPoints());
                    String transaction_id = mDatabase.push().getKey();
                    Hashtable<String, Object> dataMap = new Hashtable<String, Object>();
                    dataMap.put("transaction_id", transaction_id);
                    dataMap.put("user_id", currentUser.getUid());
                    dataMap.put("order_points_price", points.getPoints_cost());
                    dataMap.put("order_points", points.getPoints());
                    dataMap.put("timestamp", public_purchases.get(0).getPurchaseTime());
                    dataMap.put("purchase_token", public_purchases.get(0).getPurchaseToken());
                    dataMap.put("order_id", public_purchases.get(0).getOrderId());
                    dataMap.put("signature", public_purchases.get(0).getSignature());
                    mDatabase.child(Constants.Keys.USERS).child(currentUser.getUid()).child("payment_transactions").child(transaction_id).setValue(dataMap);
                }
//                if (purchases.get(0).)
            }
        };

        billingClient = BillingClient.newBuilder(PointsActivity.this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        mDatabase.child(Constants.Keys.USERS).child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long points = (long) snapshot.child("points").getValue();
                    tvPointsCounter.setText(String.valueOf(points));
                    if (snapshot.child("points_timeout").exists()) {
                        long points_timeout = (long) snapshot.child("points_timeout").getValue();
                        if (System.currentTimeMillis() < points_timeout) {
                            bFreePoints.setEnabled(false);
                            countDownStart(points_timeout);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getDetails());
            }
        });

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (Controller.Companion.isDarkTheme(this)) {
            cvPointsHeader.setCardBackgroundColor(Color.parseColor("#121212"));
            cvPoints.setCardBackgroundColor(Color.parseColor("#121212"));
        } else {
            cvPointsHeader.setCardBackgroundColor(Color.WHITE);
            cvPoints.setCardBackgroundColor(Color.WHITE);
        }



        acknowledgePurchaseResponseListener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                billingClient.consumeAsync(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);

                Log.e(TAG, "onAcknowledgePurchaseResponse: Purchase acknowledged " + s + billingResult.getDebugMessage());
            }
        };





        bFreePoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bFreePoints.setEnabled(false);
                InterstitialAd.load(mContext, "ca-app-pub-3940256099942544/8691691433", adRequest,
                        new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                // The mInterstitialAd reference will be null until
                                // an ad is loaded.
                                mInterstitialAd = interstitialAd;
                                Log.i(TAG, "onAdLoaded");

                                final Handler handler = new Handler();
                                Timer timer = new Timer();
                                TimerTask doAsynchronousTask = new TimerTask() {
                                    @Override
                                    public void run() {
                                        handler.post(new Runnable() {
                                            public void run() {
                                                if (mInterstitialAd != null) {
                                                    bFreePoints.setEnabled(true);
                                                    mInterstitialAd.show(PointsActivity.this);
                                                    bFreePoints.setEnabled(true);
                                                    mDatabase.child("users").child(currentUser.getUid()).child("points").setValue(Integer.parseInt(tvPointsCounter.getText().toString()) + 10);
                                                    mDatabase.child("users").child(currentUser.getUid()).child("points_timeout").setValue(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(60));
                                                    timer.cancel();
                                                }
                                            }
                                        });
                                    }
                                };
                                timer.schedule(doAsynchronousTask, 0, 10000);
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                // Handle the error
                                Log.i(TAG, loadAdError.getMessage());
                                mInterstitialAd = null;
                                Toast.makeText(mContext, "The ad wasn't ready yet. " + loadAdError.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });


            }
        });
    }

    /*countDownStart() method for start count down*/
    public void countDownStart(long points_timeout) {
//        txtDate.setText(dateString);
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");
        String dateString = formatter.format(new Date(points_timeout));
        formatter.setLenient(false);


        String endTime = dateString;
        long milliseconds=0;

        final CountDownTimer mCountDownTimer;

        Date endDate;
        try {
            endDate = formatter.parse(endTime);
            milliseconds = endDate.getTime();

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        final long[] startTime = {System.currentTimeMillis()};


        mCountDownTimer = new CountDownTimer(milliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                startTime[0] = startTime[0] -1;
                Long serverUptimeSeconds =
                        (millisUntilFinished - startTime[0]) / 1000;

                String daysLeft = String.format("%d", serverUptimeSeconds / 86400);
                //txtViewDays.setText(daysLeft);
                Log.d("daysLeft",daysLeft);

                String hoursLeft = String.format("%d", (serverUptimeSeconds % 86400) / 3600);
                //txtViewHours.setText(hoursLeft);
                Log.d("hoursLeft",hoursLeft);

                String minutesLeft = String.format("%d", ((serverUptimeSeconds % 86400) % 3600) / 60);
                //txtViewMinutes.setText(minutesLeft);
                Log.d("minutesLeft",minutesLeft);

                String secondsLeft = String.format("%d", ((serverUptimeSeconds % 86400) % 3600) % 60);
                //txtViewSecond.setText(secondsLeft);
                Log.d("secondsLeft",secondsLeft);

                bFreePoints.setText("Remaining " + convertDate(Integer.parseInt(minutesLeft)) + ":" + convertDate(Integer.parseInt(secondsLeft)));
//                bFreePoints.setText(String.format("Remaining %02d:%02d", minutesLeft, secondsLeft));

                if (Controller.Companion.isDarkTheme(PointsActivity.this)) {
                    bFreePoints.setTextColor(Color.GRAY);
                } else {
                    bFreePoints.setTextColor(Color.GRAY);
                }

            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    public String convertDate(int input) {
        if (input >= 10) {
            return String.valueOf(input);
        } else {
            return "0" + String.valueOf(input);
        }
    }

    private void prepareAd() {
        adRequest = new AdRequest.Builder().build();
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(getResources().getString(R.string.admob_id));
        MobileAds.initialize(this);
        AdView mAdView = findViewById(R.id.adView);
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onItemClick(Points points, List<Points> pointsList, int position) {
        this.points = points;
        System.out.println("ppppppppppppppppppppppp"+ pointsList + position);
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    List<String> skuList = new ArrayList<>();
                    skuList.add(points.getSku_id());
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                    billingClient.querySkuDetailsAsync(params.build(),
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(BillingResult billingResult,
                                                                 List<SkuDetails> skuDetailsList) {
                                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                            .setSkuDetails(skuDetailsList.get(0))
                                            .build();
                                    int responseCode = billingClient.launchBillingFlow(PointsActivity.this, billingFlowParams).getResponseCode();
                                    Log.e(TAG, "onSkuDetailsResponse: " + String.valueOf(responseCode));

                                }
                            });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                System.out.println("dddddddddddddddddddddddddpayment");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    //session end
    @Override
    protected void onStop() {
        super.onStop();
    }

}