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
import com.example.gateway.GestioneDB;
import com.example.gateway.R;
import com.example.gateway.adapters.BleGattProfileListAdapter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class BlePeripheral extends Activity {

    private static final UUID Battery_Service_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb"); //Servizio
    private static final UUID Battery_Level_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"); //Caratteristica

    private static final UUID FallDetection_Service_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000"); //Servizio
    private static final UUID FallProbability_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000"); //Caratteristica
    private static final UUID WarningProbability_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000"); //Caratteristica
    private static final UUID PosturalMonitor_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000"); //Caratteristica
    private static final UUID Thresholds_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000"); //Caratteristica
    private static final UUID Status_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000"); //Caratteristica
    private final static int errorBit = 1; //Bit numero N della caratteristica status che indica un malfunzionamento hardware
    private final static int wearBit = 0; //Bit numero N della caratteristica status che indica se il sensore è indossato o meno
    private final static int offlineRecordingBit = 2; //Bit numero N della caratteristica status che indica se la SensorTile debba o meno registrare sulla SD
    private final static int streamRecordingBit = 3; //Bit numero N della caratteristica status che indica se lo streaming dei dati è attivo

    //Tipi di dati da inviare al Cloud
    private final static int type_Connected = 0;
    private final static int type_Disconnected = 0;
    private final static int type_Battery = 0;
    private final static int type_Fall = 0;
    private final static int type_Warning = 0;
    private final static int type_Wear = 0;



    private final Drawable battery_charge_25;
    private final Drawable battery_charge_50;
    private final Drawable battery_charge_75;
    private final Drawable battery_charge_100;
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
    private boolean offRec = false;
    private boolean strRec = false;
    private AlertDialog.Builder alertDialog;
    private AlertDialog dialog;
    public Context context;
    GestioneDB db;
    Activity a;


    BleGattProfileListAdapter.GroupViewHolder vistaPadre;
    BleGattProfileListAdapter.ChildViewHolder vistaFiglia;

    public BlePeripheral(BluetoothDevice bluetoothDevice, Context context, GestioneDB db, Activity a){
        isConnected = false;
        mBluetoothDevice = bluetoothDevice;
        this.context = context;
        this.db = db;
        this.a = a;
        battery_charge_25 = context.getDrawable(R.mipmap.battery_charge_25);
        battery_charge_50 = context.getDrawable(R.mipmap.battery_charge_50);
        battery_charge_75 = context.getDrawable(R.mipmap.battery_charge_75);
        battery_charge_100 = context.getDrawable(R.mipmap.battery_charge_100);
        red_led = context.getDrawable(R.drawable.red_led);
        green_led = context.getDrawable(R.drawable.green_led);
        red_led_small = context.getDrawable(R.drawable.red_led_small);
        green_led_small = context.getDrawable(R.drawable.green_led_small);
    }

    public void sendJson(String serial, String gateway, int type, String value){
        String cloudAddress = "https://68133ey3s8.execute-api.eu-west-1.amazonaws.com/Prod";

        Calendar now = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
        String date = formatter.format(now.getTime());

        String text = "";
        BufferedReader reader=null;

        String json = "{ \"serial\"\":"+serial+"\", \"gateway\" :\""+gateway+"\", \"date\":\""+date+"\", \"type\":"+type+", \"value\" :"+value+ "}";
        Log.e(TAG,json);
        try {
            URL url = new URL(cloudAddress);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(json);
            wr.flush();

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            text = sb.toString();
            Log.e(TAG,"RISPOSTA SERVER : "+text);
        }catch (Exception e){
            //QUALCOSA
        }
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.v(TAG, "Connected to peripheral");
                isConnected = true;
                onBleConnected();
                gatt.discoverServices();
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
            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for(BluetoothGattCharacteristic characteristic : characteristics){
                    List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
                    for (BluetoothGattDescriptor descriptor : descriptors/*characteristicData.getDescriptors()  MOLTO PIù COMPATTO SCRIVERE COSì*/) {
                        descriptor.setValue( BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                    gatt.setCharacteristicNotification(characteristic, true);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.v(TAG,"onCharacteristicChanged " + characteristic.getUuid() + " = " + Battery_Level_UUID);
            if(characteristic.getUuid().equals(Battery_Level_UUID)){
                Log.v(TAG,"PERCENTAGE");
                updateBatteryLevel(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
            }
            if(characteristic.getUuid().equals(FallProbability_UUID)){
                byte[] data	= characteristic.getValue();
                updateFall(String.valueOf(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat()));
            }
            if(characteristic.getUuid().equals(WarningProbability_UUID)){
                byte[] data	= characteristic.getValue();
                updateProbability(String.valueOf(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat()));
            }
            if(characteristic.getUuid().equals(Status_UUID)){
                byte[] data	= characteristic.getValue();
                boolean[] bits = convertToBits(data[0]);
                updateWear(bits[wearBit]);
                if(bits[errorBit] == true){
                    Toast.makeText(context, "Malfunzionamento HardWare", Toast.LENGTH_SHORT).show();
                    //QUALCOSA CHE GESTISCA UN MALFUNZIONAMENTO HARDWARE
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            vistaPadre.deviceName.setTextColor(Color.parseColor("#FF0000"));
                        }
                    });
                }else{
                    vistaPadre.deviceName.setTextColor(Color.parseColor("#000000"));
                }

            }
            if(characteristic.getUuid().equals(PosturalMonitor_UUID)){

            }
            if(characteristic.getUuid().equals(Thresholds_UUID)){

            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(characteristic.toString().equals(Status_UUID)){
                byte[] data	= characteristic.getValue();
                boolean[] bits = convertToBits(data[0]);
                if(bits[offlineRecordingBit]){
                    offRec = true;
                    Toast.makeText(context, "OfflineRecording ON", Toast.LENGTH_SHORT).show();
                }else{
                    offRec = false;
                    Toast.makeText(context, "OfflineRecording OFF", Toast.LENGTH_SHORT).show();
                }
                if(bits[streamRecordingBit]){
                    strRec = true;
                    Toast.makeText(context, "StreamRecording ON", Toast.LENGTH_SHORT).show();
                }else{
                    strRec = false;
                    Toast.makeText(context, "StreamRecording OFF", Toast.LENGTH_SHORT).show();
                }
            }
            Log.v(TAG, "LA CARATTERISTICA E' STATA SCRITTA CORRETTAMENTE "+ characteristic);
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
        db.putEvent(serial, gateway, type_Connected, "true");
        sendJson(serial, gateway, type_Connected, "true");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                vistaPadre.connection_Status.setImageDrawable(green_led_small);
                vistaFiglia.statusLed.setImageDrawable(green_led);
                vistaFiglia.connect.setEnabled(false);
                vistaFiglia.disconnect.setEnabled(true);
                vistaFiglia.sendThreshold.setEnabled(true);
                vistaFiglia.offlineRecording.setEnabled(true);
                vistaFiglia.streamRecording.setEnabled(true);
            }
        });
    }

    public void onBleDisconnected(){
        db.putEvent(serial, gateway, type_Disconnected, "true");
        sendJson(serial, gateway, type_Disconnected, "true");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                vistaPadre.connection_Status.setImageDrawable(red_led_small);
                vistaFiglia.statusLed.setImageDrawable(red_led);
                vistaFiglia.disconnect.setEnabled(false);
                vistaFiglia.connect.setEnabled(true);
                vistaFiglia.sendThreshold.setEnabled(false);
                vistaFiglia.offlineRecording.setEnabled(false);
                vistaFiglia.streamRecording.setEnabled(false);
                offRec = false;
                strRec = false;
            }
        });
    }

    public void updateBatteryLevel(final int percentage){
        db.putEvent(serial, gateway, type_Battery, String.valueOf(percentage));
        sendJson(serial, gateway, type_Battery, String.valueOf(percentage));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(percentage<=25){
                    vistaPadre.battery.setImageDrawable(battery_charge_25);
                }else if (percentage<=50){
                    vistaPadre.battery.setImageDrawable(battery_charge_50);
                }else if (percentage<=75){
                    vistaPadre.battery.setImageDrawable(battery_charge_75);
                }else{
                    vistaPadre.battery.setImageDrawable(battery_charge_100);
                }
                vistaFiglia.charge.setProgress(percentage);
            }
        });
    }

    public void updateFall(final String fall){
        db.putEvent(serial, gateway, type_Fall, String.valueOf(fall));
        sendJson(serial, gateway, type_Fall, String.valueOf(fall));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                vistaPadre.etichetta_fall.setText("Fall");
                vistaPadre.fall.setText(fall);
                //vistaPadre.deviceName.setTextColor(Color.parseColor("#FF0000"));
                if(Integer.parseInt(fall)>fallPercentage) {
                    dialog.cancel();
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(a).setTitle("FALL DETECTED").setMessage("SensorTile = " + serial + "\n" + "MAC = " + mBluetoothDevice.getAddress() + "\n" + "PERCENTAGE = " + fall).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            fallPercentage = 0;
                        }
                    }).setIcon(android.R.drawable.ic_dialog_alert);
                    AlertDialog dialog = alertDialog.create();
                    dialog.show();
                }
                fallPercentage = Integer.parseInt(fall);
            }
        });
    }

    public void updateProbability(final String warning){
        db.putEvent(serial, gateway, type_Warning, String.valueOf(warning));
        sendJson(serial, gateway, type_Warning, String.valueOf(warning));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                vistaPadre.etichetta_fall.setText("War");
                vistaPadre.fall.setText(warning);
                //vistaPadre.deviceName.setTextColor(Color.parseColor("#FF0000"));
                if(Integer.parseInt(warning) > warningPercentage) {
                    dialog.cancel();
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(a).setTitle("WARNING DETECTED").setMessage("SensorTile = " + serial + "\n" + "MAC = " + mBluetoothDevice.getAddress() + "\n" + "PERCENTAGE = " + warning).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            warningPercentage = 0;
                        }
                    }).setIcon(android.R.drawable.ic_dialog_alert);
                    AlertDialog dialog = alertDialog.create();
                    dialog.show();
                }
                warningPercentage = Integer.parseInt(warning);
            }
        });
    }

    public void updateWear(final boolean bol){
        db.putEvent(serial, gateway, type_Wear, String.valueOf(bol));
        sendJson(serial, gateway, type_Wear, String.valueOf(bol));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(bol){
                    vistaPadre.indossato.setImageDrawable(green_led);
                    vistaFiglia.wearLed.setImageDrawable(green_led);
                }else {
                    vistaPadre.indossato.setImageDrawable(red_led);
                    vistaFiglia.wearLed.setImageDrawable(red_led);
                }
            }
        });
    }

    public void updateThreshold(int threshold){
        if(mBluetoothGatt != null){
            BluetoothGattService service = mBluetoothGatt.getService(FallDetection_Service_UUID);
            if(service != null){
                BluetoothGattCharacteristic charac = service.getCharacteristic(Thresholds_UUID);
                if(charac != null) {
                    byte[] value = new byte[1];
                    value[0] = (byte) (threshold & 0xFF);
                    charac.setValue(value);
                    mBluetoothGatt.writeCharacteristic(charac);
                    Log.v(TAG, "TH   =   " + value);
                }
            }
        }
    }

    public void startOfflineRecording(){
        if(mBluetoothGatt != null) {
            BluetoothGattService service = mBluetoothGatt.getService(FallDetection_Service_UUID);
            if(service != null) {
                BluetoothGattCharacteristic charac = service.getCharacteristic(Status_UUID);
                if (charac != null) {
                    int i = charac.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    byte[] value = new byte[1];
                    int bit = 1 << offlineRecordingBit;
                    byte b= (byte) (i ^ bit);
                    value[0] = (byte) (b);
                    charac.setValue(value);
                    mBluetoothGatt.writeCharacteristic(charac);
                }
            }
        }
    }

    public void startStreamRecording(){
        if(mBluetoothGatt != null) {
            BluetoothGattService service = mBluetoothGatt.getService(FallDetection_Service_UUID);
            if(service != null) {
                BluetoothGattCharacteristic charac = service.getCharacteristic(Status_UUID);
                if (charac != null) {
                    int i = charac.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    byte[] value = new byte[1];
                    int bit = 1 << streamRecordingBit;
                    byte b= (byte) (i ^ bit);
                    value[0] = (byte) (b);
                    charac.setValue(value);
                    mBluetoothGatt.writeCharacteristic(charac);
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

    public BluetoothDevice getmBluetoothDevice(){
        return mBluetoothDevice;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public BleGattProfileListAdapter.GroupViewHolder getVistaPadre() {
        return vistaPadre;
    }

    public void setVistaPadre(BleGattProfileListAdapter.GroupViewHolder vistaPadre) {
        this.vistaPadre = vistaPadre;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public BleGattProfileListAdapter.ChildViewHolder getVistaFiglia() {
        return vistaFiglia;
    }

    public void setVistaFiglia(BleGattProfileListAdapter.ChildViewHolder vistaFiglia) {
        this.vistaFiglia = vistaFiglia;
    }
}
