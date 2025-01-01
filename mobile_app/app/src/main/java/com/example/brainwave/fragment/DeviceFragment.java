package com.example.brainwave.fragment;

import android.Manifest;
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
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    String deviceName = device.getName();
                    if (deviceName != null && !deviceName.isEmpty() && !isDeviceInList(deviceName)) {
                        String deviceAddress = device.getAddress();
                        availableDevices.add(new Device(deviceName, deviceAddress, false));
                        availableAdapter.notifyDataSetChanged();
                    }
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

        rvConnected.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAvailable.setLayoutManager(new LinearLayoutManager(getContext()));

        connectedAdapter = new DeviceAdapter(connectedDevices);
        availableAdapter = new DeviceAdapter(availableDevices);

        rvConnected.setAdapter(connectedAdapter);
        rvAvailable.setAdapter(availableAdapter);

        availableAdapter.setOnItemClickListener(new DeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Device device, int position) {
                connectToDevice(position);
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getContext(), "Bluetooth không được hỗ trợ", Toast.LENGTH_SHORT).show();
            return;
        }

        checkPermissionsAndStart();
    }

    private void checkPermissionsAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                }, 1001);
                return;
            }
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(getContext(), "Bluetooth chưa được bật", Toast.LENGTH_SHORT).show();
        } else {
            fetchPairedDevices();
            discoverDevices();
        }
    }

    private void fetchPairedDevices() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                if (deviceName == null || deviceName.isEmpty()) {
                    deviceName = "Thiết bị không tên";
                }
                connectedDevices.add(new Device(deviceName, device.getAddress(), true));
            }
            connectedAdapter.notifyDataSetChanged();
        }
    }

    private void discoverDevices() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        if (getContext() != null) {
            getContext().registerReceiver(receiver, filter);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchPairedDevices();
                discoverDevices();
            } else {
                Toast.makeText(getContext(), "Không được cấp quyền Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
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
        String deviceName = device.getName();
        String deviceAddress = device.getAddress();

        if (deviceAddress == null || deviceAddress.isEmpty()) {
            Toast.makeText(getContext(), "Địa chỉ thiết bị không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 3);
                return;
            }
            bluetoothDevice.createBond(); // Request pairing

            // Wait for bonding to complete before adding to the connected list
            if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                connectedDevices.add(new Device(bluetoothDevice.getName(), bluetoothDevice.getAddress(), true));
                connectedAdapter.notifyDataSetChanged();
                availableDevices.remove(device);
                availableAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Pairing with " + deviceName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to pair with " + deviceName, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to pair device", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getContext() != null) {
            try {
                getContext().unregisterReceiver(receiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }
}
