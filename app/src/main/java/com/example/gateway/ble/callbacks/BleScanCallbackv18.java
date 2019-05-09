package com.example.gateway.ble.callbacks;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.support.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public abstract class BleScanCallbackv18 implements BluetoothAdapter.LeScanCallback {
    @Override
    public abstract void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord);

    public abstract void onScanComplete();
}
