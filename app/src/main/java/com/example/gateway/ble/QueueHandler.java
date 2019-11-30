package com.example.gateway.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class QueueHandler {
    public BluetoothGatt mBluetoothGatt;

    public QueueHandler(BluetoothGatt mBluetoothGatt) {
        this.mBluetoothGatt = mBluetoothGatt;
    }

    public void requestCharacteristicValue(BluetoothGattCharacteristic ch) {
        if (mBluetoothGatt == null) return;

        mBluetoothGatt.readCharacteristic(ch);
        // new value available will be notified in Callback Object
    }

    /* set new value for particular characteristic */
    public void writeDataToCharacteristic(final BluetoothGattCharacteristic ch, final byte[] dataToWrite)
    {
        if (mBluetoothGatt == null || ch == null) return;

        // first set it locally....
        ch.setValue(dataToWrite);
        // ... and then "commit" changes to the peripheral
        mBluetoothGatt.writeCharacteristic(ch);
    }

    /* enables/disables notification for characteristic */
    public void setNotificationForCharacteristic(BluetoothGattCharacteristic ch, boolean enabled)
    {
        if (mBluetoothGatt == null) return;

        boolean success = mBluetoothGatt.setCharacteristicNotification(ch, enabled);
        if(!success) {
            Log.e("------", "Seting proper notification status for characteristic failed!");
        }

        // This is also sometimes required (e.g. for heart rate monitors) to enable notifications/indications
        // see: https://developer.bluetooth.org/gatt/descriptors/Pages/DescriptorViewer.aspx?u=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
        BluetoothGattDescriptor descriptor = ch.getDescriptor(GateWayConfig.DESCRITTORE);
        if(descriptor != null) {
            byte[] val = enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            descriptor.setValue(val);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }


    public boolean txQueueProcessing = false;

    private Queue<TxQueueItem> txQueue = new LinkedList<TxQueueItem>();

    private enum TxQueueItemType {
        SubscribeCharacteristic,
        ReadCharacteristic,
        WriteCharacteristic
    }

    private class TxQueueItem
    {
        BluetoothGattCharacteristic characteristic;
        byte[] dataToWrite; // Only used for characteristic write
        boolean enabled; // Only used for characteristic notification subscription
        public TxQueueItemType type;
    }

    private void addToTxQueue(TxQueueItem txQueueItem) {

        txQueue.add(txQueueItem);

        // If there is no other transmission processing, go do this one!
        if (!txQueueProcessing) {
            processTxQueue();
        }
    }

    /* queues enables/disables notification for characteristic */
    public void queueSetNotificationForCharacteristic(BluetoothGattCharacteristic ch, boolean enabled)
    {
        // Add to queue because shitty Android GATT stuff is only synchronous
        TxQueueItem txQueueItem = new TxQueueItem();
        txQueueItem.characteristic = ch;
        txQueueItem.enabled = enabled;
        txQueueItem.type = TxQueueItemType.SubscribeCharacteristic;
        addToTxQueue(txQueueItem);
    }

    /* queues enables/disables notification for characteristic */
    public void queueWriteDataToCharacteristic(final BluetoothGattCharacteristic ch, final byte[] dataToWrite)
    {
        // Add to queue because shitty Android GATT stuff is only synchronous
        TxQueueItem txQueueItem = new TxQueueItem();
        txQueueItem.characteristic = ch;
        txQueueItem.dataToWrite = dataToWrite;
        txQueueItem.type = TxQueueItemType.WriteCharacteristic;
        addToTxQueue(txQueueItem);
    }

    /* request to fetch newest value stored on the remote device for particular characteristic */
    public void queueRequestCharacteristicValue(BluetoothGattCharacteristic ch) {

        // Add to queue because shitty Android GATT stuff is only synchronous
        TxQueueItem txQueueItem = new TxQueueItem();
        txQueueItem.characteristic = ch;
        txQueueItem.type = TxQueueItemType.ReadCharacteristic;
        addToTxQueue(txQueueItem);
    }

    /**
     * Call when a transaction has been completed.
     * Will process next transaction if queued
     */
    public void processTxQueue()
    {
        if (txQueue.size() <= 0)  {
            txQueueProcessing = false;
            return;
        }

        if(txQueueProcessing==false){
            txQueueProcessing = true;
            TxQueueItem txQueueItem = txQueue.remove();
            switch (txQueueItem.type) {
                case WriteCharacteristic:
                    writeDataToCharacteristic(txQueueItem.characteristic, txQueueItem.dataToWrite);
                    break;
                case SubscribeCharacteristic:
                    setNotificationForCharacteristic(txQueueItem.characteristic, txQueueItem.enabled);
                    break;
                case ReadCharacteristic:
                    requestCharacteristicValue(txQueueItem.characteristic);
            }
        }


    }
}
