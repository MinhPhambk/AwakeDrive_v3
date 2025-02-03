package com.example.brainwave.adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.brainwave.R;
import com.example.brainwave.model.Device;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    private final List<Device> devices;
    private OnItemClickListener onItemClickListener;
    private OnUnpairClickListener onUnpairClickListener;
    private OnConnectClickListener onConnectClickListener;
    private OnDisconnectClickListener onDisconnectClickListener;

    public interface OnConnectClickListener {
        void onConnectClick(Device device, int position);
    }

    public interface OnDisconnectClickListener {
        void onDisconnectClick(Device device, int position);
    }

    public void setOnConnectClickListener(OnConnectClickListener listener) {
        this.onConnectClickListener = listener;
    }

    public void setOnDisconnectClickListener(OnDisconnectClickListener listener) {
        this.onDisconnectClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Device device, int position);
    }

    public interface OnUnpairClickListener {
        void onUnpairClick(Device device, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnUnpairClickListener(OnUnpairClickListener listener) {
        this.onUnpairClickListener = listener;
    }

    public DeviceAdapter(List<Device> devices) {
        this.devices = devices;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = devices.get(position);
        holder.deviceName.setText(device.getName());
        if(device.getStatus().equals("Đã lưu")){
            holder.deviceStatus.setText("Đã lưu");
            holder.deviceStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark));
            holder.unpairButton.setVisibility(View.VISIBLE);
        } else if (device.getStatus().equals("Đã kết nối")) {
            holder.deviceStatus.setText("Đã kết nối");
            holder.deviceStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark));
            holder.unpairButton.setVisibility(View.VISIBLE);
        } else{
            holder.deviceStatus.setText("Có sẵn");
            holder.deviceStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray));
            holder.unpairButton.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(device, position);
            }
        });
        holder.unpairButton.setOnClickListener(v -> {
            if (onUnpairClickListener != null) {
                onUnpairClickListener.onUnpairClick(device, position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName, deviceStatus;
        ImageView unpairButton;

        DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.tv_device_name);
            deviceStatus = itemView.findViewById(R.id.tv_device_status);
            unpairButton = itemView.findViewById(R.id.iv_unpair);
        }
    }
}