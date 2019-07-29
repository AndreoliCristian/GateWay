package com.example.gateway.adapters;

//Classe fittizia che rappresenta il menù che si apre cliccando su un dispositivo
public class PeripheralConfig {
    private int mItemId;
    private Object o;

    public PeripheralConfig(int mItemId, Object o) {
        this.mItemId = mItemId;
        this.o = o;
    }

    public int getmItemId() {
        return mItemId;
    }

    public Object getO() {
        return o;
    }
}
