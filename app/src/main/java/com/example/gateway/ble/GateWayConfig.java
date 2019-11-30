package com.example.gateway.ble;

import java.util.UUID;

public class GateWayConfig {
    public static String GateWay = "00001234";

    public static final UUID DESCRITTORE = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); //Caratteristica

    public static UUID Battery_Service_UUID = UUID.fromString("180f0050-ee39-11e8-b568-0800200c9a66"); //Servizio
    public static UUID Battery_Level_UUID = UUID.fromString("2a190050-ee39-11e8-b568-0800200c9a66"); //Caratteristica

    public static UUID FallDetection_Service_UUID = UUID.fromString("0000b250-ee39-11e8-b568-0800200c9a66"); //Servizio
    public static UUID FallProbability_UUID = UUID.fromString("2100b250-ee39-11e8-b568-0800200c9a66"); //Caratteristica
    public static UUID WarningProbability_UUID = UUID.fromString("4100b250-ee39-11e8-b568-0800200c9a66"); //Caratteristica
    public static UUID ThresholdsProbability_UUID = UUID.fromString("e100b250-ee39-11e8-b568-0800200c9a66"); //Caratteristica

    public static UUID ThresholdsWear_UUID = UUID.fromString("8100b250-ee39-11e8-b568-0800200c9a66"); //Caratteristica

    public static UUID Status_UUID = UUID.fromString("6100b250-ee39-11e8-b568-0800200c9a66"); //Caratteristica
    public final static int errorBit = 5; //Bit numero N della caratteristica status che indica un malfunzionamento hardware
    public final static int wearBit = 7; //Bit numero N della caratteristica status che indica se il sensore è indossato o meno

    public static String cloudAddress = "http://ec2-34-249-10-102.eu-west-1.compute.amazonaws.com:100/Message/add";
    //Tipi di dati da inviare al Cloud
    public static int type_Connected = 5;
    public static int type_Disconnected = 6;
    public static int type_Battery = 3;
    public static int type_Fall = 1;
    public static int type_Warning = 2;
    public static int type_Wear = 4;
    public static int type_Error = 0;
}
