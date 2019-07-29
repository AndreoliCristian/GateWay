package com.example.gateway.ble;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.example.gateway.DBManager;
import com.example.gateway.JsonFormat;
import com.example.gateway.R;
import com.example.gateway.adapters.BleGattProfileListAdapter;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class BlePeripheral extends Activity {

    private QueueHandler queueHandler;

    private final Drawable battery_charge_25;
    private final Drawable battery_charge_50;
    private final Drawable battery_charge_75;
    private final Drawable battery_charge_100;
    private final Drawable bt5;
    private final Drawable bt25;
    private final Drawable bt50;
    private final Drawable bt75;
    private final Drawable bt100;
    private final Drawable red_led;
    private final Drawable green_led;
    private final Drawable red_led_small;
    private final Drawable green_led_small;

    private String serial = "none";
    private String gateway = "asdfdsfs";

    private static String TAG = BlePeripheral.class.getSimpleName();
    private int deviceId;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    private boolean isConnected;
    private int fallPercentage;
    private int warningPercentage;
    private AlertDialog dialog;
    public Context context;
    DBManager db;
    public Activity a;


    BleGattProfileListAdapter.GroupViewHolder groupView;
    BleGattProfileListAdapter.ChildViewHolder childView;

    public BlePeripheral(BluetoothDevice bluetoothDevice, Context context, DBManager db, Activity a){
        isConnected = false;
        mBluetoothDevice = bluetoothDevice;
        this.context = context;
        this.db = db;
        this.a = a;
        battery_charge_25 = context.getDrawable(R.mipmap.battery_charge_25);
        battery_charge_50 = context.getDrawable(R.mipmap.battery_charge_50);
        battery_charge_75 = context.getDrawable(R.mipmap.battery_charge_75);
        battery_charge_100 = context.getDrawable(R.mipmap.battery_charge_100);
        bt5 = context.getDrawable(R.mipmap.forebt5);
        bt25 = context.getDrawable(R.mipmap.forebt25);
        bt50 = context.getDrawable(R.mipmap.forebt50);
        bt75 = context.getDrawable(R.mipmap.forebt75);
        bt100 = context.getDrawable(R.mipmap.forebt100);
        red_led = context.getDrawable(R.drawable.red_led);
        green_led = context.getDrawable(R.drawable.green_led);
        red_led_small = context.getDrawable(R.drawable.red_led_small);
        green_led_small = context.getDrawable(R.drawable.green_led_small);
    }



    public void sendJson(String serial, String gateway, int type, String value){

        Calendar now = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        String date = formatter.format(now.getTime());
        Log.e(TAG,"date = "+date);

        JsonFormat j = new JsonFormat(serial, gateway, date, type, value);
        Gson gson = new Gson();
        String json = gson.toJson(j);
        Log.e(TAG,"FORMATO = "+json);

        try {
            URL obj = new URL(GateWayConfig.cloudAddress);
            HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
            postConnection.setRequestProperty("Content-Type", "application/json; utf-8");
            postConnection.setRequestProperty("Accept", "application/json");
            postConnection.setDoOutput(true);
            postConnection.setRequestMethod("POST");
            OutputStream os = postConnection.getOutputStream();
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
            os.flush();
            os.close();
            int responseCode = postConnection.getResponseCode();
            Log.e(TAG,"POST Response Code :  " + responseCode);
            Log.e(TAG,"POST Response Message : " + postConnection.getResponseMessage());
            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        postConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in .readLine()) != null) {
                    response.append(inputLine);
                } in .close();
                Log.e(TAG,response.toString());
            } else {
                Log.e(TAG,"POST NOT WORKED");
            }

            /*URL url = new URL(cloudAddress);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(json);
            wr.flush();

            Log.e(TAG,"JSON2 ");
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            Log.e(TAG,"XXXXXXXXXXXXXXXXXXXXXXXXXXXX ");
            StringBuilder sb = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            text = sb.toString();
            Log.e(TAG,"RISPOSTA SERVER : "+text);*/
        }catch (Exception e){
            //QUALCOSA
        }
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


    private boolean txQueueProcessing = false;

    private Queue<BlePeripheral.TxQueueItem> txQueue = new LinkedList<BlePeripheral.TxQueueItem>();

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
        public BlePeripheral.TxQueueItemType type;
    }

    private void addToTxQueue(BlePeripheral.TxQueueItem txQueueItem) {

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
        BlePeripheral.TxQueueItem txQueueItem = new BlePeripheral.TxQueueItem();
        txQueueItem.characteristic = ch;
        txQueueItem.enabled = enabled;
        txQueueItem.type = BlePeripheral.TxQueueItemType.SubscribeCharacteristic;
        addToTxQueue(txQueueItem);
    }

    /* queues enables/disables notification for characteristic */
    public void queueWriteDataToCharacteristic(final BluetoothGattCharacteristic ch, final byte[] dataToWrite)
    {
        // Add to queue because shitty Android GATT stuff is only synchronous
        BlePeripheral.TxQueueItem txQueueItem = new BlePeripheral.TxQueueItem();
        txQueueItem.characteristic = ch;
        txQueueItem.dataToWrite = dataToWrite;
        txQueueItem.type = BlePeripheral.TxQueueItemType.WriteCharacteristic;
        addToTxQueue(txQueueItem);
    }

    /* request to fetch newest value stored on the remote device for particular characteristic */
    public void queueRequestCharacteristicValue(BluetoothGattCharacteristic ch) {

        // Add to queue because shitty Android GATT stuff is only synchronous
        BlePeripheral.TxQueueItem txQueueItem = new BlePeripheral.TxQueueItem();
        txQueueItem.characteristic = ch;
        txQueueItem.type = BlePeripheral.TxQueueItemType.ReadCharacteristic;
        addToTxQueue(txQueueItem);
    }

    /**
     * Call when a transaction has been completed.
     * Will process next transaction if queued
     */
    private void processTxQueue()
    {
        if (txQueue.size() <= 0)  {
            txQueueProcessing = false;
            return;
        }

        if(txQueueProcessing==false){
            txQueueProcessing = true;
            BlePeripheral.TxQueueItem txQueueItem = txQueue.remove();
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

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.v(TAG, "Connected to peripheral");
                gatt.discoverServices();
                onBleConnected();
                isConnected = true;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnect();
                isConnected = false;
                onBleDisconnected();
                close();

                //QUI POTREI AGGIUNGERE UN POSSIBILE CICLO WHILE CHE IN CASO DI DISCONNESSIONE PROVI A RICONNETTERSI ALL'INFINITO, EVENTUALE RIFLESSIONE (SUONO, LUCI, ETC.) SULLA UI
                //AD UN'EVENTUALE CONNESSIONE/DISCONNESSIONE POTREI DISABILITARE IL RELATIVO BOTTONE COSI' CHE SIA IMPOSSIBILE RIPREMERE IL BOTTONE CONNECT/DISCONNECT SE IL DISPOSITIVO E' GIA' CONNESSO/DISCONNESSO
            }


        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services) {
                Log.v(TAG, "SERVIZIO scoperto = " + service.getUuid().toString());
                if(service.getUuid().equals(GateWayConfig.Battery_Service_UUID) || service.getUuid().equals(GateWayConfig.FallDetection_Service_UUID)) {
                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        //Log.v(TAG, "CARACT scoperta = " + characteristic.getUuid().toString());
                        if (characteristic.getUuid().equals(GateWayConfig.Status_UUID)){
                            queueSetNotificationForCharacteristic(characteristic, true);
                            Log.v(TAG, "CARACT SETTATA stat = " + characteristic.getUuid().toString());
                        }
                        if (characteristic.getUuid().equals(GateWayConfig.WarningProbability_UUID)){
                            queueSetNotificationForCharacteristic(characteristic, true);
                            Log.v(TAG, "CARACT SETTATA war = " + characteristic.getUuid().toString());
                        }
                        if (characteristic.getUuid().equals(GateWayConfig.FallProbability_UUID)){
                            queueSetNotificationForCharacteristic(characteristic, true);
                            Log.v(TAG, "CARACT SETTATA fall = " + characteristic.getUuid().toString());
                        }
                        if (characteristic.getUuid().equals(GateWayConfig.Battery_Level_UUID)){
                            queueSetNotificationForCharacteristic(characteristic, true);
                            Log.v(TAG, "CARACT SETTATA batt = " + characteristic.getUuid().toString());
                        }
                        /*
                        if (characteristic.getUuid().equals(FallProbability_UUID) ||
                                characteristic.getUuid().equals(WarningProbability_UUID) ||
                                characteristic.getUuid().equals(Battery_Level_UUID) ||
                                characteristic.getUuid().equals(Status_UUID)) {
                            queueSetNotificationForCharacteristic(characteristic, true);
                            //Log.v(TAG, "CARACT scoperta = " + characteristic.getUuid().toString());
                        }*/
                    }
                }
            }
            //processTxQueue();
            mBluetoothGatt = gatt;
            //queueRequestCharacteristicValue(gatt.getService(FallDetection_Service_UUID).getCharacteristic(Status_UUID));
            //processTxQueue();
            queueRequestCharacteristicValue(gatt.getService(GateWayConfig.FallDetection_Service_UUID).getCharacteristic(GateWayConfig.Status_UUID));
            queueRequestCharacteristicValue(gatt.getService(GateWayConfig.Battery_Service_UUID).getCharacteristic(GateWayConfig.Battery_Level_UUID));
        }




        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            /*Log.v(TAG, "NUMERO CODA = " + txQueue.size());
            Log.v(TAG, "CONTROLLO CODA = " + txQueueProcessing);*/
            txQueueProcessing = false;
            processTxQueue();
            Log.e(TAG,"DESCRITTORE SCRITTO CORRETTAMENTE = "+descriptor.getUuid().toString());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.v(TAG,"CARATTERISTICA CAMBIATA");
            Log.v(TAG,"onCharacteristicChanged " + characteristic.getUuid() + " = " + GateWayConfig.Battery_Level_UUID);
            if(characteristic.getUuid().equals(GateWayConfig.Battery_Level_UUID)){
                Log.v(TAG,"PERCENTAGE");
                updateBatteryLevel(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
            }
            if(characteristic.getUuid().equals(GateWayConfig.FallProbability_UUID)){
                Log.v(TAG,"LA PERSONA FALL = ");
                byte[] data	= characteristic.getValue();
                updateFall(String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)));
            }
            if(characteristic.getUuid().equals(GateWayConfig.WarningProbability_UUID)){
                byte[] data	= characteristic.getValue();
                Log.v(TAG,"WAR = "+String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)));
                updateProbability(String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)));
            }
            if(characteristic.getUuid().equals(GateWayConfig.Status_UUID)){
                byte[] data	= characteristic.getValue();
                boolean[] bits = convertToBits(data[0]);
                updateStatus(bits);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            txQueueProcessing = false;
            processTxQueue();

            if(characteristic.getUuid().equals(GateWayConfig.Battery_Level_UUID)){
                Log.v(TAG,"BATTERIA = "+characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
                updateBatteryLevel(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
            }
            if(characteristic.getUuid().equals(GateWayConfig.Status_UUID)){
                //Log.v(TAG,"BATTERIA = "+characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
                byte[] data	= characteristic.getValue();
                boolean[] bits = convertToBits(data[0]);
                updateStatus(bits);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            txQueueProcessing = false;
            processTxQueue();
            Log.v(TAG,"PROVA = "+String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)));
        }
    };

    public static boolean[] convertToBits(byte b) {
        boolean[] bits = new boolean[8];
        for (int i = 0; i < bits.length; i++) {
            bits[7 - i] = ((b & (1 << i)) != 0);
        }
        return bits;
    }

    public BluetoothGatt connect(final Context context) throws Exception{
        if(mBluetoothDevice == null){
            throw  new  Exception("No bluetooth device provided");
        }
        if(!isConnected) {
            mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, gattCallback);
        }
        else{
            Toast.makeText(context, "Dispositivo già connesso", Toast.LENGTH_SHORT).show();
        }
        return mBluetoothGatt;
    }

    public void onBleConnected(){
        db.putEvent(serial, gateway, GateWayConfig.type_Connected, "true");
        sendJson(serial, gateway, GateWayConfig.type_Connected, "true");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupView.connection_Status.setImageDrawable(green_led_small);
                childView.statusLed.setImageDrawable(green_led);
                childView.connect.setEnabled(false);
                childView.disconnect.setEnabled(true);
                childView.sendThresholdInterest.setEnabled(true);
                childView.sendThresholdWear.setEnabled(true);
                childView.sendThresholdProbability.setEnabled(true);
                childView.options.setEnabled(true);
            }
        });
    }

    public void onBleDisconnected(){
        db.putEvent(serial, gateway, GateWayConfig.type_Disconnected, "true");
        sendJson(serial, gateway, GateWayConfig.type_Disconnected, "true");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupView.connection_Status.setImageDrawable(red_led_small);
                childView.statusLed.setImageDrawable(red_led);
                childView.connect.setEnabled(true);
                childView.disconnect.setEnabled(false);
                childView.sendThresholdInterest.setEnabled(false);
                childView.sendThresholdWear.setEnabled(true);
                childView.sendThresholdProbability.setEnabled(true);
                groupView.indossato.setImageDrawable(red_led_small);
                childView.wearLed.setImageDrawable(red_led);
                childView.options.setEnabled(false);
            }
        });
    }

    public void updateBatteryLevel(final int percentage){
        db.putEvent(serial, gateway, GateWayConfig.type_Battery, String.valueOf(percentage));
        sendJson(serial, gateway, GateWayConfig.type_Battery, String.valueOf(percentage));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(percentage<=5){
                    childView.bt.setImageDrawable(bt5);
                    groupView.battery.setImageDrawable(battery_charge_25);
                }
                else if(percentage<=25){
                    childView.bt.requestLayout();
                    childView.bt.getLayoutParams().height = 504;
                    childView.bt.getLayoutParams().width = 700;
                    childView.bt.setImageDrawable(bt25);
                    groupView.battery.setImageDrawable(battery_charge_25);
                }else if (percentage<=50){
                    childView.bt.requestLayout();
                    childView.bt.getLayoutParams().height = 504;
                    childView.bt.getLayoutParams().width = 700;
                    childView.bt.setImageDrawable(bt50);
                    groupView.battery.setImageDrawable(battery_charge_50);
                }else if (percentage<=75){
                    childView.bt.requestLayout();
                    childView.bt.getLayoutParams().height = 504;
                    childView.bt.getLayoutParams().width = 700;
                    childView.bt.setImageDrawable(bt75);
                    groupView.battery.setImageDrawable(battery_charge_75);
                }else{
                    childView.bt.setImageDrawable(bt100);
                    groupView.battery.setImageDrawable(battery_charge_100);
                }
                childView.charge.setProgress(percentage);
                //childView.battery.setText(percentage);
            }
        });
    }

    public void updateFall(final String fall){
        db.putEvent(serial, gateway, GateWayConfig.type_Fall, String.valueOf(fall));
        sendJson(serial, gateway, GateWayConfig.type_Fall, String.valueOf(fall));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupView.etichetta_fall.setText("Fall");
                groupView.fall.setText(fall);
                if(dialog != null){
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                }
                if(Integer.parseInt(fall)>fallPercentage) {
                    if (dialog != null) {
                        dialog.cancel();
                    }
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(a).setTitle("FALL DETECTED").setMessage("SensorTile = " + serial + "\n" + "MAC = " + mBluetoothDevice.getAddress() + "\n" + "PERCENTAGE = " + fall).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            fallPercentage = 0;
                        }
                    }).setIcon(android.R.drawable.ic_dialog_alert);
                    //AlertDialog dialog = alertDialog.create();
                    dialog = alertDialog.create();
                    dialog.show();
                }
                fallPercentage = Integer.parseInt(fall);
            }
        });
    }

    public void updateProbability(final String warning){
        db.putEvent(serial, gateway, GateWayConfig.type_Warning, String.valueOf(warning));
        sendJson(serial, gateway, GateWayConfig.type_Warning, String.valueOf(warning));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupView.etichetta_fall.setText("War");
                groupView.fall.setText(warning);
                if(dialog != null){
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                }
                if(Integer.parseInt(warning) > warningPercentage) {
                    if(dialog != null){
                        dialog.cancel();
                    }
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(a).setCancelable(false).setTitle("WARNING DETECTED").setMessage("SensorTile = " + serial + "\n" + "MAC = " + mBluetoothDevice.getAddress() + "\n" + "PERCENTAGE = " + warning).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            warningPercentage = 0;
                        }
                    }).setIcon(android.R.drawable.ic_dialog_alert);
                    dialog = alertDialog.create();
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
                warningPercentage = Integer.parseInt(warning);
            }
        });
    }

    public  void updateStatus(final boolean[] bits){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(bits[GateWayConfig.wearBit]){
                    db.putEvent(serial, gateway, GateWayConfig.type_Wear, String.valueOf(GateWayConfig.wearBit));
                    sendJson(serial, gateway, GateWayConfig.type_Wear, String.valueOf(GateWayConfig.wearBit));
                    groupView.indossato.setImageDrawable(green_led_small);
                    childView.wearLed.setImageDrawable(green_led);
                }else {
                    db.putEvent(serial, gateway, GateWayConfig.type_Wear, String.valueOf(GateWayConfig.wearBit));
                    sendJson(serial, gateway, GateWayConfig.type_Wear, String.valueOf(GateWayConfig.wearBit));
                    groupView.indossato.setImageDrawable(red_led_small);
                    childView.wearLed.setImageDrawable(red_led);
                }
                if(bits[GateWayConfig.errorBit]) {
                    db.putEvent(serial, gateway, GateWayConfig.type_Error, String.valueOf(GateWayConfig.wearBit));
                    sendJson(serial, gateway, GateWayConfig.type_Error, String.valueOf(GateWayConfig.wearBit));
                    Toast.makeText(context, "Malfunzionamento HardWare", Toast.LENGTH_SHORT).show();
                    //QUALCOSA CHE GESTISCA UN MALFUNZIONAMENTO HARDWARE
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            groupView.deviceName.setTextColor(Color.parseColor("#FF0000"));
                        }
                    });
                }else{
                    groupView.deviceName.setTextColor(Color.parseColor("#000000"));
                }
            }
        });
    }

    public void updateThresholdInterest(float threshold){
        if(mBluetoothGatt != null){
            BluetoothGattService service = mBluetoothGatt.getService(GateWayConfig.FallDetection_Service_UUID);
            if(service != null){
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(GateWayConfig.ThresholdsProbability_UUID);
                if(characteristic != null) {
                    int intBits =  Float.floatToIntBits(threshold);
                    byte value[] = new byte[] {(byte) (intBits >> 24), (byte) (intBits >> 16), (byte) (intBits >> 8), (byte) (intBits) };
                    int bitti = value[0] << 24 | (value[1] & 0xFF) << 16 | (value[2] & 0xFF) << 8 | (value[3] & 0xFF);
                    byte[] prova = new byte[4];
                    prova[0] = value[3];
                    prova[1] = value[2];
                    prova[2] = value[1];
                    prova[3] = value[0];
                    queueWriteDataToCharacteristic(characteristic,prova);
                }
            }
        }
    }

    public void updateThresholdProbability(int threshold){
        if(mBluetoothGatt != null){
            BluetoothGattService service = mBluetoothGatt.getService(GateWayConfig.FallDetection_Service_UUID);
            if(service != null){
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(GateWayConfig.ThresholdsProbability_UUID);
                if(characteristic != null) {
                    byte[] value = new byte[1];
                    value[0] = (byte) (threshold & 0xFF);
                    queueWriteDataToCharacteristic(characteristic,value);
                }
            }
        }
    }

    public void updateThresholdWear(float threshold){
        if(mBluetoothGatt != null){
            BluetoothGattService service = mBluetoothGatt.getService(GateWayConfig.FallDetection_Service_UUID);
            if(service != null){
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(GateWayConfig.ThresholdsWear_UUID);
                if(characteristic != null) {
                    int intBits =  Float.floatToIntBits(threshold);
                    byte value[] = new byte[] {(byte) (intBits >> 24), (byte) (intBits >> 16), (byte) (intBits >> 8), (byte) (intBits) };
                    int bitti = value[0] << 24 | (value[1] & 0xFF) << 16 | (value[2] & 0xFF) << 8 | (value[3] & 0xFF);
                    byte[] prova = new byte[4];
                    prova[0] = value[3];
                    prova[1] = value[2];
                    prova[2] = value[1];
                    prova[3] = value[0];
                    queueWriteDataToCharacteristic(characteristic,prova);
                }
            }
        }
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

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public BleGattProfileListAdapter.GroupViewHolder getGroupView() {
        return groupView;
    }

    public void setGroupView(BleGattProfileListAdapter.GroupViewHolder groupView) {
        this.groupView = groupView;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public boolean isConnected(){
        return true;
    }

    public BleGattProfileListAdapter.ChildViewHolder getChildView() {
        return childView;
    }

    public void setChildView(BleGattProfileListAdapter.ChildViewHolder childView) {
        this.childView = childView;
    }

    public void prova(){
        //queueRequestCharacteristicValue(mBluetoothGatt.getService(FallDetection_Service_UUID).getCharacteristic(Status_UUID));
        Log.v(TAG, "NUMERO CODA = " + txQueue.size());
        Log.v(TAG, "CONTROLLO CODA = " + txQueueProcessing);
        queueRequestCharacteristicValue(mBluetoothGatt.getService(GateWayConfig.Battery_Service_UUID).getCharacteristic(GateWayConfig.Battery_Level_UUID));
        Log.v(TAG, "NUMERO CODA = " + txQueue.size());
        Log.v(TAG, "CONTROLLO CODA = " + txQueueProcessing);
        //Log.v(TAG, "CERCO SERVIZIO = " + mBluetoothGatt.getService(FallProbability_UUID).getCharacteristic(FallProbability_UUID).toString());
        //processTxQueue();
    }
}
