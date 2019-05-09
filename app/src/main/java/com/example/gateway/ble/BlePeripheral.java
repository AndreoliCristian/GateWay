package com.example.gateway.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gateway.adapters.BleGattProfileListAdapter;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class BlePeripheral {
    private static String TAG = BlePeripheral.class.getSimpleName();
    private int deviceId;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    private boolean isConnected;
    public Context context;


    BleGattProfileListAdapter.GroupViewHolder nuovavista;

    public BlePeripheral(BluetoothDevice bluetoothDevice, Context context){
        isConnected = false;
        mBluetoothDevice = bluetoothDevice;
        this.context = context;
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.v(TAG, "Connected to peripheral");
                isConnected = true;
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onBleConnected();
                    }
                });*/
                onBleConnected();
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnect();
                isConnected = false;
                nuovavista.connection_Status.setText("NO");
                close();

                //QUI POTREI AGGIUNGERE UN POSSIBILE CICLO WHILE CHE IN CASO DI DISCONNESSIONE PROVI A RICONNETTERSI ALL'INFINITO, EVENTUALE RIFLESSIONE (SUONO, LUCI, ETC.) SULLA UI
                //AD UN'EVENTUALE CONNESSIONE/DISCONNESSIONE POTREI DISABILITARE IL RELATIVO BOTTONE COSI' CHE SIA IMPOSSIBILE RIPREMERE IL BOTTONE CONNECT/DISCONNECT SE IL DISPOSITIVO E' GIA' CONNESSO/DISCONNESSO
            }


        }
    };

    public BluetoothGatt connect(/*BluetoothDevice bluetoothDevice, *//*BluetoothGattCallback callback,*/ final Context context) throws Exception{
        if(mBluetoothDevice == null){
            throw  new  Exception("No bluetooth device provided");
        }
        if(!isConnected) {
            //mBluetoothDevice = bluetoothDevice;
            mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, gattCallback);
        }
        else{
            Toast.makeText(context, "Dispositivo già connesso", Toast.LENGTH_SHORT).show();
        }
        return mBluetoothGatt;
    }

    public void onBleConnected(){
        nuovavista.connection_Status.setText("SI");
        //listItem.setTag(nuovavista);
    }

    public void disconnect(){
        if(mBluetoothGatt != null){
            mBluetoothGatt.disconnect();
        }
    }

    public void close(){
        if(mBluetoothGatt != null){
            mBluetoothGatt.close();	// close connection to Peripheral
            mBluetoothGatt = null; // release from memory
        }
    }

    public BluetoothDevice getmBluetoothDevice(){
        return mBluetoothDevice;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public BleGattProfileListAdapter.GroupViewHolder getNuovavista() {
        return nuovavista;
    }

    public void setNuovavista(BleGattProfileListAdapter.GroupViewHolder nuovavista) {
        this.nuovavista = nuovavista;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
