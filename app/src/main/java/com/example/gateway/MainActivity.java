package com.example.gateway;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.gateway.adapters.BleGattProfileListAdapter;
import com.example.gateway.ble.BleCommManager;
import com.example.gateway.ble.BlePeripheral;
import com.example.gateway.ble.GateWayConfig;
import com.example.gateway.ble.callbacks.BleScanCallbackv18;
import com.example.gateway.ble.callbacks.BleScanCallbackv21;
import com.example.gateway.models.BlePeripheralListItem;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MULTIPLE_PERMISSIONS = 42;
    private final static int REQUEST_ENABLE_BT = 1;
    private String[] list_of_permissions = new String[] {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private BleCommManager mBleCommManager;
    private	boolean	mScanningActive	= false;

    private	MenuItem GWID;
    private	MenuItem mScanProgressSpinner;
    private	MenuItem mStartScanItem, mStopScanItem;
    private ExpandableListView mBlePeripheralsListView;
    private TextView mPeripheralsListEmptyTV;
    private BleGattProfileListAdapter mBlePeripheralsListAdapter;
    private DBManager db;
    private Map<Integer, BlePeripheral> sensorList = new HashMap<>();
    private ArrayList<BluetoothDevice> disposit = new ArrayList<>();
    private int count;
    Activity a;

    private ExpandableListView log;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setFinishOnTouchOutside(false);
        setContentView(R.layout.activity_main);

        checkPermissions();
        initUI();
        db = new DBManager(this);
        db.open();
        a = MainActivity.this;

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    private void initUI(){
        mPeripheralsListEmptyTV = (TextView)findViewById(R.id.peripheral_list_empty);
        mBlePeripheralsListView = (ExpandableListView) findViewById(R.id.peripherals_list);
        mBlePeripheralsListAdapter = new BleGattProfileListAdapter(sensorList, getApplicationContext());
        mBlePeripheralsListView.setAdapter(mBlePeripheralsListAdapter);
        mBlePeripheralsListView.setEmptyView(mPeripheralsListEmptyTV);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBluetooth();
        configInitialization();
    }

    private void writeToFile(File file, String data) {
        data = data.trim();
        try {
            PrintWriter p = new PrintWriter(new FileOutputStream(file, true));
            p.println(data);
            p.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //	stop	scanning	when	the	activity	pauses
        if(mBleCommManager.getmBluetoothAdapter().isEnabled()) {
            mBleCommManager.stopScanning(mBleScanCallbackv18, mScanCallbackv21);
        }
        try {
            unregisterReceiver(mBluetoothAdvertiseReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "receiver	not	registered");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //	Inflate	the	menu;
        //	this	adds	items	to	the	action	bar	if	it	is	present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        GWID = menu.findItem(R.id.GWID);
        GWID.setVisible(true);
        GWID.setTitle(GateWayConfig.GateWay);
        mStartScanItem = menu.findItem(R.id.action_start_scan);
        mStopScanItem =	 menu.findItem(R.id.action_stop_scan);
        mStopScanItem.setVisible(false);
        mScanProgressSpinner = menu.findItem(R.id.scan_progress_item);
        mScanProgressSpinner.setVisible(false);
        return	true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //	Start	a	BLE	scan	when	a	user	clicks	the	"start	scanning"	menu	button
        //	and	stop	a	BLE	scan
        //	when	a	user	clicks	the	"stop	scanning"	menu	button
        switch(item.getItemId()){
            case R.id.action_start_scan:
                //	User	chose	the	"Scan"	item
                startScan();
                Toast.makeText(MainActivity.this, "Action clicked", Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_stop_scan:
                //	User	chose	the	"Stop"	item
                stopScan();
                return true;
            default:
                //	If	we	got	here,	the	user's	action	was	not	recognized.
                //	Invoke	the	superclass	to	handle	it.
                return super.onOptionsItemSelected(item);
        }
    }

    public void initBluetooth() {
        //	notify	when	bluetooth	is	turned	on	or	off
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothAdvertiseReceiver, filter);
        try {
            mBleCommManager = new BleCommManager(this);
        } catch (Exception e) {
            Toast.makeText(this, "Could	not	initialize	bluetooth", Toast.LENGTH_SHORT).show();
            Log.e(TAG, e.getMessage());
            finish();
        }
        //	should	prompt	user	to	open	settings	if	Bluetooth	is	not	enabled.
        if (mBleCommManager.getmBluetoothAdapter().isEnabled()) {
            // Update bluetooth UI
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        mBleCommManager.setmBluetoothLeScanner();
    }

    public void startScan(){
        //update UI
        mStartScanItem.setVisible(false);
        mStopScanItem.setVisible(true);
        mScanProgressSpinner.setVisible(true);
        //	clear	the	list	of	Peripherals	and	start	scanning
        for(int i = 0; i< sensorList.size(); i++){
            if(sensorList.get(i).isConnected()){
                sensorList.get(i).disconnect();
            }
        }
        mBlePeripheralsListAdapter.clear();
        disposit.clear();
        sensorList.clear();
        mBlePeripheralsListView.setAdapter(mBlePeripheralsListAdapter);
        try{
            mScanningActive	= true;
            count = 0;
            mBleCommManager.scanForPeripherals(mBleScanCallbackv18, mScanCallbackv21);
        }	catch(Exception	e){
            Log.e(TAG,"Could not open Ble Device Scanner");
        }

    }

    public void stopScan(){mBleCommManager.stopScanning(mBleScanCallbackv18, mScanCallbackv21);}

    public void onBleScanStopped()	{
        //	update	UI	compenents	to	reflect	that	a	BLE	scan	has	stopped
        //	Possible	for	this	method	to	be	called	before	the	menu	has	been	created
        //	Check	to	see	if	menu	items	are	initialized,	or	Activity	will	crash
        mScanningActive = false;
        if(mStopScanItem != null) mStopScanItem.setVisible(false);
        if(mScanProgressSpinner != null){
            mScanProgressSpinner.setVisible(false);
        }
        if(mStartScanItem != null) mStartScanItem.setVisible(true);
    }

    public void onBlePeripheralDiscovered(final BluetoothDevice bluetoothDevice){
        Log.v(TAG,	"Found "+bluetoothDevice.getName()+", " +	bluetoothDevice.getAddress());
        //	only	add	the	peripheral	if
        //	-	it	has	a	name,	on
        //	-	doesn't	already	exist	in	our	list,	or
        //	-	is	transmitting	at	a	higher	power	(is	closer)
        //			than	a	similar	peripheral
        boolean addPeripheral = true;
        if(bluetoothDevice.getName() == null){
            addPeripheral = false;
        }
        for(BlePeripheralListItem listItem : mBlePeripheralsListAdapter.getItems())	{
            if(listItem.getDeviceMac().equals(bluetoothDevice.getAddress()))
            {
                addPeripheral = false;
            }
        }
        if	(addPeripheral)	{
            mBlePeripheralsListAdapter.addBluetoothPeripheral(bluetoothDevice);
            disposit.add(bluetoothDevice);


            if(sensorList.get(count) == null){
                BlePeripheral blePeripheral = new BlePeripheral(disposit.get(count), getApplicationContext(), db, a);
                blePeripheral.setDeviceId(count);
                sensorList.put(count,blePeripheral);
                sensorList.get(count).setSensorTile(bluetoothDevice.getName());
            }
            count = count + 1;

            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    mBlePeripheralsListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private	final BleScanCallbackv21 mScanCallbackv21 = new BleScanCallbackv21(){
        @Override
        public void onScanResult(int callbackType, ScanResult result){
            BluetoothDevice bluetoothDevice = result.getDevice();
            onBlePeripheralDiscovered(bluetoothDevice);
        }
        @Override
        public	void	onBatchScanResults(List<ScanResult> results){
            for	(ScanResult result : results){
                BluetoothDevice	bluetoothDevice	=	result.getDevice();
                onBlePeripheralDiscovered(bluetoothDevice);
            }
        }
        @Override
        public void onScanFailed(int errorCode){
            switch(errorCode){
                case SCAN_FAILED_ALREADY_STARTED:
                    Log.e(TAG,"Scan	with	the	same	settings	already	started");
                    break;
                case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    Log.e(TAG,"App	cannot	be	registered.");
                    break;
                case	SCAN_FAILED_FEATURE_UNSUPPORTED: Log.e(TAG,	"Power	optimized	scan	is	not	supported.");
                    break;
                default:	//	SCAN_FAILED_INTERNAL_ERROR
                    Log.e(TAG,	"Fails	to	start	scan	due	an	internal	error");
            }
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    onBleScanStopped();
                }
            });
        }
        public void onScanComplete(){
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    onBleScanStopped();
                }
            });
        }
    };

    public final BleScanCallbackv18 mBleScanCallbackv18	= new BleScanCallbackv18(){
                @Override
                public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord){
                    onBlePeripheralDiscovered(bluetoothDevice);
                }
                @Override
                public void onScanComplete(){
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            onBleScanStopped();
                        }
                    });
                }
    };





    private final BroadcastReceiver mBluetoothAdvertiseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        //initBluetooth();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Update bluetooth UI
                            }
                        });
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }

            }

        }
    };

    private void configInitialization(){
        File folder = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "GateWayConfig");
        Log.e(TAG,"DIRECTORY = "+folder.mkdir()+"___"+folder);
        File filePointer = new File(folder + File.separator + "GWConfig.txt");

        try {
            if (!filePointer.exists()) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(filePointer, true));
                writer.append(GateWayConfig.cloudAddress+"\n");
                writer.append("type_Connected="+GateWayConfig.type_Connected+"\n");
                writer.append("type_Disconnected="+GateWayConfig.type_Disconnected+"\n");
                writer.append("type_Battery="+GateWayConfig.type_Battery+"\n");
                writer.append("type_Fall="+GateWayConfig.type_Fall+"\n");
                writer.append("type_Warning="+GateWayConfig.type_Warning+"\n");
                writer.append("type_Wear="+GateWayConfig.type_Wear+"\n");
                writer.append("type_Error="+GateWayConfig.type_Error+"\n");
                writer.append("Battery_Service_UUID="+GateWayConfig.Battery_Service_UUID+"\n");
                writer.append("Battery_Level_UUID="+GateWayConfig.Battery_Level_UUID+"\n");
                writer.append("FallDetection_Service_UUID="+GateWayConfig.FallDetection_Service_UUID+"\n");
                writer.append("FallProbability_UUID="+GateWayConfig.FallProbability_UUID+"\n");
                writer.append("WarningProbability_UUID="+GateWayConfig.WarningProbability_UUID+"\n");
                writer.append("ThresholdsProbability_UUID="+GateWayConfig.ThresholdsProbability_UUID+"\n");
                writer.append("ThresholdsWear_UUID="+GateWayConfig.ThresholdsWear_UUID+"\n");
                writer.append("Status_UUID="+GateWayConfig.Status_UUID+"\n");
                writer.append("GateWay="+GateWayConfig.GateWay+"\n");
                writer.close();
            }else {
                String str;
                String[] tokens;
                BufferedReader br = new BufferedReader(new FileReader(filePointer));

                GateWayConfig.cloudAddress = br.readLine();
                str = br.readLine();
                tokens = str.split("=");
                GateWayConfig.type_Connected = Integer.parseInt(tokens[1]);
                str = br.readLine();
                tokens = str.split("=");
                GateWayConfig.type_Disconnected = Integer.parseInt(tokens[1]);
                str = br.readLine();
                tokens = str.split("=");
                GateWayConfig.type_Battery = Integer.parseInt(tokens[1]);
                str = br.readLine();
                tokens = str.split("=");
                GateWayConfig.type_Fall = Integer.parseInt(tokens[1]);
                str = br.readLine();
                tokens = str.split("=");
                GateWayConfig.type_Warning = Integer.parseInt(tokens[1]);
                str = br.readLine();
                tokens = str.split("=");
                GateWayConfig.type_Wear = Integer.parseInt(tokens[1]);
                str = br.readLine();
                tokens = str.split("=");
                GateWayConfig.type_Error = Integer.parseInt(tokens[1]);
                str = br.readLine();
                tokens = str.split("=");
                GateWayConfig.Battery_Service_UUID = UUID.fromString(tokens[1]);
                str = br.readLine();
                tokens = str.split("=");
                GateWayConfig.Battery_Level_UUID = UUID.fromString(tokens[1]);
                str = br.readLine();
                tokens = str.split("=");
                GateWayConfig.FallDetection_Service_UUID = UUID.fromString(tokens[1]);
                str = br.readLine();
                tokens = str.split("=");
                GateWayConfig.FallProbability_UUID = UUID.fromString(tokens[1]);
                str = br.readLine();
                tokens = str.split("=");
                GateWayConfig.WarningProbability_UUID = UUID.fromString(tokens[1]);
                str = br.readLine();
                tokens = str.split("=");
                GateWayConfig.ThresholdsProbability_UUID = UUID.fromString(tokens[1]);
                str = br.readLine();
                tokens = str.split("=");
                GateWayConfig.ThresholdsWear_UUID = UUID.fromString(tokens[1]);
                str = br.readLine();
                tokens = str.split("=");
                GateWayConfig.Status_UUID = UUID.fromString(tokens[1]);
                str = br.readLine();
                tokens = str.split("=");
                GateWayConfig.GateWay = tokens[1];
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : list_of_permissions) {
            result = ContextCompat.checkSelfPermission(getApplicationContext(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), MULTIPLE_PERMISSIONS);
        }
    }
}
