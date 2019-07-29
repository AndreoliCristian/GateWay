package com.example.gateway;

public class JsonFormat {

    public String serial;
    public String gateway;
    public String date;
    public int type;
    public String value;

    public JsonFormat(String serial, String gateway, String date, int type, String value) {
        this.serial = serial;
        this.gateway = gateway;
        this.date = date;
        this.type = type;
        this.value = value;
    }
}
