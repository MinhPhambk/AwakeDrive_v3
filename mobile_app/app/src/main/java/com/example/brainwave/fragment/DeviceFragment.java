package com.example.brainwave.fragment;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.companion.BluetoothDeviceFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.brainwave.R;
import com.example.brainwave.adapter.DeviceAdapter;
import com.example.brainwave.model.Device;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DeviceFragment extends Fragment {

    private BluetoothAdapter bluetoothAdapter;
    private DeviceAdapter connectedAdapter, availableAdapter;
    private final List<Device> connectedDevices = new ArrayList<>();
    private final List<Device> availableDevices = new ArrayList<>();
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getName() != null && !isDeviceInList(device.getName())) {
                    availableDevices.add(new Device(device.getName(), device.getAddress(), "Có sẵn"));
                    availableAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvConnected = view.findViewById(R.id.rv_connected_devices);
        RecyclerView rvAvailable = view.findViewById(R.id.rv_available_devices);
        ImageView ivRefreshConnected = view.findViewById(R.id.iv_refresh_connected);
        ImageView ivRefreshAvailable = view.findViewById(R.id.iv_refresh_available);

        rvConnected.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAvailable.setLayoutManager(new LinearLayoutManager(getContext()));

        connectedAdapter = new DeviceAdapter(connectedDevices);
        availableAdapter = new DeviceAdapter(availableDevices);

        rvConnected.setAdapter(connectedAdapter);
        rvAvailable.setAdapter(availableAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth không được hỗ trợ", Toast.LENGTH_SHORT).show();
            return;
        }

        ivRefreshConnected.setOnClickListener(v -> refreshConnectedDevices(ivRefreshConnected));
        ivRefreshAvailable.setOnClickListener(v -> refreshAvailableDevices(ivRefreshAvailable));
        availableAdapter.setOnItemClickListener((device, position) -> connectToDevice(position));
        connectedAdapter.setOnUnpairClickListener((device, position) -> unpairDevice(position));
        startBluetoothProcesses();
    }


    private void startBluetoothProcesses() {
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(getContext(), "Bluetooth chưa được bật", Toast.LENGTH_SHORT).show();
        } else {
            fetchPairedDevices();
            discoverDevices();
        }
    }

    private void fetchPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName() != null ? device.getName() : "Thiết bị không tên";
                String deviceAddress = device.getAddress();

                // Kiểm tra nếu thiết bị đã kết nối
                if (isConnected(device)) {
                    // Thêm thiết bị đã kết nối vào danh sách
                    connectedDevices.add(new Device(deviceName, deviceAddress, "Đã kết nối"));
                } else {
                    // Thêm thiết bị đã lưu vào danh sách
                    connectedDevices.add(new Device(deviceName, deviceAddress, "Đã lưu"));
                }
            }

            // Cập nhật adapter (notifyDataSetChanged) sau khi thay đổi dữ liệu
            connectedAdapter.notifyDataSetChanged();
        }
    }


    private void discoverDevices() {
        bluetoothAdapter.cancelDiscovery();
        bluetoothAdapter.startDiscovery();
        getContext().registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    private boolean isDeviceInList(String deviceName) {
        for (Device device : availableDevices) {
            if (device.getName().equals(deviceName)) {
                return true;
            }
        }
        return false;
    }

    private void connectToDevice(int position) {
        Device device = availableDevices.get(position);
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());
        try {
            bluetoothDevice.createBond(); // Request pairing
            if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                connectedDevices.add(new Device(device.getName(), device.getAddress(), "Đã kết nối"));
                connectedAdapter.notifyDataSetChanged();
                availableDevices.remove(device);
                availableAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Đã kết nối với " + device.getName(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Kết nối thất bại", Toast.LENGTH_SHORT).show();
        }

    }

    // Hủy ghép nối
    private void unpairDevice(int position) {
        Device device = connectedDevices.get(position);
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());

        try {
            // Reflection để gọi phương thức removeBond
            Method removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
            boolean success = (boolean) removeBondMethod.invoke(bluetoothDevice);

            if (success) {
                Toast.makeText(getContext(), "Đã hủy ghép nối với " + device.getName(), Toast.LENGTH_SHORT).show();
                connectedDevices.remove(position);
                connectedAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Hủy ghép nối thất bại", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Lỗi khi hủy ghép nối", Toast.LENGTH_SHORT).show();
        }
    }


    private void refreshConnectedDevices(ImageView ivRefreshConnected) {
        // Hiệu ứng xoay
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(ivRefreshConnected, "rotation", 0f, 360f);
        rotateAnimator.setDuration(500); // Thời gian xoay 500ms
        rotateAnimator.start();

        // Làm mới danh sách thiết bị đã kết nối
        connectedDevices.clear();
        fetchPairedDevices(); // Tải lại danh sách thiết bị đã kết nối
    }

    private void refreshAvailableDevices(ImageView ivRefreshAvailable) {
        // Hiệu ứng xoay
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(ivRefreshAvailable, "rotation", 0f, 360f);
        rotateAnimator.setDuration(500); // Thời gian xoay 500ms
        rotateAnimator.start();

        // Làm mới danh sách thiết bị khả dụng
        bluetoothAdapter.cancelDiscovery();
        availableDevices.clear();
        availableAdapter.notifyDataSetChanged();
        discoverDevices(); // Bắt đầu tìm kiếm thiết bị mới
    }

    public static boolean isConnected(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("isConnected");
            return (Boolean) method.invoke(device);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getContext().unregisterReceiver(receiver);
        } catch (IllegalArgumentException ignored) {
        }
    }
}