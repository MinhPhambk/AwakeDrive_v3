package com.example.brainwave.fragment;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.brainwave.R;
import com.example.brainwave.adapter.DeviceAdapter;
import com.example.brainwave.model.Device;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
                    availableDevices.add(new Device(device.getName(), device.getAddress(), false));
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
        checkPermissionsAndStart();
    }

    private void checkPermissionsAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                }, 1001);
                return;
            }
        }
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
                connectedDevices.add(new Device(deviceName, device.getAddress(), true));
            }
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
                connectedDevices.add(new Device(device.getName(), device.getAddress(), true));
                connectedAdapter.notifyDataSetChanged();
                availableDevices.remove(device);
                availableAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Đã kết nối với " + device.getName(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Kết nối thất bại", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getContext().unregisterReceiver(receiver);
        } catch (IllegalArgumentException ignored) {
        }
    }
}
