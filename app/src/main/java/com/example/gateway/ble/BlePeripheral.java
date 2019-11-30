package com.example.gateway.ble;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.example.gateway.DBManager;
import com.example.gateway.JsonFormat;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class BlePeripheral extends Activity {

    public QueueHandler queueHandler;
    public PeripheralUIHandler peripheralUIHandler;


    public String SensorTile = "none";
    //public String gateway = "00001234";

    private static String TAG = BlePeripheral.class.getSimpleName();
    private int deviceId;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    private boolean isConnected;
    public Context context;
    public DBManager db;
    public Activity a;

    public float defaultInterest = 350;
    public int defaultProbability = 5;

    public BlePeripheral(BluetoothDevice bluetoothDevice, Context context, DBManager db, Activity a){
        isConnected = false;
        mBluetoothDevice = bluetoothDevice;
        SensorTile = mBluetoothDevice.getAddress();
        this.context = context;
        this.db = db;
        this.a = a;
        peripheralUIHandler = new PeripheralUIHandler(context,a, SensorTile,mBluetoothDevice.getAddress());
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

            /*URL url = new URL(GateWayConfig.cloudAddress);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(json);
            wr.flush();

            BufferedReader reader;
            String text;

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

            /*URL obj = new URL(GateWayConfig.cloudAddress);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");
            con.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(json);
            wr.flush();


            //add request header
            //con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + GateWayConfig.cloudAddress);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());*/
        }catch (Exception e){
            //QUALCOSA
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
            SensorTile = mBluetoothDevice.getAddress();
            mBluetoothGatt = gatt;
            queueHandler = new QueueHandler(mBluetoothGatt);
            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services) {
                Log.v(TAG, "SERVIZIO scoperto = " + service.getUuid().toString());
                if(service.getUuid().equals(GateWayConfig.Battery_Service_UUID) || service.getUuid().equals(GateWayConfig.FallDetection_Service_UUID)) {
                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        //Log.v(TAG, "CARACT scoperta = " + characteristic.getUuid().toString());
                        if (characteristic.getUuid().equals(GateWayConfig.Status_UUID)){
                            queueHandler.queueSetNotificationForCharacteristic(characteristic, true);
                            Log.v(TAG, "CARACT SETTATA stat = " + characteristic.getUuid().toString());
                        }
                        if (characteristic.getUuid().equals(GateWayConfig.WarningProbability_UUID)){
                            queueHandler.queueSetNotificationForCharacteristic(characteristic, true);
                            Log.v(TAG, "CARACT SETTATA war = " + characteristic.getUuid().toString());
                        }
                        if (characteristic.getUuid().equals(GateWayConfig.FallProbability_UUID)){
                            queueHandler.queueSetNotificationForCharacteristic(characteristic, true);
                            Log.v(TAG, "CARACT SETTATA fall = " + characteristic.getUuid().toString());
                        }
                        if (characteristic.getUuid().equals(GateWayConfig.Battery_Level_UUID)){
                            queueHandler.queueSetNotificationForCharacteristic(characteristic, true);
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
            //queueRequestCharacteristicValue(gatt.getService(FallDetection_Service_UUID).getCharacteristic(Status_UUID));
            //processTxQueue();
            queueHandler.queueRequestCharacteristicValue(gatt.getService(GateWayConfig.FallDetection_Service_UUID).getCharacteristic(GateWayConfig.Status_UUID));
            queueHandler.queueRequestCharacteristicValue(gatt.getService(GateWayConfig.Battery_Service_UUID).getCharacteristic(GateWayConfig.Battery_Level_UUID));
            updateThresholdProbability(100);
            //updateThresholdInterest(defaultInterest);
            //updateThresholdProbability(defaultProbability);
        }




        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            /*Log.v(TAG, "NUMERO CODA = " + txQueue.size());
            Log.v(TAG, "CONTROLLO CODA = " + txQueueProcessing);*/
            queueHandler.txQueueProcessing = false;
            queueHandler.processTxQueue();
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
            queueHandler.txQueueProcessing = false;
            queueHandler.processTxQueue();

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
            queueHandler.txQueueProcessing = false;
            queueHandler.processTxQueue();
            //Log.v(TAG,"PROVA = "+ByteBuffer.wrap(characteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN).getFloat());
            //Log.v(TAG,"PROVA = "+String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)));
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
        db.putEvent(SensorTile, GateWayConfig.GateWay, GateWayConfig.type_Connected, "true");
        sendJson(SensorTile, GateWayConfig.GateWay, GateWayConfig.type_Connected, "true");
        peripheralUIHandler.onBleConnected();
    }

    public void onBleDisconnected(){
        db.putEvent(SensorTile, GateWayConfig.GateWay, GateWayConfig.type_Disconnected, "true");
        sendJson(SensorTile, GateWayConfig.GateWay, GateWayConfig.type_Disconnected, "true");
        peripheralUIHandler.onBleDisconnected();
    }

    public void updateBatteryLevel(final int percentage){
        db.putEvent(SensorTile, GateWayConfig.GateWay, GateWayConfig.type_Battery, String.valueOf(percentage));
        sendJson(SensorTile, GateWayConfig.GateWay, GateWayConfig.type_Battery, String.valueOf(percentage));
        peripheralUIHandler.updateBatteryLevel(percentage);
    }

    public void updateFall(final String fall){
        db.putEvent(SensorTile, GateWayConfig.GateWay, GateWayConfig.type_Fall, String.valueOf(fall));
        sendJson(SensorTile, GateWayConfig.GateWay, GateWayConfig.type_Fall, String.valueOf(fall));
        peripheralUIHandler.updateFall(fall);
    }

    public void updateProbability(final String warning){
        db.putEvent(SensorTile, GateWayConfig.GateWay, GateWayConfig.type_Warning, String.valueOf(warning));
        sendJson(SensorTile, GateWayConfig.GateWay, GateWayConfig.type_Warning, String.valueOf(warning));
        peripheralUIHandler.updateProbability(warning);
    }

    public  void updateStatus(final boolean[] bits){

        db.putEvent(SensorTile, GateWayConfig.GateWay, GateWayConfig.type_Wear, String.valueOf(bits[GateWayConfig.wearBit]));
        sendJson(SensorTile, GateWayConfig.GateWay, GateWayConfig.type_Wear, String.valueOf(bits[GateWayConfig.wearBit]));
        if(bits[GateWayConfig.errorBit]) {
            db.putEvent(SensorTile, GateWayConfig.GateWay, GateWayConfig.type_Error, String.valueOf(bits[GateWayConfig.errorBit]));
            sendJson(SensorTile, GateWayConfig.GateWay, GateWayConfig.type_Error, String.valueOf(bits[GateWayConfig.errorBit]));
        }
        peripheralUIHandler.updateStatus(bits);
    }

    public void updateThresholdInterest(float threshold){
        if(mBluetoothGatt != null){
            BluetoothGattService service = mBluetoothGatt.getService(GateWayConfig.FallDetection_Service_UUID);
            if(service != null){
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(GateWayConfig.ThresholdsProbability_UUID);
                if(characteristic != null) {
                    /*int intBits =  Float.floatToIntBits(threshold);
                    byte value[] = new byte[] {(byte) (intBits >> 24), (byte) (intBits >> 16), (byte) (intBits >> 8), (byte) (intBits) };
                    int bitti = value[0] << 24 | (value[1] & 0xFF) << 16 | (value[2] & 0xFF) << 8 | (value[3] & 0xFF);*/
                    byte[] value = ByteBuffer.allocate(4).putFloat(threshold).array();
                    byte[] prova = new byte[4];
                    prova[0] = value[3];
                    prova[1] = value[2];
                    prova[2] = value[1];
                    prova[3] = value[0];
                    ByteBuffer.allocate(4).putFloat(threshold).array();
                    queueHandler.queueWriteDataToCharacteristic(characteristic,prova);
                }
            }
        }
    }

    public void updateThresholdProbability(int threshold){
        Log.e(TAG,"FFFFF "+threshold);
        if(mBluetoothGatt != null){
            Log.e(TAG,"FFFFF1 "+threshold);
            BluetoothGattService service = mBluetoothGatt.getService(GateWayConfig.FallDetection_Service_UUID);
            if(service != null){
                Log.e(TAG,"FFFFF2 "+threshold);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(GateWayConfig.ThresholdsProbability_UUID);
                if(characteristic != null) {
                    Log.e(TAG,"FFFFF3 "+threshold);
                    byte[] value = new byte[1];
                    value[0] = (byte) (threshold & 0xFF);
                    int i = value[0] & 0xFF;
                    Log.e(TAG,"FFFFF4 "+i+"___"+value[0]+"___"+value.length);
                    queueHandler.queueWriteDataToCharacteristic(characteristic,value);
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
                    /*int intBits =  Float.floatToIntBits(threshold);
                    byte value[] = new byte[] {(byte) (intBits >> 24), (byte) (intBits >> 16), (byte) (intBits >> 8), (byte) (intBits) };
                    int bitti = value[0] << 24 | (value[1] & 0xFF) << 16 | (value[2] & 0xFF) << 8 | (value[3] & 0xFF);*/
                    byte[] value = ByteBuffer.allocate(4).putFloat(threshold).array();
                    byte[] prova = new byte[4];
                    prova[0] = value[3];
                    prova[1] = value[2];
                    prova[2] = value[1];
                    prova[3] = value[0];
                    ByteBuffer.allocate(4).putFloat(threshold).array();
                    queueHandler.queueWriteDataToCharacteristic(characteristic,prova);
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

    public void setSensorTile(String sensorTile) {
        this.SensorTile = sensorTile;
    }

    public boolean isConnected(){
        return true;
    }

    public void prova(){
        //queueRequestCharacteristicValue(mBluetoothGatt.getService(FallDetection_Service_UUID).getCharacteristic(Status_UUID));
        //Log.v(TAG, "NUMERO CODA = " + txQueue.size());
        //Log.v(TAG, "CONTROLLO CODA = " + txQueueProcessing);
        //queueHandler.queueRequestCharacteristicValue(mBluetoothGatt.getService(GateWayConfig.Battery_Service_UUID).getCharacteristic(GateWayConfig.Battery_Level_UUID));
        //Log.v(TAG, "NUMERO CODA = " + txQueue.size());
        //Log.v(TAG, "CONTROLLO CODA = " + txQueueProcessing);
        //Log.v(TAG, "CERCO SERVIZIO = " + mBluetoothGatt.getService(FallProbability_UUID).getCharacteristic(FallProbability_UUID).toString());
        queueHandler.processTxQueue();
        Log.v(TAG, "NUMERO CODA = ");
    }
}
