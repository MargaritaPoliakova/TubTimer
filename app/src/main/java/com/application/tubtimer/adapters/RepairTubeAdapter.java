package com.application.tubtimer.adapters;

import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.application.tubtimer.connection.Command;
import com.application.tubtimer.database.Timer;
import com.application.tubtimer.fragments.TubeFragment;

public class RepairTubeAdapter extends TubeAdapter {
    public RepairTubeAdapter(TubeFragment tubeFragment, int type) {
        super(tubeFragment, type);
    }

    @Override
    public void onBindViewHolder(@NonNull final TubeViewHolder holder, int position) {
        final Timer timer = timers.get(holder.getAdapterPosition());
        init(holder,timer,false);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setMessage("Переместить в свободные?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int position = tubeFragment.repairTubeAdapter.timers.indexOf(timer);
                                if (position>=0) {
                                    tubeFragment.repairTubeAdapter.timers.remove(position);
                                    tubeFragment.repairTubeAdapter.notifyItemRemoved(position);
                                }
                                timer.stop();
                                manager.update(timer);
                                tubeFragment.commandManager.send(Command.ACTION_CHANGE, timer);
                            }
                        })
                        .setNegativeButton("Нет", null).create().show();
            }
        });

    }
}
