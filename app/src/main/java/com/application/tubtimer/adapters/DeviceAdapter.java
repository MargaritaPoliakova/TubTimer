package com.application.tubtimer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adroitandroid.near.model.Host;
import com.application.tubtimer.R;
import com.application.tubtimer.activities.SearchActivity;
import com.application.tubtimer.connection.DiscoveryManager;

import java.util.ArrayList;
import java.util.List;


public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_ITEM = 2;
    public List<Host> hostList;
    SearchActivity activity;

    public DeviceAdapter(SearchActivity activity) {
        hostList = new ArrayList<>();
        this.activity = activity;
    }

    public void setData(List<Host> hosts){
        hostList = hosts;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        else return TYPE_ITEM;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false);
        return new DeviceViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull final DeviceViewHolder holder, int position) {
        if (TYPE_HEADER == holder.mViewType) {
            if (hostList.size()==0){
                holder.name.setText(R.string.empty);
            }else{
                String s = "Найденные устройства:";
                if (DiscoveryManager.host) s = "Подключённые устройства " + hostList.size() + ":";
                holder.name.setText(s);
            }
        } else {
            Host host = hostList.get(position - 1);
            holder.name.setText(host.getName());
            if (!DiscoveryManager.host)
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.discoveryManager.sendConnectRequest(hostList.get(holder.getAdapterPosition() - 1));
                        Toast.makeText(activity, "Отправлено", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    @Override
    public int getItemCount() {
        return hostList.size() + 1;
    }


    class DeviceViewHolder extends RecyclerView.ViewHolder {
        private final int mViewType;

        TextView name;

        public DeviceViewHolder(@NonNull View root, int mViewType) {
            super(root);
            name = root.findViewById(R.id.name_tv);
            this.mViewType = mViewType;
        }
    }

}
