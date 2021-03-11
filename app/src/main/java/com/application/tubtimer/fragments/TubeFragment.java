package com.application.tubtimer.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.application.tubtimer.adapters.FreeTubeAdapter;
import com.application.tubtimer.adapters.RepairTubeAdapter;
import com.application.tubtimer.adapters.TrackTubeAdapter;
import com.application.tubtimer.adapters.TubeAdapter;
import com.application.tubtimer.connection.Command;
import com.application.tubtimer.connection.CommandManager;
import com.application.tubtimer.connection.DiscoveryManager;
import com.application.tubtimer.database.DatabaseManager;
import com.application.tubtimer.database.Timer;
import com.application.tubtimer.activities.MainActivity;
import com.application.tubtimer.R;

import java.util.ArrayList;

public class TubeFragment extends Fragment {

    Button button_add, button_show, bt_search;
    public MainActivity main;
    public RecyclerView recycler;
    public DatabaseManager manager;
    public TextView empty;
    EditText number, et_search;
    LinearLayout linear_add;
    public CommandManager commandManager;

    public TrackTubeAdapter trackTubeAdapter;
    public FreeTubeAdapter freeTubeAdapter;
    public RepairTubeAdapter repairTubeAdapter;


    public void changeFragment(int id){
        switch (id){
            case R.id.navigation_track:
                onTrack();
                break;
            case R.id.navigation_free:
                onFree();
                break;
            case R.id.navigation_repair:
                onRepair();
                break;
        }
        ((TubeAdapter) recycler.getAdapter()).checkEmpty();

    }

    public void hideKeyboard(){
        // прячем клавиатуру
        InputMethodManager imm = (InputMethodManager) main.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(number.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void offerNumber(){
        int i=1;
        while (!manager.timerNotExist(i))i++;
        number.setText(i+"");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View root = inflater.inflate(R.layout.fragment_tube, container, false);
        button_add = root.findViewById(R.id.startBtn);
        button_show = root.findViewById(R.id.show_add);
        recycler = root.findViewById(R.id.tube_recycler);
        number = root.findViewById(R.id.tub_number);
        linear_add = root.findViewById(R.id.linear_add);
        empty = root.findViewById(R.id.empty_list);
        bt_search = root.findViewById(R.id.bt_search);
        et_search = root.findViewById(R.id.et_search);

        main = (MainActivity) getHost();



        manager = main.manager;
        //todo переворот
        RecyclerView.LayoutManager layoutManager;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            layoutManager = new LinearLayoutManager(main.getApplicationContext());
        else layoutManager = new GridLayoutManager(main.getApplicationContext(),2);

        recycler.setLayoutManager(layoutManager);

        freeTubeAdapter = new FreeTubeAdapter(this,Timer.TUBE_FREE);
        trackTubeAdapter = new TrackTubeAdapter(this,Timer.TUBE_ON_TRACK);
        repairTubeAdapter = new RepairTubeAdapter(this,Timer.TUBE_IN_REPAIR);


        bt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                TubeAdapter adapter = (TubeAdapter) recycler.getAdapter();
                int serch_number = Integer.parseInt(et_search.getText().toString());
                ArrayList<Timer> list = manager.getByType(adapter.type);
                for (Timer t : list) {
                    if (serch_number == t.number) {
                        list.remove(t);
                        list.add(0, t);
                        adapter.notifyDataSetChanged();
                        return;
                    }
                }
            }
                catch (Exception e) {Toast.makeText(main.getApplicationContext(), "Некорректный ввод", Toast.LENGTH_SHORT).show();
                }
            }
        });



        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    TubeAdapter adapter = (TubeAdapter) recycler.getAdapter();
                    Timer timer = new Timer(Integer.parseInt(number.getText().toString()),
                            30*60, adapter.type);
                    if (manager.timerNotExist(timer)) {
                        manager.insert(timer);
                        commandManager.send(Command.ACTION_ADD, timer);
                        recycler.smoothScrollToPosition(0);
                        adapter.notifyItemInserted(0);
                        adapter.checkEmpty();
                        offerNumber();

                        /*if (commandManager.canChangeTimers()){
                            manager.insert(timer);
                            recycler.smoothScrollToPosition(0);
                            adapter.notifyItemInserted(0);
                            adapter.checkEmpty();
                            offerNumber();
                        }
                        else Toast.makeText(main,"Запрос отправлен", Toast.LENGTH_SHORT).show();
                        commandManager.send(Command.ACTION_ADD, timer);*/
                    }else Toast.makeText(main, "Error", Toast.LENGTH_SHORT).show();

                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(main,"Error",Toast.LENGTH_SHORT).show();
                }

            }
        });

        changeFragment(R.id.navigation_track);

        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.START | ItemTouchHelper.END);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder holder, int direction) {
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setMessage("Удалить таймер?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TubeAdapter.TubeViewHolder holder1 = (TubeAdapter.TubeViewHolder) holder;
                                int position = manager.getByType(holder1.timer.type).indexOf(holder1.timer);
                                TubeAdapter adapter = (TubeAdapter) recycler.getAdapter();
                                if (commandManager.canChangeTimers())adapter.deleteTimer(position);
                                else Toast.makeText(main,"Запрос отправлен", Toast.LENGTH_SHORT).show();
                                commandManager.send(Command.ACTION_DELETE, holder1.timer);
                            }
                        })
                        .setNegativeButton("Нет", null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                recycler.getAdapter().notifyDataSetChanged();
                            }
                        }).create().show();

            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recycler);



        button_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (linear_add.getVisibility()==View.GONE){
                    linear_add.setVisibility(View.VISIBLE);
                    button_show.setText("Скрыть");
                    offerNumber();
                }else{
                    linear_add.setVisibility(View.GONE);
                    button_show.setText("Показать");
                    hideKeyboard();
                }
            }
        });

        return root;
    }






    void onTrack(){
        getActivity().setTitle(R.string.title_track);
        button_show.setVisibility(View.GONE);
        linear_add.setVisibility(View.GONE);

        recycler.setAdapter(trackTubeAdapter);
    }






    void onFree(){
        getActivity().setTitle(R.string.title_free);
        button_show.setVisibility(View.VISIBLE);
        button_show.setText("Показать");

        recycler.setAdapter(freeTubeAdapter);

    }






    void onRepair(){
        getActivity().setTitle(R.string.title_repair);
        button_show.setVisibility(View.VISIBLE);
        button_show.setText("Показать");

        recycler.setAdapter(repairTubeAdapter);
    }
}
