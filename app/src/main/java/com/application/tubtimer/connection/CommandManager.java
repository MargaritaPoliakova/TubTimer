package com.application.tubtimer.connection;

import android.content.DialogInterface;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.collection.ArraySet;

import com.adroitandroid.near.connect.NearConnect;
import com.adroitandroid.near.model.Host;
import com.application.tubtimer.activities.MainActivity;
import com.application.tubtimer.adapters.TubeAdapter;
import com.application.tubtimer.database.DatabaseManager;
import com.application.tubtimer.database.Timer;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class CommandManager {

    public final static String UPDATE_ALL = "$update";
    public final static String REQUEST_UPDATE_ALL = "request_update";
    public final static String REQUEST_DELETE = "request_update";
    public static final String MESSAGE_REQUEST_PING = "ping";

    static final int pingTime = 5;

    MainActivity main;
    public NearConnect nearConnection;
    private HashMap<Host, Boolean> connectionStatus;

    public void setNearConnection(NearConnect nearConnection) {
        this.nearConnection = nearConnection;
    }

    public CommandManager(MainActivity main) {
        this.main = main;
    }

    public void setPeers(final ArraySet<Host> peers){
        nearConnection = new NearConnect.Builder()
                .forPeers(peers)
                .setContext(main.getApplicationContext())
                .setListener(getNearConnectListener(), Looper.getMainLooper()).build();
        nearConnection.startReceiving();
    }

    @NonNull
    public NearConnect.Listener getNearConnectListener() {
        return new NearConnect.Listener() {
            @Override
            public void onReceive(@NotNull byte[] bytes, @NotNull final Host sender) {
                try {
                    if (!connectionStatus.containsKey(sender)){
                        connectionStatus.put(sender,false);
                        sendPingRequest(sender);
                        Toast.makeText(main,sender.getName()+" подключён", Toast.LENGTH_SHORT).show();
                    }
                    String message = new String(bytes);
                    Log.d("my", "receive " + message);
                    if (message.startsWith("&")) {
                        final Command command = Command.parseCommand(message);
                        final TubeAdapter activeAdapter = (TubeAdapter) main.tubeFragment.recycler.getAdapter();
                        switch (command.action) {
                            case Command.ACTION_ADD:
                                /*if (DiscoveryManager.host) {
                                    new AlertDialog.Builder(main)
                                            .setMessage(sender.getName() + " хочет добавить таймер №" + command.timer.number)
                                            .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (main.manager.timerNotExist(command.timer)) {
                                                        main.manager.insert(command.timer);
                                                        if (command.timer.type == activeAdapter.type)
                                                            activeAdapter.notifyItemInserted(0);
                                                    }
                                                    send(Command.ACTION_ADD, command.timer);
                                                }
                                            })
                                            .setNegativeButton("Нет", null).create().show();
                                }else {*/
                                    if (main.manager.timerNotExist(command.timer)) {
                                        main.manager.insert(command.timer);
                                        if (command.timer.type == activeAdapter.type)
                                            activeAdapter.notifyItemInserted(0);

                                    }
                                //}
                                break;
                            case Command.ACTION_CHANGE:
                                main.manager.change(command.timer, activeAdapter);
                                break;
                            case Command.ACTION_STOP:
                                if (DiscoveryManager.host){
                                    new AlertDialog.Builder(main)
                                            .setMessage(sender.getName()+" хочет убрать таймер №"+command.timer.number)
                                            .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    stop(command.timer, activeAdapter);
                                                    send(Command.ACTION_STOP, command.timer);
                                                }
                                            })
                                            .setNegativeButton("Нет", null).create().show();
                                }else {
                                    stop(command.timer,activeAdapter);
                                    main.tubeFragment.recycler.smoothScrollToPosition(0);
                                }

                                break;
                            case Command.ACTION_DELETE:

                                if (DiscoveryManager.host){
                                    new AlertDialog.Builder(main)
                                            .setMessage(sender.getName()+" хочет удалить таймер №"+command.timer.number)
                                            .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    delete(command.timer);
                                                    send(Command.ACTION_DELETE,command.timer);
                                                    activeAdapter.notifyDataSetChanged();
                                                }
                                            })
                                            .setNegativeButton("Нет", null).create().show();
                                }else {
                                    delete(command.timer);
                                    activeAdapter.notifyDataSetChanged();
                                }


                                break;
                        }
                    } else if (message.startsWith(UPDATE_ALL)) {
                        message = message.substring(UPDATE_ALL.length());
                        UpdateAllHelper.updateAllAdapters(main, message);
                    }else switch (message){
                        case MESSAGE_REQUEST_PING:
//                            Toast.makeText(main.getApplicationContext(),"Ping", Toast.LENGTH_SHORT).show();
                            connectionStatus.replace(sender,true);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep((pingTime - 2) * 1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    sendPingRequest(sender);
                                }
                            }).start();

                            break;
                        case REQUEST_UPDATE_ALL:
                            sendAll();
                            break;

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onSendComplete(long jobId) {

            }

            @Override
            public void onSendFailure(@org.jetbrains.annotations.Nullable Throwable e, long jobId) {

            }

            @Override
            public void onStartListenFailure(@org.jetbrains.annotations.Nullable Throwable e) {

            }
        };
    }

    public void sendPingRequest(final Host host){
        nearConnection.send(MESSAGE_REQUEST_PING.getBytes(), host);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(pingTime * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (connectionStatus.get(host)){
                    connectionStatus.replace(host, false);
                    Log.d("my",host.getName()+" connectionStatus ok");
                } else {
                    connectionStatus.remove(host);
                    main.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(main,host.getName()+" отключён", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();

    }

    public boolean canChangeTimers(){
        return DiscoveryManager.host||nearConnection==null||DiscoveryManager.connectedHosts.size()==0;
    }

    void delete(Timer timer){
        main.manager.delete(timer);
        out:
        for (int i = 0; i < 3; i++) {
            ArrayList<Timer> list = main.manager.getByType(i);
            for (int j = 0; j < list.size(); j++) {
                Timer t = list.get(j);
                if (t.number==timer.number){
                    t.stop();
                    list.remove(j);
                    break out;
                }
            }
        }
    }

    void stop(Timer timer, TubeAdapter activeAdapter){
        DatabaseManager.FindTimerHelper helper = main.manager.findTimerByNumber(timer);
        helper.timers.set(helper.position,helper.timer);
        activeAdapter.stopTimer(helper.timer);

        main.manager.dao.update(helper.timer);
        activeAdapter.notifyDataSetChanged();
    }

    public void requestUpdateAll(){
        if (!DiscoveryManager.host)
            for (Host host: DiscoveryManager.connectedHosts)
                nearConnection.send(REQUEST_UPDATE_ALL.getBytes(),host);
    }

    public void notifyAllHosts(){
        setNearConnection(DiscoveryManager.nearConnection);
        nearConnection.setListener(getNearConnectListener());
        requestUpdateAll();
        sendAll();
        connectionStatus = new HashMap<>();

        for (Host host: DiscoveryManager.connectedHosts)
            connectionStatus.put(host, false);

        if (DiscoveryManager.host)
            for (Host host: DiscoveryManager.connectedHosts)
                sendPingRequest(host);


    }

    public void send(int action , Timer timer){
        if(nearConnection==null/*||!DiscoveryManager.host*/){
            Log.d("my","send null");
            return;
        }
        Log.d("my","send continue");

        byte[] bytes = new Command(action, timer).getBytes();
        for (Host host: DiscoveryManager.connectedHosts) {
            nearConnection.send(bytes, host);
        }
        
    }

    public void sendAll(){
        if (!DiscoveryManager.host)return;
        byte[] bytes =(UPDATE_ALL+new UpdateAllHelper(main.manager.getByType(Timer.TUBE_ON_TRACK),
                main.manager.getByType(Timer.TUBE_FREE),main.manager.getByType(Timer.TUBE_IN_REPAIR))
                .getData()).getBytes();
        for (Host host: DiscoveryManager.connectedHosts) {
            nearConnection.send(bytes, host);
        }
    }

    static class UpdateAllHelper{
        static Gson gson = new Gson();;
        public ArrayList<Timer> track;
        public ArrayList<Timer> free;
        public ArrayList<Timer> repair;

        public UpdateAllHelper(ArrayList<Timer> track, ArrayList<Timer> free, ArrayList<Timer> repair) {
            this.track = track;
            this.free = free;
            this.repair = repair;
        }

        String getData(){
            return gson.toJson(this, UpdateAllHelper.class);
        }

        static void updateAllAdapters(final MainActivity main, String data){
            UpdateAllHelper helper = gson.fromJson(data, UpdateAllHelper.class);
            main.manager.setLists(helper.track, helper.free, helper.repair);

            for (final Timer timer:helper.track){
                timer.setOnTickListener(new Timer.TickListener() {
                    @Override
                    public void onTick(int secondsUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        main.tubeFragment.trackTubeAdapter.finishTimer(timer);
                    }
                });
                if (timer.activated)timer.start();
            }

            TubeAdapter adapter = (TubeAdapter) main.tubeFragment.recycler.getAdapter();
            adapter.notifyDataSetChanged();
            adapter.checkEmpty();
        }
    }
}
