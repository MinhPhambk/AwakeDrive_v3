package com.example.brainwave.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public interface OnItemClickListener {
        void onItemClick(Device device, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
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
        holder.deviceStatus.setText(device.isConnected() ? "Đang bật" : "Có sẵn");
        holder.deviceStatus.setTextColor(device.isConnected() ?
                ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark) :
                ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray));

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(device,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName, deviceStatus;

        DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.tv_device_name);
            deviceStatus = itemView.findViewById(R.id.tv_device_status);
        }
    }
}
