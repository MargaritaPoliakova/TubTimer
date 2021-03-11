package com.application.tubtimer.connection;

import android.content.DialogInterface;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.adroitandroid.near.connect.NearConnect;
import com.adroitandroid.near.discovery.NearDiscovery;
import com.adroitandroid.near.model.Host;
import com.application.tubtimer.activities.SearchActivity;
import com.application.tubtimer.adapters.DeviceAdapter;

import java.util.ArrayList;
import java.util.Set;

public class DiscoveryManager {
    private static final long DISCOVERABLE_TIMEOUT_MILLIS = 60000; //вызывается после makeDiscoverable() (стать видимым)
    private static final long DISCOVERY_TIMEOUT_MILLIS = 10000; // вызывается после startDiscovery()
    private static final long DISCOVERABLE_PING_INTERVAL_MILLIS = 5000;
    public static final String MESSAGE_REQUEST_CONNECT = "connect";
    public static final String MESSAGE_REQUEST_CONNECT_FROM_HOST = "connect_from_host";
    public static final String MESSAGE_REQUEST_HOST = "are_you_host?";

    public static final String MESSAGE_RESPONSE_ACCEPT_CONNECT = "accept_request";
    public static final String MESSAGE_RESPONSE_NOT_HOST = "not_host";
    
    public static final String MESSAGE_DISCONNECT = "disconnect";
    public static final String MESSAGE_REQUEST_PING = "ping";

    public static final String MESSAGE_RESPONSE_DECLINE_CONNECT = "decline_request";

    public static boolean host = false;
    
    SearchActivity activity;
    public static NearDiscovery nearDiscovery;
    public static NearConnect nearConnection;
    

    public static ArrayList<Host> connectedHosts;
    ArrayList<Host> foundHosts = new ArrayList<>();
    public DeviceAdapter adapter;


    public DiscoveryManager(final SearchActivity activity) {
        this.activity = activity;
        adapter = new DeviceAdapter(activity);

        if (connectedHosts==null)connectedHosts = new ArrayList<>();
        foundHosts.addAll(connectedHosts);
        if (host)adapter.setData(foundHosts);
        else  if (!connectedHosts.isEmpty())activity.connectedDevice.setText("Подключено к "+connectedHosts.get(0).getName());

        if (isConnected())activity.button_search.setText("отключиться");


        if (nearDiscovery==null)
            nearDiscovery = new NearDiscovery.Builder()
                    .setContext(activity.getApplicationContext())
                    .setDiscoverableTimeoutMillis(DISCOVERABLE_TIMEOUT_MILLIS)
                    .setDiscoveryTimeoutMillis(DISCOVERY_TIMEOUT_MILLIS)
                    .setDiscoverablePingIntervalMillis(DISCOVERABLE_PING_INTERVAL_MILLIS)
                    .setDiscoveryListener(getNearDiscoveryListener(), Looper.getMainLooper())
                    .build();
        if (nearConnection==null)
            nearConnection = new NearConnect.Builder()
                    .fromDiscovery(nearDiscovery)
                    .setContext(activity.getApplicationContext())
                    .setListener(getNearConnectListener(), Looper.getMainLooper())
                    .build();

    }
    
    public boolean isConnected(){
        return host||!connectedHosts.isEmpty();
    }
    








    @NonNull
    private NearDiscovery.Listener getNearDiscoveryListener() {
        return new NearDiscovery.Listener() {
            @Override
            public void onPeersUpdate(Set<? extends Host> hosts) {

                for (Host host: hosts){
                    if (!isConnected()&&!connectedHosts.contains(host))
                        nearConnection.send(MESSAGE_REQUEST_HOST.getBytes(),host);
                }
                
            }


            @Override
            public void onDiscoveryTimeout() {
                if (foundHosts.isEmpty())Toast.makeText(activity, "Устройства не найдены", Toast.LENGTH_LONG).show();
                activity.bar.setVisibility(View.GONE);
            }

            @Override
            public void onDiscoveryFailure(Throwable e) {
                Toast.makeText(activity, "Что-то пошло не так", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDiscoverableTimeout() {

            }
        };
    }


    @NonNull
    private NearConnect.Listener getNearConnectListener() {
        return new NearConnect.Listener() {
            @Override
            public void onReceive(byte[] bytes, final Host sender) {
                if (bytes != null) {
                    String message = new String(bytes);
                    switch (message){
                        case MESSAGE_REQUEST_HOST:
                            if (!foundHosts.contains(sender)){
                                foundHosts.add(sender);
                                adapter.setData(foundHosts);
                                nearConnection.send(MESSAGE_RESPONSE_NOT_HOST.getBytes(), sender);
                            }
                            break;
                        case MESSAGE_RESPONSE_NOT_HOST:
                            if (isConnected())break;
                            if (!foundHosts.contains(sender)) {
                                foundHosts.add(sender);
                                adapter.setData(foundHosts);
                                nearConnection.send(MESSAGE_RESPONSE_NOT_HOST.getBytes(), sender);
                            }
                            break;
                            
                            
                        case MESSAGE_REQUEST_CONNECT:
                            new AlertDialog.Builder(activity)
                                    .setMessage(sender.getName() + " хочет подключиться к вам")
                                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            nearConnection.send(MESSAGE_REQUEST_CONNECT_FROM_HOST.getBytes(), sender);
                                        }
                                    })
                                    .setNegativeButton("Нет", null).create().show();
                            break;
                        case MESSAGE_REQUEST_CONNECT_FROM_HOST:
                            if (isConnected())break;
                            nearConnection.send(MESSAGE_RESPONSE_ACCEPT_CONNECT.getBytes(), sender);
                            activity.recycler.setVisibility(View.GONE);
                            activity.connectedDevice.setVisibility(View.VISIBLE);
                            activity.connectedDevice.setText("Подключено к "+sender.getName());

                            connectedHosts.add(sender);
                            activity.button_search.setText("отключиться");
                            break;
                        case MESSAGE_RESPONSE_ACCEPT_CONNECT:
                            host = true;
                            connectedHosts.add(sender);
                            activity.button_search.setText("отключиться");
                            adapter.setData(connectedHosts);
                            break;
                            
                        case MESSAGE_DISCONNECT:
                            connectedHosts.remove(sender);
                            disconnect();
                            break;
                        case MESSAGE_REQUEST_PING:
//                            Toast.makeText(activity.getApplicationContext(),"Ping", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }

            @Override
            public void onStartListenFailure(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onSendFailure(Throwable throwable, long l) {
                throwable.printStackTrace();
            }

            @Override
            public void onSendComplete(long l) {

            }

        };
    }

    public void sendConnectRequest(Host host) {
        nearConnection.send(MESSAGE_REQUEST_CONNECT.getBytes(), host);
    }

    public void stop() {
        nearDiscovery.stopDiscovery();
        nearConnection.stopReceiving(true);
    }

    public void start(String name){
        foundHosts.clear();
        if (!nearDiscovery.isDiscoverable())nearDiscovery.makeDiscoverable(name,"");
        nearDiscovery.startDiscovery();
        if (!nearConnection.isReceiving()) {
            nearConnection.startReceiving();
        }
    }

    public void disconnect() {
        activity.button_search.setText("поиск");
        activity.recycler.setVisibility(View.VISIBLE);
        activity.connectedDevice.setVisibility(View.GONE);
        host = false;
        for (Host host: connectedHosts)
            nearConnection.send(MESSAGE_DISCONNECT.getBytes(), host);
        connectedHosts.clear();
        adapter.setData(foundHosts);
    }
}
