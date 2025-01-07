package com.example.brainwave.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brainwave.R;

import java.lang.reflect.Method;
import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.MyViewHolder> {
    private Context context;
    private List<BluetoothDevice> devices;

    public BluetoothDeviceAdapter(Context context, List<BluetoothDevice> devices) {
        this.context = context;
        this.devices = devices;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_device, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        BluetoothDevice currentDevice = devices.get(position);
        holder.deviceName.setText(currentDevice.getName() != null ? currentDevice.getName() : "Unknown Device");
        holder.deviceAddress.setText(currentDevice.getAddress());

        // Initially hide the delete icon
        holder.deleteIcon.setVisibility(View.GONE);

        // Handle item click to toggle delete icon visibility
        holder.itemView.setOnClickListener(v -> {
            if (holder.deleteIcon.getVisibility() == View.VISIBLE) {
                holder.deleteIcon.setVisibility(View.GONE);
            } else {
                holder.deleteIcon.setVisibility(View.VISIBLE);
            }
        });

        // Handle delete icon click for unpairing or disconnecting the device
        holder.deleteIcon.setOnClickListener(v -> {
            // Unpair the device (requires Bluetooth permissions)
            try {
                Method removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
                removeBondMethod.invoke(currentDevice);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Remove from the device list and update the adapter
            devices.remove(position);
            notifyItemRemoved(position);
        });
    }


    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void addDevice(BluetoothDevice device) {
        devices.add(device);
        notifyItemInserted(devices.size() - 1);
    }

    public void removeDevice(BluetoothDevice device) {
        int position = devices.indexOf(device);
        if (position >= 0) {
            devices.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        ImageView deleteIcon;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceAddress = itemView.findViewById(R.id.device_address);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
        }
    }
}
