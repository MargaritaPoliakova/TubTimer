package com.application.tubtimer.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;
import androidx.room.Room;

import com.adroitandroid.near.connect.NearConnect;
import com.adroitandroid.near.connect.NearConnectImpl;
import com.adroitandroid.near.model.Host;
import com.application.tubtimer.R;
import com.application.tubtimer.adapters.TubeAdapter;
import com.application.tubtimer.connection.Command;
import com.application.tubtimer.connection.CommandManager;
import com.application.tubtimer.connection.DiscoveryManager;
import com.application.tubtimer.database.AppDatabase;
import com.application.tubtimer.database.DatabaseManager;
import com.application.tubtimer.database.Timer;
import com.application.tubtimer.fragments.TubeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public AppDatabase database;
    public DatabaseManager manager;
    public MyService myService;
    boolean bound;
    Intent intent;
    ServiceConnection sConn;
    public TubeFragment tubeFragment = new TubeFragment();

    CommandManager commandManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        commandManager = new CommandManager(this);
        tubeFragment.commandManager = commandManager;

        getSupportFragmentManager().beginTransaction().add(R.id.container, tubeFragment).commit();
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                tubeFragment.changeFragment(item.getItemId());
                return true;
            }
        });

        database = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "tube_database2")
                .allowMainThreadQueries()
                .build();

        manager = new DatabaseManager(this);

        intent = new Intent(getApplicationContext(), MyService.class);
        sConn = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d("my", "MainActivity onServiceConnected");
                myService = ((MyService.MyBinder) binder).getService();
                bound = true;
                myService.nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(myService.NOTIF_ID,myService.NOTIF_ID, NotificationManager.IMPORTANCE_DEFAULT);
                    myService.nm.createNotificationChannel(channel);
                }
//                myService.sendNotification("Я родился!");

            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d("my", "MainActivity onServiceDisconnected");
                bound = false;
            }
        };
        bindService(intent, sConn, BIND_AUTO_CREATE);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        ArrayList<Host> connectedHosts = data.getParcelableArrayListExtra(SearchActivity.DEVICES);


        commandManager.notifyAllHosts();
        if (DiscoveryManager.connectedHosts!=null) {
//            commandManager.setPeers(new ArraySet<>(connectedHosts));
            Log.d("my","connect");
            Log.d("my",DiscoveryManager.connectedHosts.size()+"");

        }else Log.d("my","null");
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void onClick(View view) {
//        SearchActivity.start(this, commandManager.nearConnection==null? null: new ArrayList<>(commandManager.nearConnection.getPeers()));

    }

    public void onClick2(View view) {
        /*if (commandManager.nearConnection!=null)
            for (Host host:commandManager.nearConnection.getPeers()) {
                commandManager.nearConnection.send(CommandManager.MESSAGE_REQUEST_PING.getBytes(), host);
                Log.d("my",commandManager.nearConnection.getPeers().size()+"");

            }*/
//        commandManager.sendAll();
       /* Timer timer = tubeFragment.repairTubeAdapter.timers.get(0);
        timer = new Timer(timer.number,timer.duration,timer.duration,timer.type,false);

        commandManager.getNearConnectListener()
                .onReceive(new Command(Command.ACTION_DELETE, timer).getBytes(),null);*/
    }

    @Override
    protected void onDestroy() {
        for(Timer timer:manager.getByType(Timer.TUBE_ON_TRACK)) {
            manager.dao.update(timer);
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        for(Timer timer:manager.getByType(Timer.TUBE_ON_TRACK)) {
            manager.dao.update(timer);
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.about_prog:
                intent = new Intent(MainActivity.this, AboutProgramActivity.class);
                startActivity(intent);
                break;
            case R.id.exit:
                MainActivity.this.finish();
                break;
            case R.id.web:
                SearchActivity.start(this);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
    }
}