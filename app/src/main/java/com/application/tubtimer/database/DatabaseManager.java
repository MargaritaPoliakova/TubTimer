package com.application.tubtimer.database;

import android.util.Log;

import com.application.tubtimer.activities.MainActivity;
import com.application.tubtimer.adapters.TubeAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DatabaseManager{
    public TimerDao dao;
    private ArrayList<Timer> track;
    private ArrayList<Timer> free;
    private ArrayList<Timer> repair;

    MainActivity main;
    public DatabaseManager(MainActivity main) {
        this.main = main;
        this.dao = main.database.timerDao();
    }

    public List<Timer> getAll(){
        return null;
    }

    
    public Timer getByNumber(int number) {
        for (int i = 0; i < 3; i++) {
            for (Timer t:getByType(i)){
                if (t.number==number)return t;
            }
        }
        return null;
    }

    public boolean timerNotExist(Timer timer){
        return timerNotExist(timer.number);
    }

    public boolean timerNotExist(int number){
        return dao.getByNumber(number)==null;
    }

    public FindTimerHelper findTimerByNumber(Timer timer){
        for (int i = 0; i < 3; i++) {
            ArrayList<Timer> list = getByType(i);
            for (int j = 0; j < list.size(); j++) {
                Timer t = list.get(j);
                if (t.number == timer.number) return new FindTimerHelper(t,j,list);
            }
        }
        return null;
    }

    public void change(final Timer timer, TubeAdapter activeAdapter) {
        FindTimerHelper timerHelper = findTimerByNumber(timer);
        Timer t = timerHelper.timer;
        ArrayList<Timer> list = timerHelper.timers;
        Log.d("my", "change tube " + t.number);
        if (timer.type == t.type)
            list.set(timerHelper.position, timer);
        else if (timer.type == Timer.TUBE_IN_REPAIR) activeAdapter.moveToRepair(t);
        else {
            list.remove(t);
            if (t.type == Timer.TUBE_IN_REPAIR) update(timer);
        }

        if (timer.activated) {
            activeAdapter.startTimer(timer);
            timer.setOnTickListener(new Timer.TickListener() {
                @Override
                public void onTick(int secondsUntilFinished) {

                }

                @Override
                public void onFinish() {
                    main.tubeFragment.trackTubeAdapter.finishTimer(timer);
                }
            });
        } else if (t.type == Timer.TUBE_ON_TRACK) activeAdapter.stopTimer(t);

        activeAdapter.notifyDataSetChanged();
        dao.update(timer);
        return;
    }
    
    public boolean insert(Timer timer) {
        try{
            dao.insert(timer);
            getByType(timer.type).add(0, timer);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    
    public void update(Timer timer) {
        dao.update(timer);
        getByType(timer.type).add(0,timer);
    }

    
    public void delete(Timer timer) {
        dao.deleteByNumber(timer.number);
    }

    public ArrayList<Timer> getByType(int type) {
        switch (type) {
            case Timer.TUBE_FREE:
                if (free==null)free = (ArrayList<Timer>) dao.getByType(Timer.TUBE_FREE);
                return free;
            case Timer.TUBE_ON_TRACK:
                if (track==null){
                    track = (ArrayList<Timer>) dao.getByType(Timer.TUBE_ON_TRACK);
                    track.sort(new Comparator<Timer>() {
                        @Override
                        public int compare(Timer o1, Timer o2) {
                            if (!o1.activated)return -1;
                            if (!o2.activated)return 1;
                            return 0;
                        }
                    });
                }
                return track;
            case Timer.TUBE_IN_REPAIR:
                if (repair==null)repair = (ArrayList<Timer>) dao.getByType(Timer.TUBE_IN_REPAIR);
                return repair;
        }
        return null;
    }

    public void setLists(ArrayList<Timer> track, ArrayList<Timer> free, ArrayList<Timer> repair){

        main.database.clearAllTables();
        for (Timer timer:track)dao.insert(timer);
        for (Timer timer:free)dao.insert(timer);
        for (Timer timer:repair)dao.insert(timer);

        for (Timer timer:this.track)timer.stop();
        this.track.clear();
        this.free.clear();
        this.repair.clear();
        this.track.addAll(track);
        this.free.addAll(free);
        this.repair.addAll(repair);
    }

    public class FindTimerHelper {
        public Timer timer;
        public ArrayList<Timer> timers;
        public int position;
        public FindTimerHelper(Timer timer, int pos, ArrayList<Timer> timers) {
            this.timer = timer;
            this.timers = timers;
            position = pos;
        }
    }
}
