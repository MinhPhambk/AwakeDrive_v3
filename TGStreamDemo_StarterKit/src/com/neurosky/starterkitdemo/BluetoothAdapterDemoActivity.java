package com.neurosky.starterkitdemo;


import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;
import com.neurosky.connection.DataType.BodyDataType;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity demonstrates how to use the constructor:
 * public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
 * and related functions:
 * (1) Make sure that the device supports Bluetooth and Bluetooth is on 
 * (2) setGetDataTimeOutTime
 * (3) startLog
 * (4) Using connect() and start() to replace connectAndStart()
 * (5) isBTConnected
 * (6) Use close() to release resource 
 * (7) Demo of TgStreamHandler
 * (8) Demo of BodyDataType
 * (9) Demo of recording raw data
 *
 */
public class BluetoothAdapterDemoActivity extends Activity {

	private static final String TAG = BluetoothAdapterDemoActivity.class.getSimpleName();
	private TgStreamReader tgStreamReader;

	private BluetoothAdapter mBluetoothAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.first_view);

		initView();
		setUpDrawWaveView();

		try {
			// (1) Make sure that the device supports Bluetooth and Bluetooth is on 
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
				Toast.makeText(
						this,
						"Please enable your Bluetooth and re-run this program !",
						Toast.LENGTH_LONG).show();
				finish();
//				return;
			}  
		} catch (Exception e) {
			e.printStackTrace();
			Log.i(TAG, "error:" + e.getMessage());
			return;
		}
		
		// Example of constructor public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
		tgStreamReader = new TgStreamReader(mBluetoothAdapter,callback);
		// (2) Demo of setGetDataTimeOutTime, the default time is 5s, please call it before connect() of connectAndStart()
		tgStreamReader.setGetDataTimeOutTime(6);
		// (3) Demo of startLog, you will get more sdk log by logcat if you call this function
	}

	private TextView tv_ps = null;
	private TextView  tv_connection = null;
	private TextView  tv_hr = null;
	
	private TextView  tv_badpacket = null;
	
	private Button btn_start = null;
	private Button btn_stop = null;
	private LinearLayout wave_layout;
	
	private int badPacketCount = 0;

	private void initView() {
		tv_ps = (TextView) findViewById(R.id.tv_ps);
		tv_connection = (TextView) findViewById(R.id.tv_connection);
		tv_hr = (TextView) findViewById(R.id.tv_hr);

		tv_badpacket = (TextView) findViewById(R.id.tv_badpacket);
		
		
		btn_start = (Button) findViewById(R.id.btn_start);
		btn_stop = (Button) findViewById(R.id.btn_stop);
		wave_layout = (LinearLayout) findViewById(R.id.wave_layout);
		
		btn_start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				badPacketCount = 0;
				
				// (5) demo of isBTConnected 
				if(tgStreamReader != null && tgStreamReader.isBTConnected()){
					
					// Prepare for connecting
					tgStreamReader.stop();
					tgStreamReader.close();
				}
				
				// (4) Demo of  using connect() and start() to replace connectAndStart(),
				// please call start() when the state is changed to STATE_CONNECTED
				tgStreamReader.connect();
//				tgStreamReader.connectAndStart();
			}
		});

		btn_stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				
				tgStreamReader.stop();
				tgStreamReader.close();
			}

		});
	}

	public void stop() {
		if(tgStreamReader != null){
			tgStreamReader.stop();
			tgStreamReader.close();
		}
	}

	@Override
	protected void onDestroy() {
		//(6) use close() to release resource 
		if(tgStreamReader != null){
			tgStreamReader.close();
			tgStreamReader = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		stop();
	}

	DrawWaveView waveView = null;

	public void setUpDrawWaveView() {
		waveView = new DrawWaveView(getApplicationContext());
		wave_layout.addView(waveView, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		waveView.setValue(2048, 4000, -4000);
	}

	public void updateWaveView(int data) {
		if (waveView != null) {
			waveView.updateData(data);
		}
	}
	
    // (7) demo of TgStreamHandler
	private TgStreamHandler callback = new TgStreamHandler() {

		@Override
		public void onStatesChanged(int connectionStates) {
			// TODO Auto-generated method stub
			Log.d(TAG, "connectionStates change to: " + connectionStates);
			switch (connectionStates) {
			case ConnectionStates.STATE_CONNECTING:
				// Do something when connecting
				break;
			case ConnectionStates.STATE_CONNECTED:
				// Do something when connected
				tgStreamReader.start();
				showToast("Connected", Toast.LENGTH_SHORT);
				break;
			case ConnectionStates.STATE_WORKING:
				// Do something when working
				
				//(9) demo of recording raw data , stop() will call stopRecordRawData,
				//or you can add a button to control it
				tgStreamReader.startRecordRawData();
				
				break;
			case ConnectionStates.STATE_GET_DATA_TIME_OUT:
				// Do something when getting data timeout
				
				//(9) demo of recording raw data, exception handling
				tgStreamReader.stopRecordRawData();
				
				showToast("Get data time out!", Toast.LENGTH_SHORT);
				break;
			case ConnectionStates.STATE_STOPPED:
				// Do something when stopped
				// We have to call tgStreamReader.stop() and tgStreamReader.close() much more than 
				// tgStreamReader.connectAndstart(), because we have to prepare for that.
				
				break;
			case ConnectionStates.STATE_DISCONNECTED:
				// Do something when disconnected
				break;
			case ConnectionStates.STATE_ERROR:
				// Do something when you get error message
				break;
			case ConnectionStates.STATE_FAILED:
				// Do something when you get failed message
				// It always happens when open the BluetoothSocket error or timeout
				// Maybe the device is not working normal.
				// Maybe you have to try again
				break;
			}
			Message msg = LinkDetectedHandler.obtainMessage();
			msg.what = MSG_UPDATE_STATE;
			msg.arg1 = connectionStates;
			LinkDetectedHandler.sendMessage(msg);
			

		}

		@Override
		public void onRecordFail(int flag) {
			// You can handle the record error message here
			Log.e(TAG,"onRecordFail: " +flag);

		}

		@Override
		public void onChecksumFail(byte[] payload, int length, int checksum) {
			// You can handle the bad packets here.
			badPacketCount ++;
			Message msg = LinkDetectedHandler.obtainMessage();
			msg.what = MSG_UPDATE_BAD_PACKET;
			msg.arg1 = badPacketCount;
			LinkDetectedHandler.sendMessage(msg);

		}

		@Override
		public void onDataReceived(int datatype, int data, Object obj) {
			// You can handle the received data here
			// You can feed the raw data to algo sdk here if necessary.
			
			Message msg = LinkDetectedHandler.obtainMessage();
			msg.what = datatype;
			msg.arg1 = data;
			msg.obj = obj;
			LinkDetectedHandler.sendMessage(msg);
			//Log.i(TAG,"onDataReceived");
		}

	};

	private boolean isPressing = false;
	private static final int MSG_UPDATE_BAD_PACKET = 1001;
	private static final int MSG_UPDATE_STATE = 1002;

	int raw;
	private Handler LinkDetectedHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// (8) demo of BodyDataType
			switch (msg.what) {
			
			
			case BodyDataType.CODE_RAW:
				if(isPressing){
					updateWaveView(msg.arg1);
				}else{
					updateWaveView(msg.arg1);//0
				}
				break;
			case BodyDataType.CODE_HEATRATE:
				Log.d(TAG, "CODE_HEATRATE" + msg.arg1);
				tv_hr.setText("" +msg.arg1 );
				break;

			case BodyDataType.CODE_POOR_SIGNAL:
				int poorSignal = msg.arg1;
				Log.d(TAG, "poorSignal:" + poorSignal);
				tv_ps.setText(""+msg.arg1);

				if (poorSignal == 200) {
					isPressing = true;
				}
				if (poorSignal == 0) {
					if (isPressing) {
						isPressing = false;
					}
				}

				break;
			case MSG_UPDATE_BAD_PACKET:
				tv_badpacket.setText("" + msg.arg1);
				
				break;
			case MSG_UPDATE_STATE:
				tv_connection.setText(""+msg.arg1);
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	
	public void showToast(final String msg,final int timeStyle){
		BluetoothAdapterDemoActivity.this.runOnUiThread(new Runnable()    
        {    
            public void run()    
            {    
            	Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }    
    
        });  
	}
}
