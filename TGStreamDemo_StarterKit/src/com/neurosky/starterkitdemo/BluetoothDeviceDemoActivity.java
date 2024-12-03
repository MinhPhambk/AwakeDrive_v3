package com.neurosky.starterkitdemo;

import java.lang.reflect.Method;
import java.util.Set;

import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;
import com.neurosky.connection.DataType.BodyDataType;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This activity demonstrates how to use the constructor:
 * public TgStreamReader(BluetoothDevice mBluetoothDevice,TgStreamHandler tgStreamHandler)
 * and related functions:
 * (1) changeBluetoothDevice
 * (2) Demo of drawing ECG
 * (3) Demo of getting Bluetooth device dynamically
 * (4) setTgStreamHandler
 *
 */
public class BluetoothDeviceDemoActivity extends Activity {
	private static final String TAG = BluetoothDeviceDemoActivity.class.getSimpleName();
	private TgStreamReader tgStreamReader;
	
	// TODO connection sdk
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBluetoothDevice;
	private String address = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.bluetoothdevice_view);

		initView();
		setUpDrawWaveView();

		try {
			// TODO	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i(TAG, "error:" + e.getMessage());
			return;
		}
	}

	private TextView tv_ps = null;
	private TextView  tv_connection = null;
	private TextView  tv_hr = null;
	
	private TextView  tv_badpacket = null;
	
	private Button btn_start = null;
	private Button btn_stop = null;
	private Button btn_selectdevice = null;
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
				showToast("connecting ...",Toast.LENGTH_SHORT);
				start();
			}
		});

		btn_stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(tgStreamReader != null){
					tgStreamReader.stop();
				}
			}

		});
		
		btn_selectdevice =  (Button) findViewById(R.id.btn_selectdevice);
		
		btn_selectdevice.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				scanDevice();
			}

		});
	}
	
	private void start(){
		if(address != null){
			BluetoothDevice bd = mBluetoothAdapter.getRemoteDevice(address);
			createStreamReader(bd);

			tgStreamReader.connectAndStart();
		}else{
			showToast("Please select device first!", Toast.LENGTH_SHORT);
		}
	}

	public void stop() {
		if(tgStreamReader != null){
			tgStreamReader.stop();
			tgStreamReader.close();//if there is not stop cmd, please call close() or the data will accumulate 
			tgStreamReader = null;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		super.onStop();
		stop();
	}

	// TODO view
	DrawWaveView waveView = null;
	// (2) demo of drawing ECG, set up of view
	public void setUpDrawWaveView() {
		
		waveView = new DrawWaveView(getApplicationContext());
		wave_layout.addView(waveView, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		waveView.setValue(2048, 4000, -4000);
	}
	// (2) demo of drawing ECG, update view
	public void updateWaveView(int data) {
		if (waveView != null) {
			waveView.updateData(data);
		}
	}
	private int currentState = 0;
	private TgStreamHandler callback = new TgStreamHandler() {

		@Override
		public void onStatesChanged(int connectionStates) {
			// TODO Auto-generated method stub
			Log.d(TAG, "connectionStates change to: " + connectionStates);
			currentState  = connectionStates;
			switch (connectionStates) {
			case ConnectionStates.STATE_CONNECTED:
				//sensor.start();
				showToast("Connected", Toast.LENGTH_SHORT);
				break;
			case ConnectionStates.STATE_WORKING:
				//byte[] cmd = new byte[1];
				//cmd[0] = 's';
				//tgStreamReader.sendCommandtoDevice(cmd);
				
				break;
			case ConnectionStates.STATE_GET_DATA_TIME_OUT:
				//get data time out
				break;
			case ConnectionStates.STATE_COMPLETE:
				//read file complete
				break;
			case ConnectionStates.STATE_STOPPED:
				break;
			case ConnectionStates.STATE_DISCONNECTED:
				break;
			case ConnectionStates.STATE_ERROR:
				Log.d(TAG,"Connect error, Please try again!");
				break;
			case ConnectionStates.STATE_FAILED:
				Log.d(TAG,"Connect failed, Please try again!");
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
	private static final int MSG_CONNECT = 1003;


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
				if (poorSignal == 0) {// 鍙屾墜鏉惧紑
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
				break;
				
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	
	public void showToast(final String msg,final int timeStyle){
		BluetoothDeviceDemoActivity.this.runOnUiThread(new Runnable()    
        {    
            public void run()    
            {    
            	Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }    
    
        });  
	}
	
	//show device list while scanning
	private ListView list_select;
	private BTDeviceListAdapter deviceListApapter = null;
	private Dialog selectDialog;
	
	// (3) Demo of getting Bluetooth device dynamically
    public void scanDevice(){

    	if(mBluetoothAdapter.isDiscovering()){
    		mBluetoothAdapter.cancelDiscovery();
    	}
    	
    	setUpDeviceListView();
    	//register the receiver for scanning
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);
    	
    	mBluetoothAdapter.startDiscovery();
    }
    
 private void setUpDeviceListView(){
    	
    	LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.dialog_select_device, null);
		list_select = (ListView) view.findViewById(R.id.list_select);
		selectDialog = new Dialog(this, R.style.dialog1);
		selectDialog.setContentView(view);
    	//List device dialog

    	deviceListApapter = new BTDeviceListAdapter(this);
    	list_select.setAdapter(deviceListApapter);
    	list_select.setOnItemClickListener(selectDeviceItemClickListener);
    	
    	selectDialog.setOnCancelListener(new OnCancelListener(){

			@Override
			public void onCancel(DialogInterface arg0) {
				// TODO Auto-generated method stub
				Log.e(TAG,"onCancel called!");
				BluetoothDeviceDemoActivity.this.unregisterReceiver(mReceiver);
			}
    		
    	});
    	
    	selectDialog.show();
    	
    	Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
    	for(BluetoothDevice device: pairedDevices){
    		deviceListApapter.addDevice(device);
    	}
		deviceListApapter.notifyDataSetChanged();
    }
 
 //Select device operation
 private OnItemClickListener selectDeviceItemClickListener = new OnItemClickListener(){
	 
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
			// TODO Auto-generated method stub
			Log.d(TAG, "Rico ####  list_select onItemClick     ");
	    	if(mBluetoothAdapter.isDiscovering()){
	    		mBluetoothAdapter.cancelDiscovery();
	    	}
	    	//unregister receiver
	    	BluetoothDeviceDemoActivity.this.unregisterReceiver(mReceiver);

	    	mBluetoothDevice =deviceListApapter.getDevice(arg2);
	    	selectDialog.dismiss();
	    	selectDialog = null;
	    	
			Log.d(TAG,"onItemClick name: "+mBluetoothDevice.getName() + " , address: " + mBluetoothDevice.getAddress() );
			address = mBluetoothDevice.getAddress().toString();
			
			//ger remote device
			BluetoothDevice remoteDevice = mBluetoothAdapter.getRemoteDevice(mBluetoothDevice.getAddress().toString());
         
			//bind and connect
			//bindToDevice(remoteDevice); // create bond works unstable on Samsung S5
			//showToast("pairing ...",Toast.LENGTH_SHORT);

			tgStreamReader = createStreamReader(remoteDevice); 
			tgStreamReader.connectAndStart();
		
		}
	
 };
 
 /**
	 * If the TgStreamReader is created, just change the bluetooth
	 * else create TgStreamReader, set data receiver, TgStreamHandler and parser
	 * @param bd
	 * @return TgStreamReader
	 */
	public TgStreamReader createStreamReader(BluetoothDevice bd){

		if(tgStreamReader == null){
			// Example of constructor public TgStreamReader(BluetoothDevice mBluetoothDevice,TgStreamHandler tgStreamHandler)
			tgStreamReader = new TgStreamReader(bd,callback);
			tgStreamReader.startLog();
		}else{
			// (1) Demo of changeBluetoothDevice
			tgStreamReader.changeBluetoothDevice(bd);
			
			// (4) Demo of setTgStreamHandler, you can change the data handler by this function
			tgStreamReader.setTgStreamHandler(callback);
		}
		return tgStreamReader;
	}
 
 /**
  * Check whether the given device is bonded, if not, bond it 
  * @param bd
  */
 public void bindToDevice(BluetoothDevice bd){
 	    int ispaired = 0;
		if(bd.getBondState() != BluetoothDevice.BOND_BONDED){
			//ispaired = remoteDevice.createBond();
			try {
				//Set pin
				if(Utils.autoBond(bd.getClass(), bd, "0000")){
					ispaired += 1;
				}
				//bind to device
				if(Utils.createBond(bd.getClass(), bd)){
					ispaired += 2;
				}
				Method createCancelMethod=BluetoothDevice.class.getMethod("cancelBondProcess");
                boolean bool=(Boolean)createCancelMethod.invoke(bd);
                Log.d(TAG,"bool="+bool);
					
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.d(TAG, " paire device Exception:    " + e.toString());	
			}
		}
		Log.d(TAG, " ispaired:    " + ispaired);	

 }
 
//The BroadcastReceiver that listens for discovered devices 
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
				Log.d(TAG, "mReceiver()");
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Log.d(TAG,"mReceiver found device: " + device.getName());
				
				// update to UI
				deviceListApapter.addDevice(device);
				deviceListApapter.notifyDataSetChanged();

			} 
		}
	};
    

}
