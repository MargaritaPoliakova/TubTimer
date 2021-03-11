package com.application.tubtimer.adapters;

import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.application.tubtimer.connection.Command;
import com.application.tubtimer.connection.DiscoveryManager;
import com.application.tubtimer.database.Timer;
import com.application.tubtimer.fragments.TubeFragment;

public class FreeTubeAdapter extends TubeAdapter {

    public FreeTubeAdapter(TubeFragment tubeFragment, int type) {
        super(tubeFragment, type);
    }

    @Override
    public void onBindViewHolder(@NonNull final TubeViewHolder holder, int position) {
        final Timer timer = timers.get(holder.getAdapterPosition());
        init(holder,timer,true);

        holder.b_half_hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTimerAndStart(holder,timer,30);
            }
        });

        holder.b_hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTimerAndStart(holder,timer,60);
            }
        });

        holder.b_one_and_half_hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTimerAndStart(holder,timer,90);
            }
        });

        holder.b_two_hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTimerAndStart(holder,timer,120);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                moveToRepair(timer);
                tubeFragment.commandManager.send(Command.ACTION_CHANGE, timer);

                return true;
            }
        });


        timer.setOnTickListener(new Timer.TickListener() {
            @Override
            public void onTick(int secondsUntilFinished) {
            }

            @Override
            public void onFinish() {
                tubeFragment.trackTubeAdapter.finishTimer(timer);
            }
        });
    }


    void changeTimerAndStart(TubeViewHolder holder, final Timer timer, final int min){
        new AlertDialog.Builder(holder.itemView.getContext())
                .setMessage("Запустить таймер №"+timer.number+"?")
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        if (tubeFragment.commandManager.canChangeTimers()) {
                            timer.setTime(min * 60);
                            startTimer(timer);
//                        }else Toast.makeText(tubeFragment.main,"Запрос отправлен", Toast.LENGTH_SHORT).show();
                        tubeFragment.commandManager.send(Command.ACTION_CHANGE, timer);
                    }
                })
                .setNegativeButton("Нет", null).create().show();
    }

}
