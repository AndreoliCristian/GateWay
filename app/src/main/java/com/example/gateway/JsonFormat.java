package com.example.gateway;

public class JsonFormat {

    public String SensorTile;
    public String gateway;
    public String date;
    public int type;
    public String value;

    public JsonFormat(String SensorTile, String gateway, String date, int type, String value) {
        this.SensorTile = SensorTile;
        this.gateway = gateway;
        this.date = date;
        this.type = type;
        this.value = value;
    }
}
