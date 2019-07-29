package com.example.gateway.ble;

import java.util.UUID;

public class GateWayConfig {
    public static final UUID PROVA = UUID.fromString("0000b250-ee39-11e8-b568-0800200c9a66"); //Caratteristica
    public static final UUID DESCRITTORE = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); //Caratteristica

    public static final UUID Battery_Service_UUID = UUID.fromString("180f0050-ee39-11e8-b568-0800200c9a66"); //Servizio
    public static final UUID Battery_Level_UUID = UUID.fromString("2a190050-ee39-11e8-b568-0800200c9a66"); //Caratteristica

    public static final UUID FallDetection_Service_UUID = UUID.fromString("0000b250-ee39-11e8-b568-0800200c9a66"); //Servizio
    public static final UUID FallProbability_UUID = UUID.fromString("2100b250-ee39-11e8-b568-0800200c9a66"); //Caratteristica
    public static final UUID WarningProbability_UUID = UUID.fromString("4100b250-ee39-11e8-b568-0800200c9a66"); //Caratteristica
    public static final UUID PosturalMonitor_UUID = UUID.fromString("c100b250-ee39-11e8-b568-0800200c9a66"); //Caratteristica
    public static final UUID ThresholdsProbability_UUID = UUID.fromString("e100b250-ee39-11e8-b568-0800200c9a66"); //Caratteristica

    public static final UUID ThresholdsInterest_UUID = UUID.fromString("a100b250-ee39-11e8-b568-0800200c9a66"); //Caratteristica
    public static final UUID ThresholdsWear_UUID = UUID.fromString("8100b250-ee39-11e8-b568-0800200c9a66"); //Caratteristica

    public static final UUID Status_UUID = UUID.fromString("6100b250-ee39-11e8-b568-0800200c9a66"); //Caratteristica
    public final static int errorBit = 5; //Bit numero N della caratteristica status che indica un malfunzionamento hardware
    public final static int wearBit = 7; //Bit numero N della caratteristica status che indica se il sensore è indossato o meno
    public final static int offlineRecordingBit = 7; //Bit numero N della caratteristica status che indica se la SensorTile debba o meno registrare sulla SD
    //private final static int streamRecordingBit = 3; //Bit numero N della caratteristica status che indica se lo streaming dei dati è attivo

    public final static String cloudAddress = "https://68133ey3s8.execute-api.eu-west-1.amazonaws.com/Prod";
    //Tipi di dati da inviare al Cloud
    public final static int type_Connected = 0;
    public final static int type_Disconnected = 1;
    public final static int type_Battery = 2;
    public final static int type_Fall = 3;
    public final static int type_Warning = 4;
    public final static int type_Wear = 5;
    public final static int type_Error = 6;
}
