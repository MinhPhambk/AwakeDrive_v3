package com.example.brainwave.fragment;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.brainwave.AlertService;
import com.example.brainwave.AttentionActivity;
import com.example.brainwave.DrawWaveView;
import com.example.brainwave.LocalDataSet;
import com.example.brainwave.R;
import com.example.brainwave.TrainModel;
import com.example.brainwave.UpdateActivity;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;

public class HomeFragment extends Fragment {
    private static final String TAG = AttentionActivity.class.getSimpleName();
    private TgStreamReader tgStreamReader;

    private BluetoothAdapter mBluetoothAdapter;
    public static Intent intent;

    private TextView tv_attention_value;
    private TextView tv_attention_notification;
    private int badPacketCount = 0;
    private int numbeOfSamples = 0;
    private static final int MAX_SAMPLES = 5;
    private static final int THRESHOLD = 80;

    private Button btn_start;
    private Button btn_stop, button_update;
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


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        checkLocationPermission();
        return inflater.inflate(R.layout.awake_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkBluetoothPermission();
        intent = new Intent(getContext(), AlertService.class);

        initView();
        setUpDrawWaveView();

        try {
            // Ensure Bluetooth is supported and enabled
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Toast.makeText(
                        getContext(),
                        "Please enable your Bluetooth and re-run this program!",
                        Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Error: " + e.getMessage());
            return;
        }

        tgStreamReader = new TgStreamReader(mBluetoothAdapter, callback);
        tgStreamReader.setGetDataTimeOutTime(6);
        tgStreamReader.startLog();
    }

    private void checkBluetoothPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 99);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Nếu quyền chưa được cấp, yêu cầu quyền từ người dùng
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Quyền đã được cấp
            initializeBluetooth();
        }
    }
    private void initializeBluetooth() {
        Toast.makeText(requireContext(), "Bluetooth được khởi tạo!", Toast.LENGTH_SHORT).show();
        // Add Bluetooth initialization code here
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
    private void initView() {
        tv_attention_value = getView().findViewById(R.id.tv_attention_value);
        tv_attention_notification = getView().findViewById(R.id.tv_attention_notification);
        button_update = getView().findViewById(R.id.btn_update);
        btn_start = getView().findViewById(R.id.btn_attention_start);
        btn_stop = getView().findViewById(R.id.btn_attention_stop);
        wave_layout = getView().findViewById(R.id.wave_layout);

        btn_start.setOnClickListener(v -> {
            if (isProcessing) {
                return;
            }
            showToast("Connecting...", Toast.LENGTH_SHORT);
            numbeOfSamples = 0;
            isProcessing = true;
            tv_attention_notification.setText("Monitoring ...");

            badPacketCount = 0;

            // Load model
            try {
                if (TrainModel.model == null) {
                    File pathFile = Paths.get("app/src/main/java/trained_nn.zip").toAbsolutePath().toFile();
                    TrainModel.model = ModelSerializer.restoreMultiLayerNetwork(pathFile, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (tgStreamReader != null && tgStreamReader.isBTConnected()) {
                tgStreamReader.stop();
                tgStreamReader.close();
            }

            tgStreamReader.connect();
        });

        btn_stop.setOnClickListener(v -> stop());

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

    private void stop() {
        if (tgStreamReader != null) {
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
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTING:
                    break;
                case ConnectionStates.STATE_CONNECTED:
                    tgStreamReader.start();
                    showToast("Connected", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_WORKING:
                    tgStreamReader.startRecordRawData();

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
        }

        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = datatype;
            msg.arg1 = data;
            msg.obj = obj;
            LinkDetectedHandler.sendMessage(msg);
        }

        @Override
        public void onRecordFail(int flag) {
            Log.e(TAG, "onRecordFail: " + flag);
        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
            badPacketCount++;
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_BAD_PACKET;
            msg.arg1 = badPacketCount;
            LinkDetectedHandler.sendMessage(msg);
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
                case MindDataType.CODE_EEGPOWER:
                    if (isPoorSignal) {
                        isPoorSignal = false;
                        break;
                    }
                    EEGPower power = (EEGPower) msg.obj;
                    if (power.isValidate()) {
                        if (numbeOfSamples >= MAX_SAMPLES) {
                            numbeOfSamples = 0;
                            dataForInfer = dataCollected.clone();
                            new AsyncTaskInfer().execute();
                        }
                        dataCollected[numbeOfSamples] = power;
                        numbeOfSamples++;
                    }
                    break;
                case MindDataType.CODE_POOR_SIGNAL:
                    int poorSignal = msg.arg1;
                    if (poorSignal > 0) {
                        isPoorSignal = true;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private class AsyncTaskInfer extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Lấy dữ liệu EEG để phân loại
            EEGPower[] EEGdata = dataForInfer.clone();
            double[] sample = new double[NUMBER_OF_FEATURES];

            for (int i = 0; i < MAX_SAMPLES; i++) {
                if (i * 16 + 15 >= sample.length) break;  // Tránh lỗi ngoài phạm vi mảng

                sample[i * 16] = EEGdata[i].delta;
                sample[i * 16 + 1] = EEGdata[i].theta;
                sample[i * 16 + 2] = EEGdata[i].lowAlpha;
                sample[i * 16 + 3] = EEGdata[i].highAlpha;
                sample[i * 16 + 4] = EEGdata[i].lowBeta;
                sample[i * 16 + 5] = EEGdata[i].highBeta;

                // Cách tính các chỉ số tỷ lệ
                if (EEGdata[i].theta != 0)
                    sample[i * 16 + 6] = (double) EEGdata[i].delta / EEGdata[i].theta;
                if (EEGdata[i].lowAlpha != 0)
                    sample[i * 16 + 7] = (double) EEGdata[i].delta / EEGdata[i].lowAlpha;
                if (EEGdata[i].highAlpha != 0)
                    sample[i * 16 + 8] = (double) EEGdata[i].delta / EEGdata[i].highAlpha;
                if (EEGdata[i].lowBeta != 0)
                    sample[i * 16 + 9] = (double) EEGdata[i].delta / EEGdata[i].lowBeta;
                if (EEGdata[i].highBeta != 0)
                    sample[i * 16 + 10] = (double) EEGdata[i].delta / EEGdata[i].highBeta;

                if (EEGdata[i].lowAlpha != 0)
                    sample[i * 16 + 11] = (double) EEGdata[i].theta / EEGdata[i].lowAlpha;
                if (EEGdata[i].highAlpha != 0)
                    sample[i * 16 + 12] = (double) EEGdata[i].theta / EEGdata[i].highAlpha;
                if (EEGdata[i].lowBeta != 0)
                    sample[i * 16 + 13] = (double) EEGdata[i].theta / EEGdata[i].lowBeta;
                if (EEGdata[i].highBeta != 0)
                    sample[i * 16 + 14] = (double) EEGdata[i].theta / EEGdata[i].highBeta;

                // Tính tỷ lệ kết hợp giữa các dải tần
                double denominator = EEGdata[i].lowAlpha + EEGdata[i].highAlpha + EEGdata[i].lowBeta + EEGdata[i].highBeta;
                if (denominator != 0) {
                    sample[i * 16 + 15] = (double) (EEGdata[i].delta + EEGdata[i].theta) / denominator;
                }
            }

            // Chuyển đổi mảng sample thành INDArray
            INDArray sample_to_infer = Nd4j.create(ArrayUtil.flattenDoubleArray(sample), sampleShape);

            // Sử dụng mô hình để dự đoán
            INDArray predicted = TrainModel.model.output(sample_to_infer, false);
            INDArray index = predicted.argMax();
            int[] pl = index.toIntVector();

            // Cập nhật trạng thái dự đoán
            currentStatus = pl[0];

            // Kiểm tra điều kiện để kích hoạt cảnh báo
            if (pl[0] == 0) {
                alertService();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Cập nhật UI với kết quả phân loại
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
