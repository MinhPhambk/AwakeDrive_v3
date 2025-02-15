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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.brainwave.AlertService;
import com.example.brainwave.AttentionActivity;
import com.example.brainwave.DrawWaveView;
import com.example.brainwave.LocalDataSet;
import com.example.brainwave.R;
import com.example.brainwave.TrainModel;
import com.example.brainwave.adapter.DeviceAdapter;
import com.example.brainwave.model.Device;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.MindDataType;
import com.neurosky.connection.EEGPower;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;

import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.util.ArrayUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment {
    private static final String TAG = AttentionActivity.class.getSimpleName();
    private TgStreamReader tgStreamReader;

    public static Intent intent;

    private TextView tv_attention_value, tv_attention_notification, txt_name_user, txt_name_user_visible;
    private int badPacketCount = 0;
    private int numbeOfSamples = 0;
    private static final int MAX_SAMPLES = 5;

    private LinearLayout wave_layout;
    private static boolean isPoorSignal = false;
    private static boolean isProcessing = false;

    public EEGPower[] dataCollected = new EEGPower[MAX_SAMPLES];
    public EEGPower[] dataForInfer = new EEGPower[MAX_SAMPLES];

    private static final int NUMBER_OF_FEATURES = 80;
    private static final int[] sampleShape = {1, NUMBER_OF_FEATURES};

    private static int currentStatus;
    private static boolean isLoading = false;
    private static boolean isLoaded = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private static String server_url = "http://192.168.0.1000:8080";
    private ConstraintLayout contraint_connect;
    private ConstraintLayout contraint_connected;
    private CardView cardView3;
    private RecyclerView rvConnected;
    private RecyclerView rvAvailable;
    private ImageView ivRefreshConnected;
    private ImageView ivRefreshAvailable;

    //abc
    private BluetoothAdapter bluetoothAdapter;
    private DeviceAdapter connectedAdapter, availableAdapter;
    private final List<Device> connectedDevices = new ArrayList<>();
    private final List<Device> availableDevices = new ArrayList<>();
    //abc
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
        checkAndRequestPermissions();
        return inflater.inflate(R.layout.awake_view, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //abc
        initView(view);
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

        checkPermissionsAndStart();
        //abc
        checkBluetoothPermission();

        intent = new Intent(getContext(), AlertService.class);

        for (int i = 0; i < connectedDevices.size(); i++) {
            String device_name = connectedDevices.get(i).getName().trim();
            String device_name_correct = "AwD";
            String[] words = device_name_correct.split(" ");
            int index = 0;
            boolean found = true;
            for (String word : words) {
                index = device_name.indexOf(word, index);
                if (index == -1) {
                    found = false;
                    break;
                }
                index += word.length();
            }
//            if (found && connectedDevices.get(i).getStatus() == "Đã kết nối") {
//                Log.d("TAG_device_name", "true");
//                contraint_connect.setVisibility(View.GONE);
//                contraint_connected.setVisibility(View.VISIBLE);
//                cardView3.setVisibility(View.VISIBLE);
//            } else {
//                Log.d("TAG_device_name", "false");
//                contraint_connect.setVisibility(View.VISIBLE);
//                contraint_connected.setVisibility(View.GONE);
//                cardView3.setVisibility(View.GONE);
//            }
        }
        setUpDrawWaveView();


        tgStreamReader = new TgStreamReader(bluetoothAdapter, callback);
        tgStreamReader.setGetDataTimeOutTime(6);
        tgStreamReader.startLog();
    }

    private void checkBluetoothPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 99);
        }
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // Kiểm tra quyền BLUETOOTH_SCAN và BLUETOOTH_CONNECT trên Android 12+ (API >= 31)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }

        // Kiểm tra quyền ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // Nếu cần xin bất kỳ quyền nào, yêu cầu tất cả cùng lúc
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(),
                    permissionsNeeded.toArray(new String[0]),
                    1001);
        } else {
            // Nếu tất cả các quyền đã được cấp, khởi tạo Bluetooth
            initializeBluetooth();
        }
    }

    private void initializeBluetooth() {
        Log.d(TAG, "Bluetooth được khởi tạo!");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                initializeBluetooth();
            } else {
                // Permission denied
                Toast.makeText(requireContext(), "Quyền vị trí bị từ chối. Không thể sử dụng Bluetooth!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initView(View view) {
        rvConnected = view.findViewById(R.id.rv_connected_devices);
        rvAvailable = view.findViewById(R.id.rv_available_devices);
        ivRefreshConnected = view.findViewById(R.id.iv_refresh_connected);
        ivRefreshAvailable = view.findViewById(R.id.iv_refresh_available);
        tv_attention_value = getView().findViewById(R.id.tv_attention_value);
        tv_attention_notification = getView().findViewById(R.id.tv_attention_notification);
        Button button_update = getView().findViewById(R.id.btn_update);
        Button btn_start = getView().findViewById(R.id.btn_attention_start);
        Button btn_stop = getView().findViewById(R.id.btn_attention_stop);
        wave_layout = getView().findViewById(R.id.wave_layout);
//        contraint_connect = getView().findViewById(R.id.contraint_connect);
        contraint_connected = getView().findViewById(R.id.contraint_connected);
        cardView3 = getView().findViewById(R.id.cardView3);
//        txt_name_user = view.findViewById(R.id.txt_name_user);
        txt_name_user_visible = view.findViewById(R.id.txt_name_user_visible);
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser user=firebaseAuth.getCurrentUser();
//        if(user.getDisplayName()!=null){
//            txt_name_user.setText("Xin chào, "+user.getDisplayName().toString()+"!");
//            txt_name_user_visible.setText("Xin chào, "+user.getDisplayName().toString()+"!");
//        }
//        ImageView img_avatar_user=view.findViewById(R.id.img_avatar_user);
        ImageView img_avatar_user_visible=view.findViewById(R.id.img_avatar_user_visible);
        if(user.getPhotoUrl()!=null){
//            Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(img_avatar_user);
            Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(img_avatar_user_visible);
        }else {
//            Glide.with(this).load(R.drawable.avatar).circleCrop().into(img_avatar_user);
            Glide.with(this).load(R.drawable.avatar).circleCrop().into(img_avatar_user_visible);
        }
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (isProcessing) {
                    return;
                }
                showToast("Connecting...", Toast.LENGTH_SHORT);
                numbeOfSamples = 0;
                isProcessing = true;
                tv_attention_notification.setText("Monitoring ...");

                badPacketCount = 0;

                // load model
                try {
                    if (TrainModel.model == null) {
//                        File pathFile = new File(getExternalFilesDir(TrainModel.modelDir), TrainModel.fileModelName);
                        File pathFile = Paths.get("app/src/main/java/trained_nn.zip").toAbsolutePath().toFile();
                        System.out.println("Model file path:");
                        System.out.println(pathFile);
                        TrainModel.model = ModelSerializer.restoreMultiLayerNetwork(pathFile, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if(tgStreamReader != null && tgStreamReader.isBTConnected()){

                    // Prepare for connecting
                    tgStreamReader.stop();
                    tgStreamReader.close();
                }

                tgStreamReader.connect();
//				tgStreamReader.connectAndStart();

            }

        });
        btn_stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                stop();
            }

        });

        button_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isLoading) {
                    return;
                }
                isLoading = true;
                AsyncTaskRunner runner = new AsyncTaskLoadModel();
                runner.execute();
                ProgressBar bar = (ProgressBar) getView().findViewById(R.id.progressBar);
                bar.setVisibility(View.VISIBLE);
            }
        });
    }

    private class AsyncTaskRunner extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressBar bar = (ProgressBar) getView().findViewById(R.id.progressBar);
            bar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            //Hide the progress bar now that we are finished
            ProgressBar bar = (ProgressBar) getView().findViewById(R.id.progressBar);
            bar.setVisibility(View.INVISIBLE);

        }

    }

    private class AsyncTaskLoadModel extends AsyncTaskRunner {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String content = "Loading model...";
            tv_attention_notification.setText(content);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String apiUrl = server_url + "/api/getmodel";
                Context context = requireContext();
                File pathFile = new File(context.getExternalFilesDir(TrainModel.modelDir), TrainModel.fileModelName);

                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                    FileOutputStream outputStream = new FileOutputStream(pathFile);

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.close();
                    inputStream.close();

                    String content = "Zip file downloaded successfully.";
                    System.out.println(content);
                    isLoaded = true;
                } else {
                    System.out.println("Failed to download zip file. Response code: " + responseCode);
                }

                connection.disconnect();
                TrainModel.model = ModelSerializer.restoreMultiLayerNetwork(pathFile, false);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (isLoaded == true) {
                String content = "Zip file downloaded successfully.";
                tv_attention_notification.setText(content);
            } else {
                String content = "Failed to download zip file.";
                tv_attention_notification.setText(content);
            }
            isLoading = false;
            isLoaded = false;
        }

    }

    public void stop() {
        if(tgStreamReader != null){
            tgStreamReader.stop();
            tgStreamReader.close();
        }
        tv_attention_value.setText("--");
        numbeOfSamples = 0;
        isProcessing = false;
        tv_attention_notification.setText("Press to Start");
        stopAlertService();
    }

    private void stopAlertService() {
        // Stop any active alert service if applicable
        Bundle b = new Bundle();
        b.putBoolean("Status", false);
        intent.putExtra("Alert", b);
        getActivity().startService(intent);
    }


    // abc
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
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(ivRefreshConnected, "rotation", 0f, 360f);
        rotateAnimator.setDuration(500); // Thời gian xoay 500ms
        rotateAnimator.start();

        connectedDevices.clear();
        fetchPairedDevices();
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

    //abc
    @Override
    public void onResume() {
        super.onResume();
        tv_attention_notification.setText("Press to Start");
    }

    @Override
    public void onPause() {
        super.onPause();
        stop();
    }

    @Override
    public void onDestroy() {
        stop();
        super.onDestroy();
        try {
            getContext().unregisterReceiver(receiver);
        } catch (IllegalArgumentException ignored) {
        }
    }

    DrawWaveView waveView = null;

    private void setUpDrawWaveView() {
        DrawWaveView waveView = new DrawWaveView(getContext());
        wave_layout.addView(waveView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        waveView.setValue(2048, 2048, -2048);
    }

    private void updateWaveView(int data) {
        // Update the wave view with new data
        if (waveView != null) {
            waveView.updateData(data);
        }
    }

    private TgStreamHandler callback = new TgStreamHandler() {

        @Override
        public void onStatesChanged(int connectionStates) {
            // TODO Auto-generated method stub
            Log.d(TAG, "connectionStates change to: " + connectionStates);
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTING:
                    break;
                case ConnectionStates.STATE_CONNECTED:
                    tgStreamReader.start();
                    showToast("Connected", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_WORKING:
                    tgStreamReader.startRecordRawData();
                    Log.d("Tagggggg", connectionStates+"");
                    break;
                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    tgStreamReader.stopRecordRawData();

                    showToast("Get data time out!", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_STOPPED:
                    break;
                case ConnectionStates.STATE_DISCONNECTED:
                    break;
                case ConnectionStates.STATE_ERROR:
                    break;
                case ConnectionStates.STATE_FAILED:
                    setFailState();
                    showToast("Connection failed!\nPlease check your bluetooth device", Toast.LENGTH_SHORT);
                    break;
            }
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_STATE;
            msg.arg1 = connectionStates;
            LinkDetectedHandler.sendMessage(msg);
        }

        @Override
        public void onRecordFail(int flag) {
            // handle the record error message
            Log.e(TAG,"onRecordFail: " +flag);

        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
            // handle the bad packets.
            badPacketCount ++;
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_BAD_PACKET;
            msg.arg1 = badPacketCount;
            LinkDetectedHandler.sendMessage(msg);

        }

        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            // handle the received data
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = datatype;
            msg.arg1 = data;
            msg.obj = obj;
            LinkDetectedHandler.sendMessage(msg);

            //Log.i(TAG,"onDataReceived");
        }

    };

    private static final int MSG_UPDATE_BAD_PACKET = 1001;
    private static final int MSG_UPDATE_STATE = 1002;

    private Handler LinkDetectedHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MindDataType.CODE_RAW:
                    updateWaveView(msg.arg1);
                    break;
                case MindDataType.CODE_MEDITATION:
                    Log.d(TAG, "HeadDataType.CODE_MEDITATION " + msg.arg1);
                    break;
                case MindDataType.CODE_ATTENTION:
                    Log.d(TAG, "CODE_ATTENTION " + msg.arg1);
                    break;
                case MindDataType.CODE_EEGPOWER:
                    if (isPoorSignal == true) {
                        isPoorSignal = false;
                        break;
                    }
                    EEGPower power = (EEGPower)msg.obj;
                    if(power.isValidate()){
                        if(numbeOfSamples >= MAX_SAMPLES) {
                            numbeOfSamples = 0;
                            dataForInfer = dataCollected.clone();
                            HomeFragment.AsyncTaskInfer runner = new AsyncTaskInfer();
                            runner.execute();
                        }
                        dataCollected[numbeOfSamples] = power;
                        numbeOfSamples++;
                    }
                    break;
                case MindDataType.CODE_POOR_SIGNAL:
                    int poorSignal = msg.arg1;
                    Log.d(TAG, "poorSignal:" + poorSignal);
                    if (poorSignal > 0) {
                        isPoorSignal = true;
                    }

                    break;
                case MSG_UPDATE_BAD_PACKET:

                    break;
                default:
                    break;
            }

            super.handleMessage(msg);
        }
    };

    private class AsyncTaskInfer extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // run training process here
            EEGPower[] EEGdata = dataForInfer.clone();
            double [] sample = new double[NUMBER_OF_FEATURES];
            for (int i = 0; i<MAX_SAMPLES; i++) {
                sample[i*16] = EEGdata[i].delta;
                sample[i*16+1] = EEGdata[i].theta;
                sample[i*16+2] = EEGdata[i].lowAlpha;
                sample[i*16+3] = EEGdata[i].highAlpha;
                sample[i*16+4] = EEGdata[i].lowBeta;
                sample[i*16+5] = EEGdata[i].highBeta;

                sample[i*16+6] = (double) EEGdata[i].delta/EEGdata[i].theta;
                sample[i*16+7] = (double)EEGdata[i].delta/EEGdata[i].lowAlpha;
                sample[i*16+8] = (double)EEGdata[i].delta/EEGdata[i].highAlpha;
                sample[i*16+9] = (double)EEGdata[i].delta/EEGdata[i].lowBeta;
                sample[i*16+10] = (double)EEGdata[i].delta/EEGdata[i].highBeta;

                sample[i*16+11] = (double)EEGdata[i].theta/EEGdata[i].lowAlpha;
                sample[i*16+12] = (double)EEGdata[i].theta/EEGdata[i].highAlpha;
                sample[i*16+13] = (double)EEGdata[i].theta/EEGdata[i].lowBeta;
                sample[i*16+14] = (double)EEGdata[i].theta/EEGdata[i].highBeta;

                sample[i*16+15] = (double)(EEGdata[i].delta + EEGdata[i].theta) / (EEGdata[i].lowAlpha + EEGdata[i].highAlpha + EEGdata[i].lowBeta +EEGdata[i].highBeta);
            }

            INDArray sample_to_infer = Nd4j.create(ArrayUtil.flattenDoubleArray(sample), sampleShape);
            INDArray predicted = TrainModel.model.output(sample_to_infer, false);
            INDArray index = predicted.argMax();
            int[] pl = index.toIntVector();
            currentStatus = pl[0];
            if(pl[0] == 0) {
                alertService();
            }

            return null;
        }

        //This block executes in UI when background thread finishes
        //This is where we update the UI with our classification results
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            String predicted_label = "You are " + LocalDataSet.statues[currentStatus].toLowerCase() + ".";
            tv_attention_value.setText(predicted_label);
        }
    }

    public void alertService() {
        Bundle b = new Bundle();
        b.putBoolean("Status", true);
        intent.putExtra("Alert", b);
        getActivity().startService(intent);
    }

    private void startAlertService() {
        Intent serviceIntent = new Intent(getContext(), AlertService.class);
        getContext().startService(serviceIntent);
    }

    private void showToast(final String message, final int duration) {
        if (getActivity() != null) {
            new Handler(getActivity().getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), message, duration).show();
                }
            });
        }
    }


    private double[] extractFeatures(EEGPower[] data) {
        // Method to extract features for classification
        return new double[NUMBER_OF_FEATURES]; // Placeholder
    }
    private void setFailState() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = getView().findViewById(R.id.tv_attention_notification);
                textView.setText("Mất kết nối!");
                if (tgStreamReader != null) {
                    tgStreamReader.stop();
                    tgStreamReader.close();
                }
            }
        });
    }
}
