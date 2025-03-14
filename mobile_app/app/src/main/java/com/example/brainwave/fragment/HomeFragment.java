package com.example.brainwave.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.brainwave.AlertService;
import com.example.brainwave.AttentionActivity;
import com.example.brainwave.DrawWaveView;
import com.example.brainwave.Interface.SoundManager;
import com.example.brainwave.LocalDataSet;
import com.example.brainwave.R;
import com.example.brainwave.TrainModel;
import com.example.brainwave.activity.DetailActivity;
import com.example.brainwave.activity.LoginActivity;
import com.example.brainwave.activity.SplashActivity;
import com.example.brainwave.adapter.DeviceAdapter;
import com.example.brainwave.model.Device;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class HomeFragment extends Fragment {
    private static final String TAG = AttentionActivity.class.getSimpleName();
    private TgStreamReader tgStreamReader;

    public static Intent intent;

    private TextView tv_attention_value, txt_name_user, txt_name_user_visible, tv_time;
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
    Button button_update;
    Button btn_start;
    Button btn_stop, bt_detail;

    private BluetoothAdapter bluetoothAdapter;
    private DeviceAdapter connectedAdapter, availableAdapter;
    private final List<Device> connectedDevices = new ArrayList<>();
    private final List<Device> availableDevices = new ArrayList<>();
    private static final int PERMISSION_REQUEST_CODE = 1001;


    private DatabaseReference databaseRef;
    private FirebaseAuth firebaseAuth;
    private Handler firebaseHandler;
    private Runnable firebaseRunnable;
    private int lastDelta = -1;
    private int lastTheta = -1;
    private int lastLowalpha = -1;
    private int lastHighAlpha = -1;
    private int lastLowBeta = -1;
    private int lastHighBeta = -1;
    private int lastLowGamma = -1;
    private int lastMiddleGamma = -1;
    private int poorSignal = -1;
    private String uid = null;
    private boolean isRecording = true;
    private long startTime = 0;
    private long endTime = 0;
    private String sessionId;
    private int seconds = 0;
    private SoundManager soundManager;
    private boolean running = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
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
        return inflater.inflate(R.layout.awake_view, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        printHashKey(getContext());
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            button_update.setBackgroundColor(Color.BLACK);
            button_update.setTextColor(Color.WHITE);
            btn_start.setBackgroundColor(Color.BLACK);
            btn_start.setTextColor(Color.WHITE);
            btn_stop.setBackgroundColor(Color.BLACK);
            btn_stop.setTextColor(Color.WHITE);
        } else {
            button_update.setBackgroundColor(Color.WHITE);
            button_update.setTextColor(Color.BLACK);
            btn_start.setBackgroundColor(Color.WHITE);
            btn_start.setTextColor(Color.BLACK);
            btn_stop.setBackgroundColor(Color.WHITE);
            btn_stop.setTextColor(Color.BLACK);
        }
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
//        connectedAdapter.setOnItemClickListener((device, position) -> {
//            if (isProcessing) {
//                return;
//            }
//            showToast("Connecting...", Toast.LENGTH_SHORT);
//            numbeOfSamples = 0;
//            isProcessing = true;
//
//            badPacketCount = 0;
//
//            // load model
//            try {
//                if (TrainModel.model == null) {
////                        File pathFile = new File(getExternalFilesDir(TrainModel.modelDir), TrainModel.fileModelName);
//                    File pathFile = Paths.get("app/src/main/java/trained_nn.zip").toAbsolutePath().toFile();
//                    System.out.println("Model file path:");
//                    System.out.println(pathFile);
//                    TrainModel.model = ModelSerializer.restoreMultiLayerNetwork(pathFile, false);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//
//            if (tgStreamReader != null && tgStreamReader.isBTConnected()) {
//
//                // Prepare for connecting
//                tgStreamReader.stop();
//                tgStreamReader.close();
//            }
//
//            tgStreamReader.connect();
////				tgStreamReader.connectAndStart();
//        });
        connectedAdapter.setOnUnpairClickListener((device, position) -> unpairDevice(position));

        checkPermissionsAndStart();

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
//            if (found && connectedDevices.get(i).getStatus() == "Đã lưu") {
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

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            uid = user.getUid();
            databaseRef = FirebaseDatabase.getInstance().getReference("BrainData").child(uid);
        } else {
            Log.e("Firebase", "Người dùng chưa đăng nhập!");
        }

    }

    private Runnable updateTime = new Runnable() {
        @Override
        public void run() {
            if (running) {
                seconds++;
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                tv_time.setText(String.format("%02d:%02d:%02d", hours, minutes, secs));
                handler.postDelayed(this, 1000);
            }
        }
    };

    private void saveDataToFirebase(String uid, String sessionId) {
        long timestamp = System.currentTimeMillis();

        Map<String, Object> brainData = new HashMap<>();
        brainData.put("timestamp", timestamp);
        brainData.put("delta", lastDelta);
        brainData.put("theta", lastTheta);
        brainData.put("lowAlpha", lastLowalpha);
        brainData.put("highAlpha", lastHighAlpha);
        brainData.put("lowBeta", lastLowBeta);
        brainData.put("highBeta", lastHighBeta);
        brainData.put("lowGamma", lastLowGamma);
        brainData.put("middleGamma", lastMiddleGamma);

        databaseRef.child("sessions").child(sessionId).child("data").child(String.valueOf(timestamp))
                .setValue(brainData)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Dữ liệu đã lưu"))
                .addOnFailureListener(e -> Log.e("Firebase", "Lỗi lưu dữ liệu", e));
    }

    public void stopRecording() {
        if (!isRecording) return;

        isRecording = false;
        endTime = System.currentTimeMillis();

        if (firebaseHandler != null) {
            firebaseHandler.removeCallbacks(firebaseRunnable);

            // Kiểm tra xem data có tồn tại không trước khi lưu session
            databaseRef.child("sessions").child(sessionId).child("data")
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            saveSessionTime(sessionId, startTime, endTime);
                        } else {
                            Log.d("Firebase", "Không có data");
                        }
                    });
        }
    }

    private void saveSessionTime(String sessionId, long start, long end) {
        if (start == 0 || end == 0) return;

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("startTime", start);
        sessionData.put("endTime", end);

        databaseRef.child("sessions").child(sessionId).child("info")
                .setValue(sessionData)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Lưu thông tin phiên đo"))
                .addOnFailureListener(e -> Log.e("Firebase", "Lỗi lưu phiên đo", e));
    }

    public static void printHashKey(Context pContext) {
        try {
            PackageInfo info = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i("TAGggggg", "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e("TAGggggg", "printHashKey()", e);
        } catch (Exception e) {
            Log.e("TAGggggg", "printHashKey()", e);
        }
    }


    private void initView(View view) {
        soundManager = SoundManager.getInstance(getContext());
        rvConnected = view.findViewById(R.id.rv_connected_devices);
        rvAvailable = view.findViewById(R.id.rv_available_devices);
        ivRefreshConnected = view.findViewById(R.id.iv_refresh_connected);
        ivRefreshAvailable = view.findViewById(R.id.iv_refresh_available);
        tv_attention_value = getView().findViewById(R.id.tv_attention_value);
        button_update = getView().findViewById(R.id.btn_update);
        btn_start = getView().findViewById(R.id.btn_attention_start);
        btn_stop = getView().findViewById(R.id.btn_attention_stop);
        bt_detail = getView().findViewById(R.id.bt_detail);
        wave_layout = getView().findViewById(R.id.wave_layout);
        contraint_connect = getView().findViewById(R.id.contraint_connect);
        contraint_connected = getView().findViewById(R.id.contraint_connected);
        cardView3 = getView().findViewById(R.id.cardView3);
        txt_name_user = view.findViewById(R.id.txt_name_user);
        txt_name_user_visible = view.findViewById(R.id.txt_name_user_visible);
        tv_time = view.findViewById(R.id.tv_time);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user.getDisplayName() != null) {
            txt_name_user.setText("Xin chào, " + user.getDisplayName().toString() + "!");
            txt_name_user_visible.setText("Xin chào, " + user.getDisplayName().toString() + "!");
        }
        ImageView img_avatar_user = view.findViewById(R.id.img_avatar_user);
        ImageView img_avatar_user_visible = view.findViewById(R.id.img_avatar_user_visible);
        if (user.getPhotoUrl() != null) {
            Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(img_avatar_user);
            Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(img_avatar_user_visible);
        } else {
            Glide.with(this).load(R.drawable.avatar).circleCrop().into(img_avatar_user);
            Glide.with(this).load(R.drawable.avatar).circleCrop().into(img_avatar_user_visible);
        }
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                soundManager.playSound();
                if (running == true) {
                    Toast.makeText(getContext(), "Vui lòng stop trước khi start lại", Toast.LENGTH_SHORT).show();
                } else {
                    running = true;
                    seconds = 0;
                    handler.post(updateTime);
                    if (isProcessing) {
                        return;
                    }
                    showToast("Connecting...", Toast.LENGTH_SHORT);
                    numbeOfSamples = 0;
                    isProcessing = true;

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


                    if (tgStreamReader != null && tgStreamReader.isBTConnected()) {

                        // Prepare for connecting
                        tgStreamReader.stop();
                        tgStreamReader.close();
                    }

                    tgStreamReader.connect();
//				tgStreamReader.connectAndStart();
//                startRecording();
                    isRecording = true;
                    startTime = System.currentTimeMillis();
                    sessionId = String.valueOf(startTime);
                    endTime = 0;
                    firebaseHandler = new Handler();
                    firebaseRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (!isRecording) return;

                            if (uid != null && lastDelta != -1 && lastTheta != -1 && lastLowalpha != -1 && lastHighAlpha != -1 &&
                                    lastHighBeta != -1 && lastLowBeta != -1 && lastLowGamma != -1 && lastMiddleGamma != -1) {
                                saveDataToFirebase(uid, sessionId);
                            }
                            firebaseHandler.postDelayed(this, 1000);
                        }
                    };
                    firebaseHandler.post(firebaseRunnable);
                }
            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                soundManager.playSound();
                running = false;
                handler.removeCallbacks(updateTime);
                stop();
                stopRecording();
            }
        });

        button_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundManager.playSound();

                if (isLoading) {
                    return;
                }
                isLoading = true;
                AsyncTaskRunner runner = new AsyncTaskLoadModel();
                runner.execute();
            }
        });

        bt_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundManager.playSound();
                Intent detail = new Intent(getContext(), DetailActivity.class);
                startActivity(detail);
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
            Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show();
            } else {
                String content = "Failed to download zip file.";
                Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show();
            }
            isLoading = false;
            isLoaded = false;
        }

    }

    public void stop() {
        if (tgStreamReader != null) {
            tgStreamReader.stop();
            tgStreamReader.close();
        }
        tv_attention_value.setText("--");
        numbeOfSamples = 0;
        isProcessing = false;
        stopAlertService();
    }

    private void stopAlertService() {
        // Stop any active alert service if applicable
        Bundle b = new Bundle();
        b.putBoolean("Status", false);
        intent.putExtra("Alert", b);
        getActivity().startService(intent);
    }


    private void checkPermissionsAndStart() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(),
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        } else {
            startBluetoothProcesses();
        }
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
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Không có quyền BLUETOOTH_CONNECT!", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null) {
            connectedDevices.clear(); // Xóa danh sách cũ trước khi cập nhật
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = (device.getName() != null) ? device.getName() : "Thiết bị không tên";
                String deviceAddress = device.getAddress();
                String status = isConnected(device) ? "Đã kết nối" : "Đã lưu";

                connectedDevices.add(new Device(deviceName, deviceAddress, status));
            }
            connectedAdapter.notifyDataSetChanged(); // Cập nhật UI
        }
    }


    private void discoverDevices() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            bluetoothDevice.createBond(); // Request pairing
            if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {

                connectedDevices.add(new Device(device.getName(), device.getAddress(), "Đã lưu"));
                connectedAdapter.notifyDataSetChanged();
                availableDevices.remove(device);
                availableAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Đã ghép nối với " + device.getName(), Toast.LENGTH_SHORT).show();
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
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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
    public void onResume() {
        super.onResume();
        FirebaseUser user=firebaseAuth.getCurrentUser();
        checkAndDeleteInvalidSessions(user.getUid());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
        try {
            getContext().unregisterReceiver(receiver);
        } catch (IllegalArgumentException ignored) {
        }
    }


    DrawWaveView waveView = null;

    private void setUpDrawWaveView() {
        waveView = new DrawWaveView(requireContext());

        wave_layout.addView(waveView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        waveView.setValue(2048, 2048, -2048);
    }

    private void updateWaveView(int data) {
        if (waveView != null) {
            waveView.updateData(data);
        }
//        Log.d("TAGgg_data", data + "");
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
                    Log.d("Tagggggg", connectionStates + "");
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
            Message msg1 = LinkDetectedHandler1.obtainMessage();
            msg.what = MSG_UPDATE_STATE;
            msg.arg1 = connectionStates;
            msg1.what = MSG_UPDATE_STATE;
            msg1.arg1 = connectionStates;
            LinkDetectedHandler.sendMessage(msg);
            LinkDetectedHandler1.sendMessage(msg1);
        }

        @Override
        public void onRecordFail(int flag) {
            // handle the record error message
            Log.e(TAG, "onRecordFail: " + flag);

        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
            // handle the bad packets.
            badPacketCount++;
            Message msg = LinkDetectedHandler.obtainMessage();
            Message msg1 = LinkDetectedHandler1.obtainMessage();
            msg.what = MSG_UPDATE_BAD_PACKET;
            msg.arg1 = badPacketCount;
            msg1.what = MSG_UPDATE_BAD_PACKET;
            msg1.arg1 = badPacketCount;
            LinkDetectedHandler.sendMessage(msg);
            LinkDetectedHandler1.sendMessage(msg1);
        }

        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            // handle the received data
            Message msg = LinkDetectedHandler.obtainMessage();
            Message msg1 = LinkDetectedHandler1.obtainMessage();
            msg.what = datatype;
            msg.arg1 = data;
            msg.obj = obj;
            msg1.what = datatype;
            msg1.arg1 = data;
            msg1.obj = obj;
            LinkDetectedHandler.sendMessage(msg);
            LinkDetectedHandler1.sendMessage(msg1);

            //Log.i(TAG,"onDataReceived");
        }

    };

    private static final int MSG_UPDATE_BAD_PACKET = 1001;
    private static final int MSG_UPDATE_STATE = 1002;

    private Handler LinkDetectedHandler = new Handler(Looper.getMainLooper()) {

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
                    EEGPower power = (EEGPower) msg.obj;
                    if (power.isValidate()) {
                        if (numbeOfSamples >= MAX_SAMPLES) {
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

    private Handler LinkDetectedHandler1 = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MindDataType.CODE_RAW:
                    break;
                case MindDataType.CODE_MEDITATION:
                    Log.d("TAGggg_data", "HeadDataType.CODE_MEDITATION " + msg.arg1);
                    break;
                case MindDataType.CODE_ATTENTION:
                    Log.d("TAGggg_data", "CODE_ATTENTION " + msg.arg1);
                    break;
                case MindDataType.CODE_EEGPOWER:
                    EEGPower power = (EEGPower) msg.obj;
                    if (power.isValidate()) {
                        lastDelta = power.delta;
                        lastTheta = power.theta;
                        lastLowalpha = power.lowAlpha;
                        lastHighAlpha = power.highAlpha;
                        lastLowBeta = power.lowBeta;
                        lastHighBeta = power.highBeta;
                        lastLowGamma = power.lowGamma;
                        lastMiddleGamma = power.middleGamma;

                        sendEEGDataToActivity();
//                        int alertness = calculateAlertness(lastDelta,lastTheta,lastLowalpha, lastHighAlpha, lastLowBeta, lastHighBeta,lastLowGamma ,lastMiddleGamma);
////                        TextView level_alert = getView().findViewById(R.id.level_alert);
////                        Log.d("TAG_alertness", alertness+"");
////                        level_alert.setText(alertness+"/100");
                    }
                    break;
                case MindDataType.CODE_POOR_SIGNAL:
                    poorSignal = msg.arg1;
                    Log.d(TAG, "poorSignal:" + poorSignal);
                    break;
                case MSG_UPDATE_BAD_PACKET:
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    // Hàm tính mức độ tỉnh táo
    public static int calculateAlertness(int delta, int theta, int lowAlpha, int highAlpha,
                                            int lowBeta, int highBeta, int lowGamma, int middleGamma) {
        // Tính tổng công suất của các dải
        int alphaPower = lowAlpha + highAlpha;
        int betaPower = lowBeta + highBeta;
        int gammaPower = lowGamma + middleGamma;

        // Công thức mức độ tỉnh táo
        int alertness = 100 * (betaPower + gammaPower) / (delta + theta + alphaPower);

        // Giới hạn kết quả trong khoảng [0, 100]
        return Math.max(0, Math.min(100, alertness));
    }
    private void sendEEGDataToActivity() {
        Intent intent = new Intent("com.example.brainwave.UPDATE_DATA");
        intent.putExtra("delta", lastDelta);
        intent.putExtra("theta", lastTheta);
        intent.putExtra("lowAlpha", lastLowalpha);
        intent.putExtra("highAlpha", lastHighAlpha);
        intent.putExtra("lowBeta", lastLowBeta);
        intent.putExtra("highBeta", lastHighBeta);
        intent.putExtra("lowGamma", lastLowGamma);
        intent.putExtra("middleGamma", lastMiddleGamma);

        if (getContext() != null) {
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
            Log.d("EEGFragment", "Broadcast sent with data: " + lastDelta);
        }
    }


    private class AsyncTaskInfer extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // run training process here
            EEGPower[] EEGdata = dataForInfer.clone();
            double[] sample = new double[NUMBER_OF_FEATURES];
            for (int i = 0; i < MAX_SAMPLES; i++) {
                sample[i * 16] = EEGdata[i].delta;
                sample[i * 16 + 1] = EEGdata[i].theta;
                sample[i * 16 + 2] = EEGdata[i].lowAlpha;
                sample[i * 16 + 3] = EEGdata[i].highAlpha;
                sample[i * 16 + 4] = EEGdata[i].lowBeta;
                sample[i * 16 + 5] = EEGdata[i].highBeta;

                sample[i * 16 + 6] = (double) EEGdata[i].delta / EEGdata[i].theta;
                sample[i * 16 + 7] = (double) EEGdata[i].delta / EEGdata[i].lowAlpha;
                sample[i * 16 + 8] = (double) EEGdata[i].delta / EEGdata[i].highAlpha;
                sample[i * 16 + 9] = (double) EEGdata[i].delta / EEGdata[i].lowBeta;
                sample[i * 16 + 10] = (double) EEGdata[i].delta / EEGdata[i].highBeta;

                sample[i * 16 + 11] = (double) EEGdata[i].theta / EEGdata[i].lowAlpha;
                sample[i * 16 + 12] = (double) EEGdata[i].theta / EEGdata[i].highAlpha;
                sample[i * 16 + 13] = (double) EEGdata[i].theta / EEGdata[i].lowBeta;
                sample[i * 16 + 14] = (double) EEGdata[i].theta / EEGdata[i].highBeta;

                sample[i * 16 + 15] = (double) (EEGdata[i].delta + EEGdata[i].theta) / (EEGdata[i].lowAlpha + EEGdata[i].highAlpha + EEGdata[i].lowBeta + EEGdata[i].highBeta);
            }

            INDArray sample_to_infer = Nd4j.create(ArrayUtil.flattenDoubleArray(sample), sampleShape);
            INDArray predicted = TrainModel.model.output(sample_to_infer, false);
            INDArray index = predicted.argMax();
            int[] pl = index.toIntVector();
            currentStatus = pl[0];
            if (pl[0] == 0) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startBluetoothProcesses(); // Chỉ chạy nếu quyền được cấp
            } else {
                Toast.makeText(getContext(), "Quyền Bluetooth bị từ chối!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkAndDeleteInvalidSessions(String userId) {
        DatabaseReference userSessionsRef = FirebaseDatabase.getInstance()
                .getReference().child("BrainData").child(userId).child("sessions");

        userSessionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot sessionSnapshot : dataSnapshot.getChildren()) {
                    String sessionId = sessionSnapshot.getKey();
                    DataSnapshot infoSnapshot = sessionSnapshot.child("info");

                    if (!infoSnapshot.exists() || infoSnapshot.getValue() == null) {
                        // Xóa toàn bộ session nếu info không tồn tại hoặc rỗng
                        sessionSnapshot.getRef().removeValue()
                                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Xóa session: " + sessionId))
                                .addOnFailureListener(e -> Log.e("Firebase", "Lỗi khi xóa session: " + sessionId, e));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Lỗi khi truy xuất session: " + databaseError.getMessage());
            }
        });
    }

    private void setFailState() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "Mất kết nối!", Toast.LENGTH_SHORT).show();
                if (tgStreamReader != null) {
                    tgStreamReader.stop();
                    tgStreamReader.close();
                }
            }
        });
    }
}
