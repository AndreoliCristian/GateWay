package com.example.gateway.models;

import android.bluetooth.BluetoothDevice;
import android.view.View;

public class BlePeripheralListItem {
    private int ItemId;
    private BluetoothDevice mBluetoothDevice;

    public BlePeripheralListItem(BluetoothDevice mBluetoothDevice) {
        this.mBluetoothDevice = mBluetoothDevice;
    }

    public BluetoothDevice getDevice() {
        return mBluetoothDevice;
    }

    public int getItemId() {
        return ItemId;
    }

    public void setItemId(int itemId) {
        ItemId = itemId;
    }
    public String getDeviceName(){
        return mBluetoothDevice.getName();
    }
    public String getDeviceMac(){
        return mBluetoothDevice.getAddress();
    }







}
