package com.example.runnersDataCollection;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.runnersDataCollection.R;

public class MainActivity extends AppCompatActivity {

    private Button next, back, done, stop;
    private EditText fName, age, weight, height, force;
    private RelativeLayout firstScreen, secondScreen;
    private TextView counter, chrono;
    private CountDownTimer countDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        SharedPreferences sp = getSharedPreferences("Current User", MainActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        done = (Button) findViewById(R.id.btnDone);
        next = (Button) findViewById(R.id.btnNext);
        stop = (Button) findViewById(R.id.btnStop);
        back = (Button) findViewById(R.id.btnBack);
        
        fName = (EditText) findViewById(R.id.editName);
        age = (EditText) findViewById(R.id.editAge);
        weight = (EditText) findViewById(R.id.editWeight);
        height = (EditText) findViewById(R.id.editHeight);
        force = (EditText) findViewById(R.id.editForce);

        firstScreen = (RelativeLayout) findViewById(R.id.firstScreen);
        secondScreen = (RelativeLayout) findViewById(R.id.secondScreen);

        counter = (TextView) findViewById(R.id.counter);
        chrono = (TextView) findViewById(R.id.chrono);


        Intent serviceIntent;
        serviceIntent = new Intent(this, SensorBackgroundService.class);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Showing the back button so we can go back and do new user data
                next.setVisibility(View.GONE);
                back.setVisibility(View.VISIBLE);

                // Getting strings from editText in intent extras and shared preferences
                String s_name = fName.getText().toString();
                serviceIntent.putExtra("name", s_name);
                editor.putString("name", s_name);
                String s_age = age.getText().toString();
                serviceIntent.putExtra("age", s_age);
                editor.putString("age", s_age);
                String s_height = height.getText().toString();
                serviceIntent.putExtra("height", s_height);
                editor.putString("height", s_height);
                String s_weight = weight.getText().toString();
                serviceIntent.putExtra("weight", s_weight);
                editor.putString("weight", s_weight);

                editor.commit();

                // Passing to the second screen to enter the percentage of the force
                firstScreen.setVisibility(View.GONE);
                secondScreen.setVisibility(View.VISIBLE);

                // Enable the "Done" button so we can start the service
                done.setEnabled(true);
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // sending data to the service by intent
                String s_force = force.getText().toString();
                serviceIntent.putExtra("force", s_force);
                editor.putString("force",s_force);
                editor.commit();

                // Disable the second screen so we get count down to start collecting data
                secondScreen.setVisibility(View.GONE);

                // Setting the counter visible
                counter.setVisibility(View.VISIBLE);

               // stop.setVisibility(View.VISIBLE);
               // stop.setEnabled(false);
                back.setVisibility(View.GONE);
                done.setVisibility(View.GONE);


                // After the count down we start the service and collect our data as needed
                startServiceWithCountDown(serviceIntent, 6000);

            }
        });

        if (isDataCollectionRunning(SensorBackgroundService.class)){

            firstScreen.setVisibility(View.GONE);
            next.setVisibility(View.GONE);
            back.setVisibility(View.VISIBLE);


            // Disable the second screen so we get count down to start collecting data
            secondScreen.setVisibility(View.GONE);

            // Setting the counter visible
            counter.setVisibility(View.VISIBLE);
            counter.setText("Run");

            // Setting stop button to visible
            //stop.setVisibility(View.VISIBLE);
            //stop.setEnabled(true);
            back.setEnabled(false);
            done.setVisibility(View.GONE);

            // if the app is killed we get the user's data saved as shared preferences
            serviceIntent.putExtra("name", sp.getString("name", "-1"));
            serviceIntent.putExtra("age", sp.getString("age", "-1"));
            serviceIntent.putExtra("height", sp.getString("height", "-1"));
            serviceIntent.putExtra("weight", sp.getString("weight", "-1"));
            serviceIntent.putExtra("force", sp.getString("force", "-1"));


        }

       /* stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Here we will stop collecting the data and go back to the first screen
                stopService(serviceIntent);

                // go back to enter percentage of force
                secondScreen.setVisibility(View.VISIBLE);
                counter.setVisibility(View.GONE);
                stop.setVisibility(View.GONE);
                done.setVisibility(View.VISIBLE);
                done.setEnabled(true);
                back.setEnabled(true);

            }
        });*/

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Showing the next button and hide the back button
                next.setVisibility(View.VISIBLE);
                back.setVisibility(View.GONE);

                // Passing back to the first screen to re-enter user's data
                firstScreen.setVisibility(View.VISIBLE);
                secondScreen.setVisibility(View.GONE);

                // Disable the Done button
                done.setEnabled(false);

            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // This function can be used to check if the service is running.
    // Usage : isDataCollectionRunning(myService.class)
    public boolean isDataCollectionRunning(Class<?> serviceClass) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startServiceWithCountDown(Intent serviceIntent, int duration){
        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                if (seconds==1) {
                    counter.setText("RUN");
                }
                counter.setText(String.valueOf(seconds));
            }
            @Override
            public void onFinish() {
                counter.setText("Run");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                }
                counter.setVisibility(View.GONE);

                new Handler().postDelayed(new Runnable() {
                    int remainingSeconds = 30;

                    @Override
                    public void run() {
                        counter.setVisibility(View.GONE);
                        chrono.setVisibility(View.VISIBLE);
                        remainingSeconds--;
                        chrono.setText("Time Remaining: 00:" + remainingSeconds);

                        if (remainingSeconds > 0) {
                            // Continue countdown
                            new Handler().postDelayed(this, 1000);
                        } else {
                            // Stop service and update UI
                            stopService(serviceIntent);
                            secondScreen.setVisibility(View.VISIBLE);
                            counter.setVisibility(View.GONE);
                            done.setVisibility(View.VISIBLE);
                            done.setEnabled(true);
                            back.setVisibility(View.VISIBLE);
                            back.setEnabled(true);
                            chrono.setVisibility(View.GONE);
                        }
                    }
                }, 1000);
            }

        };
        countDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the countdown timer to prevent memory leaks
        if (countDownTimer != null){
            countDownTimer.cancel();
        }
    }

}