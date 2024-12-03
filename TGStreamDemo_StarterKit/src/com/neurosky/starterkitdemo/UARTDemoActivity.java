package com.neurosky.starterkitdemo;


import java.io.InputStream;

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
 * public TgStreamReader(String devName, TgStreamHandler tgStreamHandler)
 * 
 * Please remember to add libNSUART.so in this project folder libs/armeabi/libNSUART.so
 *
 */
public class UARTDemoActivity extends Activity {

	private TgStreamReader tgStreamReader;
	private static String dev = "/dev/ttyMT0";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.first_view);

		initView();
		setUpDrawWaveView();
		
		// Example of constructor public TgStreamReader(String devName, TgStreamHandler tgStreamHandler)
		// devName is the name of UART, "/dev/ttyMT0" is used on pomp phone
		tgStreamReader = new TgStreamReader(dev,callback);
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
				if(tgStreamReader != null){
					tgStreamReader.stop();
					tgStreamReader.close();
				}
				
				tgStreamReader.connectAndStart();
			}
		});

		btn_stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				stop();
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
		// TODO Auto-generated method stub
		if(tgStreamReader != null){
			tgStreamReader.close();
		}
		tgStreamReader = null;
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	// TODO view
	DrawWaveView waveView = null;
	

	public void setUpDrawWaveView() {
		// TODO use self view to drawing ECG
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

	private TgStreamHandler callback = new TgStreamHandler() {

		@Override
		public void onStatesChanged(int connectionStates) {
			// TODO Auto-generated method stub
			Log.d(TAG, "connectionStates change to: " + connectionStates);
			switch (connectionStates) {
			case ConnectionStates.STATE_CONNECTED:
				//sensor.start();
				showToast("Connected", Toast.LENGTH_SHORT);
				break;
			case ConnectionStates.STATE_WORKING:
				
				break;
			case ConnectionStates.STATE_GET_DATA_TIME_OUT:
				// can't get data
				break;
			case ConnectionStates.STATE_COMPLETE:
				//read file complete
				break;
			case ConnectionStates.STATE_STOPPED:
				break;
			case ConnectionStates.STATE_DISCONNECTED:
				break;
			case ConnectionStates.STATE_ERROR:
				break;
			}
			Message msg = LinkDetectedHandler.obtainMessage();
			msg.what = MSG_UPDATE_STATE;
			msg.arg1 = connectionStates;
			LinkDetectedHandler.sendMessage(msg);
		}

		@Override
		public void onRecordFail(int a) {
			// TODO Auto-generated method stub
			Log.e(TAG,"onRecordFail: " +a);

		}

		@Override
		public void onChecksumFail(byte[] payload, int length, int checksum) {
			// TODO Auto-generated method stub
			
			badPacketCount ++;
			Message msg = LinkDetectedHandler.obtainMessage();
			msg.what = MSG_UPDATE_BAD_PACKET;
			msg.arg1 = badPacketCount;
			LinkDetectedHandler.sendMessage(msg);

		}

		@Override
		public void onDataReceived(int datatype, int data, Object obj) {
			// TODO Auto-generated method stub
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

	private static final String TAG = UARTDemoActivity.class.getSimpleName();
	int raw;
	private Handler LinkDetectedHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

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
		UARTDemoActivity.this.runOnUiThread(new Runnable()    
        {    
            public void run()    
            {    
            	Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }    
    
        });  
	}
}
