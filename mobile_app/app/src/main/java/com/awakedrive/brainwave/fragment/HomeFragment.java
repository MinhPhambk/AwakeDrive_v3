package com.awakedrive.brainwave.fragment;

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
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.awakedrive.brainwave.DrawWaveView;
import com.awakedrive.brainwave.Interface.SoundManager;
import com.awakedrive.brainwave.LocalDataSet;
import com.awakedrive.brainwave.R;
import com.awakedrive.brainwave.TrainModel;
import com.awakedrive.brainwave.activity.DetailActivity;
import com.awakedrive.brainwave.Utils;
import com.awakedrive.brainwave.adapter.DeviceAdapter;
import com.awakedrive.brainwave.model.Device;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();
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

    private static int currentStatus = 1;
    private static boolean isLoading = false;
    private static boolean isLoaded = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private static String server_url = "https://server-production-6a93.up.railway.app";
    private ConstraintLayout contraint_connect;
    private ConstraintLayout contraint_connected;
    private CardView cardView3;
    private RecyclerView rvConnected;
    private RecyclerView rvAvailable;
    private ImageView ivRefreshConnected;
    private ImageView ivRefreshAvailable;
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
    private int status = -1;
    private String uid = null;
    private boolean isRecording = true;
    private long startTime = 0;
    private long endTime = 0;
    private String sessionId;
    private int seconds = 0;
    private SoundManager soundManager;
    private boolean running = false;
    private int alertnessLevel;
    TextView level_alert;
    private boolean isReceiverRegistered = false;
    private Handler handler_alert = new Handler();
    private Handler handler = new Handler(Looper.getMainLooper());
    private long startTimestamp = 0;





    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // Kiểm tra Fragment có còn gắn với Activity không
                if (!isAdded() || getContext() == null) {
                    return; // Ngăn lỗi khi Fragment không gắn vào Context
                }

                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
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

    private AudioManager audioManager;
    private ContentObserver volumeObserver;
    private int threshold;
    private boolean isFirstRun = true;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        btn_start = getView().findViewById(R.id.btn_attention_start);
        audioManager = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        threshold = (int) (maxVolume * 0.4); // 40% mức tối đa

        // Kiểm tra và cập nhật trạng thái nút ngay từ đầu
        updateButtonState();

        // Tạo ContentObserver để lắng nghe thay đổi âm lượng
        volumeObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange) {
                updateButtonState();
            }
        };

        // Đăng ký ContentObserver theo dõi thay đổi âm lượng
        requireContext().getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, volumeObserver);

        initView(view);
        // load model
        AsyncTaskLoadModel runner = new AsyncTaskLoadModel();
        runner.execute();
        printHashKey(getContext());
//        try {
//            System.loadLibrary("jnind4jcpu");
//            Log.i("Library", "Load library thành công!");
//        } catch (UnsatisfiedLinkError e) {
//            Log.e("Library", "Lỗi load library", e);
//        }

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            btn_start.setBackgroundColor(Color.BLACK);
            btn_start.setTextColor(Color.WHITE);
            btn_stop.setBackgroundColor(Color.BLACK);
            btn_stop.setTextColor(Color.WHITE);
        } else {
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
        connectedAdapter.setOnUnpairClickListener((device, position) -> {
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Xác nhận hủy kết nối")
                    .setMessage("Bạn có chắc chắn muốn hủy ghép nối với " + device.getName() + "?")
                    .setPositiveButton("Đồng ý", (dialog1, which) -> unpairDevice(position))
                    .setNegativeButton("Hủy", (dialog12, which) -> dialog12.dismiss())
                    .show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);

            TextView messageView = dialog.findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setTextColor(Color.BLACK);
            }
        });

        checkPermissionsAndStart();

//        intent = new Intent(getContext(), AlertService.class);

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
        startUpdatingLevel();

    }

    private void updateButtonState() {
        if (volumeObserver != null) {
            requireContext().getContentResolver().unregisterContentObserver(volumeObserver);
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
        brainData.put("status", alertnessLevel);
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
        btn_stop = getView().findViewById(R.id.btn_attention_stop);
        bt_detail = getView().findViewById(R.id.bt_detail);
        wave_layout = getView().findViewById(R.id.wave_layout);
        contraint_connect = getView().findViewById(R.id.contraint_connect);
        contraint_connected = getView().findViewById(R.id.contraint_connected);
        cardView3 = getView().findViewById(R.id.cardView3);
        txt_name_user = view.findViewById(R.id.txt_name_user);
        txt_name_user_visible = view.findViewById(R.id.txt_name_user_visible);
        tv_time = view.findViewById(R.id.tv_time);
        level_alert = view.findViewById(R.id.level_alert);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        File file = new File(getContext().getFilesDir(), "trained_nn.zip");

        if (file.exists()) {
            Log.d("CheckFile", "File đã tồn tại.");
        } else {
            Log.d("CheckFile", "File không tồn tại.");
        }

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
            Glide.with(this).load(R.drawable.baseline_account_circle_24).circleCrop().into(img_avatar_user);
            Glide.with(this).load(R.drawable.baseline_account_circle_24).circleCrop().into(img_avatar_user_visible);
        }
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (!Utils.isNetworkAvailable(requireContext())) {
                    Toast.makeText(getContext(), "Không có kết nối mạng!", Toast.LENGTH_SHORT).show();
                    return;
                }
                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

                if (currentVolume < threshold) {
                    Toast.makeText(getContext(), "Vui lòng tăng âm lượng lên ít nhất 40% để tiếp tục!", Toast.LENGTH_SHORT).show();
                    return;
                }
//                startFakeSensorData();
//                startInferTime = System.currentTimeMillis();
                startTimestamp = System.currentTimeMillis();
                isFirstRun = true;

                tv_attention_value.setText("Tỉnh táo");
                currentStatus = 1;
                Utils.is_running = true;
                soundManager.playSound();
                if (running == true) {
                    Toast.makeText(getContext(), "Vui lòng stop trước khi start lại", Toast.LENGTH_SHORT).show();
                    return;
                } else
                    running = true;
                seconds = 0;
                handler.post(updateTime);
                if (isProcessing) {
                    return;
                }
                showToast("Đang kết nối...", Toast.LENGTH_SHORT);
                numbeOfSamples = 0;
                isProcessing = true;

                badPacketCount = 0;

                // load model
                InputStream is = null;
                try {
                    is = getContext().getAssets().open("trained_nn.zip");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                File tempFile = new File(getContext().getCacheDir(), "trained_nn.zip");
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    TrainModel.model = ModelSerializer.restoreMultiLayerNetwork(tempFile, false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Log.d("TAGgggg_model", TrainModel.model + "");


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
                                lastHighBeta != -1 && lastLowBeta != -1 && lastLowGamma != -1 && lastMiddleGamma != -1 && alertnessLevel != -1) {
                            saveDataToFirebase(uid, sessionId);
                        }
                        firebaseHandler.postDelayed(this, 1000);
                    }
                };
                firebaseHandler.post(firebaseRunnable);
            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                soundManager.playSound();
                long currentTimestamp = System.currentTimeMillis();
                long elapsedSeconds = (currentTimestamp - startTimestamp) / 1000;

                if (elapsedSeconds < 90) {
                    Toast.makeText(getContext(), "Vui lòng chờ ít nhất 90 giây trước khi dừng!", Toast.LENGTH_SHORT).show();
                    return;
                }

                isFirstRun=true;
                Utils.is_running = false;
                running = false;
                handler.removeCallbacks(updateTime);
                stop();
                stopRecording();
                stopPlayer();

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

    private class AsyncTaskLoadModel extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "Loading model...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (TrainModel.model == null) {
                    // Đường dẫn tới file zip trong assets
                    String zipFileName = "trained_nn.zip";
                    File modelFile = new File(getContext().getFilesDir(), TrainModel.fileModelName);

                    // Nếu model chưa tồn tại, copy từ assets
                    if (!modelFile.exists()) {
                        copyFileFromAssets(zipFileName, modelFile);
                    }

                    // Load model từ file
                    TrainModel.model = ModelSerializer.restoreMultiLayerNetwork(modelFile, false);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean isLoaded) {
            super.onPostExecute(isLoaded);
            String message = isLoaded ? "Model loaded successfully." : "Failed to load model.";
            Log.d(TAG, message);
        }

        // Hàm copy file từ assets vào bộ nhớ trong
        private void copyFileFromAssets(String assetFileName, File outputFile) throws IOException {
            try (InputStream is = getContext().getAssets().open(assetFileName);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, length);
                }
            }
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
        FirebaseUser user = firebaseAuth.getCurrentUser();
        checkAndDeleteInvalidSessions(user.getUid());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isReceiverRegistered) {
            requireContext().unregisterReceiver(receiver);
            isReceiverRegistered = false;
        }
        Utils.is_running = false;
        running = false;
        handler.removeCallbacks(updateTime);
        stop();
        stopRecording();
        stopPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (volumeObserver != null) {
            requireContext().getContentResolver().unregisterContentObserver(volumeObserver);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isReceiverRegistered) {
            requireContext().registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            isReceiverRegistered = true;
        }
    }

    DrawWaveView waveView = null;

    private void setUpDrawWaveView() {
        waveView = new DrawWaveView(requireContext());

        wave_layout.addView(waveView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        waveView.setValue(2048, 1000, -2048);
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
                    showToast("Đã kết nối", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_WORKING:
                    tgStreamReader.startRecordRawData();
                    Log.d("Tagggggg", connectionStates + "");
                    break;
                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    tgStreamReader.stopRecordRawData();
                    showToast("Hết thời gian lấy dữ liệu!", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_STOPPED:
                    break;
                case ConnectionStates.STATE_DISCONNECTED:
                    break;
                case ConnectionStates.STATE_ERROR:
                    break;
                case ConnectionStates.STATE_FAILED:
                    setFailState();
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

    private int normalizeEEG(int rawValue) {
        final int EEG_MIN = -2048;
        final int EEG_MAX = 1000;
        int normalized = (int) (((double) (rawValue - EEG_MIN) / (EEG_MAX - EEG_MIN)) * 100);

        return Math.max(0, Math.min(100, normalized));
    }


    private void startUpdatingLevel() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (level_alert != null) {
                    level_alert.setText(alertnessLevel + "/100");
                }
                handler.postDelayed(this, 5000); // Cập nhật lại sau 5 giây
            }
        }, 5000); // Bắt đầu sau 5 giây
    }

    private Handler LinkDetectedHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MindDataType.CODE_RAW:
                    updateWaveView(msg.arg1);
                    int normalizedLevel = normalizeEEG(msg.arg1);
                    alertnessLevel = normalizedLevel;
                    Log.d("TAGgggg_data1", "handleMessage: " + normalizedLevel);
                    break;
                case MindDataType.CODE_MEDITATION:
                    Log.d(TAG, "HeadDataType.CODE_MEDITATION " + msg.arg1);
                    break;
                case MindDataType.CODE_ATTENTION:
                    Log.d(TAG, "CODE_ATTENTION " + msg.arg1);
                    break;
                case MindDataType.CODE_EEGPOWER:
//                    if (isPoorSignal == true) {
//                        isPoorSignal = false;
//                        break;
//                    }
                    Log.d("TAG_dataset", "start");

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

    private void sendEEGDataToActivity() {
        Intent intent = new Intent("com.awakedrive.brainwave.UPDATE_DATA");
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
            Log.d("TAG_background", "doInBackground: ");
            if (isFirstRun) {
                try {
                    Log.d("TAG_delay", "Sleeping for 60000 ms (first run)");
                    Thread.sleep(60000); // luôn delay 60 giây nếu isFirstRun == true
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isFirstRun = false;
            }

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
            Log.d("TAG_simpple", sample + "");
            INDArray sample_to_infer = Nd4j.create(ArrayUtil.flattenDoubleArray(sample), sampleShape);
            INDArray predicted = TrainModel.model.output(sample_to_infer, false);
            INDArray index = predicted.argMax();
            int[] pl = index.toIntVector();
            currentStatus = pl[0];
            Log.d("TAGgggg_Pl", currentStatus + "");
            alertService(pl[0]);
            return null;
        }

        //This block executes in UI when background thread finishes
        //This is where we update the UI with our classification results
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            String predicted_label = LocalDataSet.statues[currentStatus];
            Log.d("Taggggg_acb", predicted_label);
            tv_attention_value.setText(predicted_label);
        }
    }

    private int previousValue = -1;
    private long zeroStartTime = 0;
    private Handler handler_delay = new Handler();
    private boolean isPlaying = false;
    private boolean isAlertShowing = false;

    public void alertService(int value) {
        if (value == 0) {
            if (previousValue == 1) {
                zeroStartTime = System.currentTimeMillis();
            }
            handler_delay.postDelayed(() -> {
                if (value == 0 && (System.currentTimeMillis() - zeroStartTime) >= 10000 && !isPlaying) {
                    playHorn();
                    showAlert();
                }
            }, 3000);
        } else if (value == 1 && previousValue == 0) {
            // Dừng nhạc sau 5 giây
            handler_delay.postDelayed(this::stopPlayer, 5000);
            stopshowAlert();
//            handler_delay.postDelayed(this::stopshowAlert, 5000);
        }

        previousValue = value;
    }

    // MediaPlayer để phát nhạc
    private MediaPlayer player;

    // Hàm phát nhạc
    private void playHorn() {
        if (isPlaying) return; // Nếu đang phát thì không phát lại

        stopPlayer(); // Dừng nhạc trước khi phát mới

        if (player == null) {
            player = new MediaPlayer();
        }

        try {
            // Mở file từ res/raw
            AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.isochronic_tones_alert);
            if (afd == null) return;

            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            player.setOnPreparedListener(mp -> {
                player.start();
                isPlaying = true;
            });

            player.setOnCompletionListener(mp -> {
                player.seekTo(0); // Quay lại đầu file và phát lại
                player.start();
            });

            player.prepareAsync(); // Chuẩn bị phát nhạc không chặn
        } catch (Exception e) {
            Log.e("MediaPlayer", "Lỗi phát nhạc", e);
        }
    }


    // Hàm dừng nhạc
    private void stopPlayer() {
        if (player != null) {
            if (player.isPlaying()) {
                player.stop(); // Dừng phát nếu đang phát
                Log.d("MediaPlayer", "⏹️ Dừng phát nhạc.");
            }
            player.release(); // Giải phóng tài nguyên
            player = null;
            isPlaying = false;
        }
    }
    private AlertDialog alertDialog;
    private Vibrator vibrator;
    private void showAlert() {
        if (isAlertShowing) return;
        isAlertShowing = true;
        new Handler(Looper.getMainLooper()).post(() -> {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.dialog_alert_custom, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
            builder.setView(view);
            builder.setCancelable(false);

            alertDialog = builder.create();
            alertDialog.show();
            TextView titleText = view.findViewById(R.id.alert_title);
            TextView msgText = view.findViewById(R.id.alert_message);

            Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
            titleText.startAnimation(shake);

            Animation fade = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_loop);
            msgText.startAnimation(fade);

            View container = view.findViewById(R.id.dialog_alert_container);
            container.setBackgroundColor(Color.RED);
            Animation flash = AnimationUtils.loadAnimation(getContext(), R.anim.alert_flash_red);
            Animation scaleShake = AnimationUtils.loadAnimation(getContext(), R.anim.alert_shake_scale);

            container.startAnimation(scaleShake);
            container.startAnimation(flash);

            // Vibrate
            vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                long[] pattern = {0, 500, 500, 500};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
                } else {
                    vibrator.vibrate(pattern, 0);
                }
            }
        });
    }



    private void startFakeSensorData() {
        new Thread(() -> {
            int[] fakeValues = {0, 0, 1, 1};
            for (int value : fakeValues) {
                alertService(value);
                try {
                    Thread.sleep(10000);
                    Log.d("TAGggg_value", value+"");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void stopshowAlert() {
        // Dừng rung
        if (vibrator != null) {
            vibrator.cancel();
        }

        // Ẩn dialog nếu đang hiển thị
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        isAlertShowing = false;
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
                Toast.makeText(getContext(), "Không có kết nối!", Toast.LENGTH_SHORT).show();
                if (tgStreamReader != null) {
                    tgStreamReader.stop();
                    tgStreamReader.close();
                }
            }
        });
    }
}
