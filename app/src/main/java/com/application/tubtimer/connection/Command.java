package com.application.tubtimer.connection;

import com.application.tubtimer.database.Timer;
import com.google.gson.Gson;

public class Command {
    public static final int ACTION_ADD = 0;
    public static final int ACTION_DELETE = 1;
    public static final int ACTION_CHANGE = 2;
    public static final int ACTION_STOP = 3;

    static Gson gson = new Gson();

    public byte[] getBytes(){
        return ("&"+gson.toJson(this)).getBytes();
    }

    public Command(int action, Timer timer) {
        this.action = action;
        this.timer = timer;
    }

    public static Command parseCommand(String command){
        String data = command.substring(1);
        return  gson.fromJson(data,Command.class);
    }

    public int action;
//    static int version;
    public Timer timer;
}
