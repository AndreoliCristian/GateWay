package com.example.gateway.adapters;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.gateway.R;
import com.example.gateway.models.BlePeripheralListItem;

import java.util.ArrayList;

public class BlePeripheralListAdapter extends BaseAdapter {
    private static String TAG = BlePeripheralListAdapter.class.getCanonicalName();
    private ArrayList<BlePeripheralListItem> mBluetoothPeripheralListItems = new ArrayList<BlePeripheralListItem>(); // list of Peripherals

    @Override
    public int getCount() {
        return mBluetoothPeripheralListItems.size();
    }

    public void addBluetoothPeripheral(BluetoothDevice bluetoothDevice){
        int listItemId = mBluetoothPeripheralListItems.size();
        BlePeripheralListItem listItem = new BlePeripheralListItem(bluetoothDevice);
        listItem.setItemId(listItemId);
        mBluetoothPeripheralListItems.add(listItem);
    }

    public ArrayList<BlePeripheralListItem> getItems(){
        return mBluetoothPeripheralListItems;
    }

    public void clear(){
        mBluetoothPeripheralListItems.clear();
    }

    @Override
    public Object getItem(int position) {
        return mBluetoothPeripheralListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mBluetoothPeripheralListItems.get(position).getItemId();
    }

    public static class ViewHolder{
        public TextView deviceName;
        public TextView deviceMac;
        public TextView etichetta_connection_status;
        public TextView connection_Status;
        public TextView etichetta_batteryLevel;
        public TextView batteryLevel;
        public TextView etichetta_indossato;
        public TextView indossato;
        public TextView etichetta_fall;
        public TextView fall;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder peripheralListItemView;
        //	if	this	ListItem	does	not	exist	yet,	generate	it
        //	otherwise,	use	it
        if(convertView == null){
            //	convert	list_item_peripheral.xml	to	a	View
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            v = inflater.inflate(R.layout.list_item_peripheral,null);
            //	match	the	UI	stuff	in	the	list	Item	to	what's	in	the	xml	file
            peripheralListItemView = new ViewHolder();
            peripheralListItemView.deviceName = (TextView)v.findViewById(R.id.advertise_name);
            peripheralListItemView.deviceMac = (TextView)v.findViewById(R.id.mac_address);
            peripheralListItemView.etichetta_batteryLevel = (TextView)v.findViewById(R.id.etichetta_batteryLevel);
            peripheralListItemView.batteryLevel = (TextView)v.findViewById(R.id.batteryLevel);
            peripheralListItemView.etichetta_connection_status = (TextView)v.findViewById(R.id.etichetta_status);
            peripheralListItemView.connection_Status = (TextView)v.findViewById(R.id.status);
            peripheralListItemView.etichetta_indossato = (TextView)v.findViewById(R.id.etichetta_indossato);
            peripheralListItemView.indossato = (TextView)v.findViewById(R.id.indossato);
            peripheralListItemView.etichetta_fall = (TextView)v.findViewById(R.id.etichetta_fall);
            peripheralListItemView.fall = (TextView)v.findViewById(R.id.fall);
            v.setTag(peripheralListItemView);
        }
        else{
            peripheralListItemView = (ViewHolder)v.getTag();
        }
        Log.v(TAG,"ListItem	size:	"+	mBluetoothPeripheralListItems.size());

        //	if	there	are	known	Peripherals,	create	a	ListItem	that	says	so
        //	otherwise,	display	a	ListItem	with	Bluetooth	Periheral	information
        if(mBluetoothPeripheralListItems.size() <= 0){
            peripheralListItemView.deviceName.setText(R.string.peripheral_list_empty);
        }else{
            BlePeripheralListItem item = mBluetoothPeripheralListItems.get(position);
            peripheralListItemView.deviceName.setText(item.getDeviceName());
            peripheralListItemView.deviceMac.setText(item.getDeviceMac());
            peripheralListItemView.etichetta_batteryLevel.setText("Batt");
            peripheralListItemView.batteryLevel.setText("100%");
            peripheralListItemView.etichetta_connection_status.setText("Status");
            peripheralListItemView.connection_Status.setText("NO");
            peripheralListItemView.etichetta_indossato.setText("Wear");
            peripheralListItemView.indossato.setText("NO");
            peripheralListItemView.etichetta_fall.setText("Fall");
            peripheralListItemView.fall.setText("100%");
        }
        return	v;

    }
}
