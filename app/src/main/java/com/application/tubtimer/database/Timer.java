package com.application.tubtimer.database;

import android.os.CountDownTimer;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


@Entity
public class Timer{
    public static final int TUBE_FREE=0;
    public static final int TUBE_ON_TRACK=1;
    public static final int TUBE_IN_REPAIR=2;

    @Ignore
    CountDownTimer downTimer;
    @Ignore
    TickListener listener;
    @PrimaryKey
    public int number;     //numberOfTube
    public int duration;     // in seconds
    public int time_left;
    public int type;
    public boolean activated;
    @Ignore
    boolean tick;

    public void setOnTickListener(TickListener listener) {
        this.listener = listener;
    }

    public Timer(int number, int duration, int time_left, int type, boolean activated) {
        this.number = number;
        this.duration = duration;
        this.time_left = time_left;
        this.type = type;
        if (activated)start();
    }


    @Ignore
    public Timer(int number, int seconds, int type) {
        duration = seconds;
        time_left = seconds;
        this.number = number;
        activated= false;
        this.type = type;
    }


    public void start(){
        activated = true;
        type = TUBE_ON_TRACK;
        downTimer = new CountDownTimer(time_left*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                time_left = (int) (millisUntilFinished/1000);
                if (listener!=null&&tick)listener.onTick(time_left);
            }

            @Override
            public void onFinish() {
                time_left = duration;
                activated = false;
                if (listener!=null)listener.onFinish();
            }
        }.start();
    }

    public void stop(){
        listener = null;
        time_left = duration;
        activated = false;
        type = TUBE_FREE;
        if (downTimer!=null)downTimer.cancel();
    }

    public void pause(){
        activated = false;
        downTimer.cancel();
    }

    public void setTime(int sec){
        duration = sec;
        time_left = sec;
    }

    public void setTick(boolean tick) {
        this.tick = tick;
    }

    public String getTimeString(){
        return getTimeString(time_left);
    }

    public static String getTimeString(int time){
        String time_string="";
        int t = time / 3600;
        if (t!=0)time_string+=t +"ч ";
        t =(time / 60) % 60;
        if (t!=0)time_string+=t+"мин ";

        if (time_string.equals("")) {
            t = (time % 60);
            if (t != 0) time_string += t + "сек";
        }
        return time_string;
    }

    public interface TickListener{
        void onTick(int secondsUntilFinished);
        void onFinish();
    }
}
