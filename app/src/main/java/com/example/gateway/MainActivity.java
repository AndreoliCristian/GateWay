package com.example.gateway;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gateway.adapters.BleGattProfileListAdapter;
import com.example.gateway.ble.BleCommManager;
import com.example.gateway.ble.BlePeripheral;
import com.example.gateway.ble.callbacks.BleScanCallbackv18;
import com.example.gateway.ble.callbacks.BleScanCallbackv21;
import com.example.gateway.models.BlePeripheralListItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class MainActivity extends AppCompatActivity {

    private static final UUID Battery_Service_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb"); //Servizio
    private static final UUID Battery_Level_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"); //Caratteristica

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MULTIPLE_PERMISSIONS = 42;
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int SCAN_PERIOD = 10000;
    private String[] list_of_permissions = new String[] {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    /**
     * Bluetooth	Stuff
     **/
    private BleCommManager mBleCommManager;
    private	boolean	mScanningActive	= false;

    private	MenuItem mScanProgressSpinner;
    private	MenuItem mStartScanItem, mStopScanItem;
    //private ListView mBlePeripheralsListView;
    private ExpandableListView mBlePeripheralsListView;
    private TextView mPeripheralsListEmptyTV;
    private BleGattProfileListAdapter mBlePeripheralsListAdapter;

    //mio debug
    private TextView textView;
    //

    //PROVE MIE
    //---------------------------------------------------------------------------------------------------------------------------------------

    private Map<Integer, BlePeripheral> listaSensori = new HashMap<>();
    private ArrayList<BluetoothDevice> disposit = new ArrayList<>();
    private BleGattProfileListAdapter.GroupViewHolder h;

    /*public void onBleConnected(){
        View porco = listaSensori.get(0).getV();
        h = new BleGattProfileListAdapter.GroupViewHolder();
        h.connection_Status.setText("SI");
        porco.setTag(h);
        //Log.v(TAG,"OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO   =   "+listaSensori.get(0).getConnessoStatus().toString());
        //listaSensori.get(listaSensori.get(0).getDeviceId()).getConnessoStatus().setText("SI");
        //Log.v(TAG,"OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO   =   "+listaSensori.get(0).getConnessoStatus().toString());

        //listItem = parente.getChildAt(0);
        //nuovavista = (BleGattProfileListAdapter.GroupViewHolder) listItem.getTag();
        //nuovavista.connection_Status.setText("SI");
        //listItem.setTag(nuovavista);
    }*/

    BleGattProfileListAdapter.GroupViewHolder nuovavista;

    //private boolean mBleConnected;

    /*private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.v(TAG, "Connected to peripheral");
                mBleConnected = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onBleConnected();
                    }
                });
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mBlePeripheral.close();
                mBleConnected = false;
            }


        }
    };*/

    public void prova(View view){
        updateTextView(String.valueOf(mBlePeripheralsListView.getSelectedId())+"  "+String.valueOf(mBlePeripheralsListView.getSelectedPosition()));
        Log.e(TAG,String.valueOf(mBlePeripheralsListView.getSelectedId())+"  "+String.valueOf(mBlePeripheralsListView.getSelectedPosition())+"  "+String.valueOf(mBlePeripheralsListView.getId()));
    }


    private int position;
    private long id;
    private BleGattProfileListAdapter.GroupViewHolder holder;
    private ExpandableListView parente;
    private View listItem;
    private int count;

    /*public	void	attachCallbacks()	{

        mBlePeripheralsListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                return false;
            }
        });
        //	if	a	list	item	is	clicked,
        //	open	corresponding	Peripheral	in	the	ConnectActivity
        mBlePeripheralsListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                Log.e(TAG,groupPosition+"  "+ id);
                Log.e(TAG,"LISTA MAPPA =  "+ listaSensori.get(groupPosition));
                if(listaSensori.get(groupPosition) == null){
                    BlePeripheral blePeripheral = new BlePeripheral(disposit.get(groupPosition), gattCallback, getApplicationContext());
                    blePeripheral.setDeviceId(groupPosition);
                    listaSensori.put(groupPosition,blePeripheral);
                }
                Log.e(TAG,"NUOVAVISTA =  "+listaSensori.get(groupPosition).getNuovavista());
                if(listaSensori.get(groupPosition).getNuovavista() == null){
                    View listItem;
                    listItem = parent.getChildAt(groupPosition);
                    Log.e(TAG,"TAG =  "+listItem.getTag().toString());
                    listaSensori.get(groupPosition).setNuovavista((BleGattProfileListAdapter.GroupViewHolder) listItem.getTag());
                }
                //parente = parent;
                return false;
            }
        });

    }*/


    //---------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        checkPermissions();
        inizializzaUI();

        //textView = findViewById(R.id.textView);
        //textView.setMovementMethod(new ScrollingMovementMethod());

        //attachCallbacks();
    }

    private void inizializzaUI(){
        mPeripheralsListEmptyTV = (TextView)findViewById(R.id.peripheral_list_empty);
        mBlePeripheralsListView = (ExpandableListView) findViewById(R.id.peripherals_list);
        mBlePeripheralsListAdapter = new BleGattProfileListAdapter(listaSensori);
        mBlePeripheralsListView.setAdapter(mBlePeripheralsListAdapter);
        mBlePeripheralsListView.setEmptyView(mPeripheralsListEmptyTV);
    }

    @Override
    protected void onResume() {
        super.onResume();
        inizializzaBluetooth();
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
        mStartScanItem = menu.findItem(R.id.action_start_scan);
        mStopScanItem =	 menu.findItem(R.id.action_stop_scan);
        mScanProgressSpinner = menu.findItem(R.id.scan_progress_item);
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

    public void inizializzaBluetooth() {
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
            // aggiorno la UI, non serve nel mio programma
            // onBluetoothActive();
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
        for(int i = 0;i<listaSensori.size();i++){
            if(listaSensori.get(i).isConnected()){
                listaSensori.get(i).disconnect();

            }
        }
        mBlePeripheralsListAdapter.clear();
        disposit.clear();
        listaSensori.clear();
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
        //updateTextView("SCOPETO");
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


            if(listaSensori.get(count) == null){
                BlePeripheral blePeripheral = new BlePeripheral(disposit.get(count), getApplicationContext());
                blePeripheral.setDeviceId(count);
                listaSensori.put(count,blePeripheral);
            }
            count = count + 1;

            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    updateTextView("AGGIORNA");
                    updateTextView(bluetoothDevice.getName()+"\n"+bluetoothDevice.getAddress());
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
            //updateTextView("DEVICE TROVATO "+bluetoothDevice.getAddress());
        }
        @Override
        public	void	onBatchScanResults(List<ScanResult> results){
            updateTextView("BATCH");
            for	(ScanResult result : results){
                BluetoothDevice	bluetoothDevice	=	result.getDevice();
                onBlePeripheralDiscovered(bluetoothDevice);
            }
        }
        @Override
        public void onScanFailed(int errorCode){
            updateTextView("ERRORE");
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
                    updateTextView("SCAN COMPLETATA");
                    onBleScanStopped();
                }
            });
        }
    };

    public final BleScanCallbackv18 mBleScanCallbackv18	= new BleScanCallbackv18(){
                @Override
                public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord){
                    updateTextView(bluetoothDevice.getName());
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
                        inizializzaBluetooth();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // aggiorno la UI, non serve nel mio programma
                                // onBluetoothActive();
                            }
                        });
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }

            }

        }
    };

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

    public void updateTextView(final String toThis) {
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(toThis + "\n");
                Editable editable = textView.getEditableText();
                Selection.setSelection(editable, editable.length());
            }
        });*/
    }
}
