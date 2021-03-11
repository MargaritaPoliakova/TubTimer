package com.application.tubtimer.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.adroitandroid.near.model.Host;
import com.application.tubtimer.R;
import com.application.tubtimer.connection.DiscoveryManager;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    public RecyclerView recycler;
    public DiscoveryManager discoveryManager;
    public TextView connectedDevice;
    public ProgressBar bar;
    public Button button_search;

    public final static String DEVICES = "manager";

    public static void start(MainActivity activity) {
        Intent intent = new Intent(activity, SearchActivity.class);
//        intent.putParcelableArrayListExtra(DEVICES, connectedHosts);
        activity.startActivityForResult(intent, 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        recycler = findViewById(R.id.recycler_devices);
        connectedDevice = findViewById(R.id.connected_device);
        bar = findViewById(R.id.device_bar);
        button_search = findViewById(R.id.search);

        discoveryManager = new DiscoveryManager(this);

        recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recycler.setAdapter(discoveryManager.adapter);

        button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!discoveryManager.isConnected()) {
                    discoveryManager.start(Settings.Secure.getString(getContentResolver(), "bluetooth_name"));
                    bar.setVisibility(View.VISIBLE);
                }else {
                    discoveryManager.disconnect();
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        bar.setVisibility(View.GONE);
//        discoveryManager.stop();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putParcelableArrayListExtra(DEVICES, discoveryManager.connectedHosts);
        finishActivity(1);
        setResult(RESULT_OK, intent);
        finish();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}