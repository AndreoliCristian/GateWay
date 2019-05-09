package com.example.gateway.adapters;

import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.example.gateway.R;
import com.example.gateway.ble.BlePeripheral;
import com.example.gateway.models.BlePeripheralListItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class BleGattProfileListAdapter  extends BaseExpandableListAdapter {
    private final static String TAF = BleGattProfileListAdapter.class.getSimpleName();
    private ArrayList<BlePeripheralListItem> mBluetoothPeripheralListItems = new ArrayList<BlePeripheralListItem>(); // list of Peripherals
    private Map<Integer,ArrayList<DeviceConfig>> mDeviceConfig = new HashMap<Integer, ArrayList<DeviceConfig>>();
    private DeviceConfig options;

    public BleGattProfileListAdapter(Map<Integer, BlePeripheral> listaSensori) {
        this.listaSensori = listaSensori;
    }

    public void addBluetoothPeripheral(BluetoothDevice bluetoothDevice){
        int listItemId = mBluetoothPeripheralListItems.size();
        BlePeripheralListItem listItem = new BlePeripheralListItem(bluetoothDevice);
        listItem.setItemId(listItemId);
        mBluetoothPeripheralListItems.add(listItem);

        //Metto un oggetto figlio fittizio dentro a ogni BleperipheralListItem
        //Ogni gruppo avrà sempre e solo 1 figlio
        Object o = new Object();
        mDeviceConfig.put(listItemId, new ArrayList<DeviceConfig>());
        int idOptions = mDeviceConfig.size();
        DeviceConfig d = new DeviceConfig(idOptions, o);
        mDeviceConfig.get(listItemId).add(d);
    }

    public ArrayList<BlePeripheralListItem> getItems(){
        return mBluetoothPeripheralListItems;
    }

    public void clear(){
        mBluetoothPeripheralListItems.clear();

    }

    @Override
    public BlePeripheralListItem getGroup(int groupPosition) {
        return mBluetoothPeripheralListItems.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mBluetoothPeripheralListItems.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return mBluetoothPeripheralListItems.get(groupPosition).getItemId();
    }

    public static class GroupViewHolder{
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
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        //MIE PROVE
        //----------------------------
        //listaSensori.get(groupPosition).setV(parent);
        //----------------------------



        View v = convertView;
        GroupViewHolder peripheralListItemView;
        //	if	this	ListItem	does	not	exist	yet,	generate	it
        //	otherwise,	use	it
        if(convertView == null){
            //	convert	list_item_peripheral.xml	to	a	View
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            v = inflater.inflate(R.layout.list_item_peripheral,parent,false);
            //	match	the	UI	stuff	in	the	list	Item	to	what's	in	the	xml	file
            peripheralListItemView = new GroupViewHolder();
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
            peripheralListItemView = (GroupViewHolder) v.getTag();
        }
        Log.v(TAG,"ListItem	size:	"+	mBluetoothPeripheralListItems.size());

        //	if	there	are	known	Peripherals,	create	a	ListItem	that	says	so
        //	otherwise,	display	a	ListItem	with	Bluetooth	Periheral	information
        if(mBluetoothPeripheralListItems.size() <= 0){
            peripheralListItemView.deviceName.setText(R.string.peripheral_list_empty);
        }else{
            BlePeripheralListItem item = mBluetoothPeripheralListItems.get(groupPosition);
            peripheralListItemView.deviceName.setText(item.getDeviceName());
            peripheralListItemView.deviceMac.setText(item.getDeviceMac());
            peripheralListItemView.etichetta_batteryLevel.setText("Batt");
            //peripheralListItemView.batteryLevel.setText("100%");
            //peripheralListItemView.etichetta_connection_status.setText("Status");
            //peripheralListItemView.connection_Status.setText("NO");
            //peripheralListItemView.etichetta_indossato.setText("Wear");
            //peripheralListItemView.indossato.setText("NO");
            //peripheralListItemView.etichetta_fall.setText("Fall");
            //peripheralListItemView.fall.setText("100%");
        }

        if(listaSensori.get(groupPosition).getNuovavista() == null) {
            listaSensori.get(groupPosition).setNuovavista(peripheralListItemView);
        }
        //listaSensori.get(groupPosition).setV(v);
        return	v;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mDeviceConfig.get(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return mDeviceConfig.get(groupPosition).get(childPosition).getmItemId();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mDeviceConfig.get(groupPosition).size();
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public static class ChildViewHolder{
        public Button connect;
        //public View progressSpinner;
        public Button disconnect;
        public Button options;
    }
    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View v = convertView;
        ChildViewHolder holder;
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            v = inflater.inflate(R.layout.device_config,null);

            holder = new ChildViewHolder();
            holder.connect = (Button)v.findViewById(R.id.connect);
            //holder.progressSpinner = (View)v.findFocus(R.id.scan_progress_item);
            holder.disconnect = (Button)v.findViewById(R.id.disconnect);
            holder.options = (Button)v.findViewById(R.id.options);
            v.setTag(holder);
        }
        else {
            holder = (ChildViewHolder) v.getTag();
        }
        holder.connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    listaSensori.get(groupPosition).connect(listaSensori.get(groupPosition).context);
                } catch (Exception e){
                    Log.e(TAG,"Error connecting to peripheral");
                }
            }
        });
        holder.disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    listaSensori.get(groupPosition).disconnect();
                } catch (Exception e){
                    Log.e(TAG,"Error connecting to peripheral");
                }
            }
        });
        return v;
    }

    public View getGroupView(ExpandableListView listView, int groupPosition) {
        long packedPosition = ExpandableListView.getPackedPositionForGroup(groupPosition);
        int flatPosition = listView.getFlatListPosition(packedPosition);
        int first = listView.getFirstVisiblePosition();
        return listView.getChildAt(flatPosition - first);
    }

    public Map<Integer, BlePeripheral> listaSensori;

    public void connect(BluetoothDevice bluetoothDevice){

    }
}
