package com.devosha.mrrandomquotes;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
/*
MainActivity class.
 */
public class MainActivity extends AppCompatActivity {

    //instance fields
    TextView quoteTxt, authorTxt;
    Button nextBtn;
    CardView quoteCard;
    ArrayList<Quote> quotes = new ArrayList<>();
    int count = 0, countclose = 0;
    Dialog dialog, loading;
    CountDownTimer timer;

    class Quote {
        String quote;
        String author;
    }

    /*
    When Activity is created:
    1. reference widgets.
    2. show no network dialog
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        quoteTxt = findViewById(R.id.txtquote);
        authorTxt = findViewById(R.id.txtauthor);
        nextBtn = findViewById(R.id.btnnext);
        quoteCard = findViewById(R.id.qouteCard);

        showSplash();
    }
    /*
    Load quotes and quote author
     */
    public void quoteLoader() {
        quoteCard.setVisibility(View.VISIBLE);

        quoteTxt.setText("\"" + quotes.get(count).quote + "\"");
        authorTxt.setText("~" + quotes.get(count).author);
        nextBtn.setVisibility(View.VISIBLE);
        count++;
    }
    /*
    Check for internet connectivity using ConnectivityManager
     */
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }
    /*
    - Initialize loading dialog and show it
    - Download data and load it to an array
     */
    public void downloadData() {
        loading = new Dialog(MainActivity.this);
        loading.setContentView(R.layout.customloading);
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loading.setCancelable(false);
        LottieAnimationView lottieNoNetwork = loading.findViewById(R.id.lottieloading);
        lottieNoNetwork.setAnimation(R.raw.ripple);
        lottieNoNetwork.playAnimation();
        loading.show();

        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.get("https://andruxnet-random-famous-quotes.p.mashape.com/?cat=famous&count=10")
                .addHeaders("X-Mashape-Key", "paste your  key here")
                .addHeaders("Accept", "application/json")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        loading.dismiss();

                        for (int i = 0; i < response.length(); i++) {

                            try {
                                JSONObject object = response.getJSONObject(i);
                                Quote quote = new Quote();
                                quote.quote = object.getString("quote");
                                quote.author = object.getString("author");
                                quotes.add(quote);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        quoteLoader();

                        nextBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (count < 9) {
                                    quoteLoader();
                                } else if (count == 9) {

                                    quoteLoader();

                                    StateListDrawable drawable = (StateListDrawable) nextBtn.getBackground();
                                    DrawableContainer.DrawableContainerState dcs = (DrawableContainer.DrawableContainerState) drawable.getConstantState();
                                    Drawable[] drawableItems = dcs.getChildren();
                                    GradientDrawable gradientDrawable = (GradientDrawable) drawableItems[0];
                                    gradientDrawable.setStroke(6, getResources().getColor(R.color.red));

                                    nextBtn.setTextColor(getResources().getColor(R.color.red));
                                    nextBtn.setText("EXIT");
                                    nextBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (countclose < 1) {
                                                Toast.makeText(MainActivity.this, "Press again If you want to exit!!", Toast.LENGTH_SHORT).show();
                                                countclose++;
                                            } else {
                                                finish();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                    @Override
                    public void onError(ANError anError) {
                    }
                });
    }
    /*
    show no network dialog or downloadData()
     */
    public void showSplash() {

        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.customdailog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        if (isOnline()) {
            downloadData();
        } else {
            LottieAnimationView lottieNoNetwork = dialog.findViewById(R.id.lottieNoNetwork);
            lottieNoNetwork.setAnimation(R.raw.network_lost);
            lottieNoNetwork.playAnimation();
            dialog.show();

            timer = new CountDownTimer(Long.MAX_VALUE, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (isOnline()) {
                        dialog.dismiss();
                        downloadData();
                        timer.cancel();
                    }
                }
                @Override
                public void onFinish() {
                }
            };
            timer.start();
        }
    }
    //end
}
