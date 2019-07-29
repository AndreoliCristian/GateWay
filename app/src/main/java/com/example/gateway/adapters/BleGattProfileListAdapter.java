package com.example.gateway.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.gateway.R;
import com.example.gateway.ble.BlePeripheral;
import com.example.gateway.models.BlePeripheralListItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class BleGattProfileListAdapter  extends BaseExpandableListAdapter {
    private final static String TAG = BleGattProfileListAdapter.class.getSimpleName();
    private ArrayList<BlePeripheralListItem> mBluetoothPeripheralListItems = new ArrayList<BlePeripheralListItem>(); // list of Peripherals
    private Map<Integer,ArrayList<PeripheralConfig>> mDeviceConfig = new HashMap<Integer, ArrayList<PeripheralConfig>>();
    private PeripheralConfig options;
    private Context context;

    public BleGattProfileListAdapter(Map<Integer, BlePeripheral> sensorList, Context context) {
        this.sensorList = sensorList;
        this.context = context;
    }

    public void addBluetoothPeripheral(BluetoothDevice bluetoothDevice){
        int listItemId = mBluetoothPeripheralListItems.size();
        BlePeripheralListItem listItem = new BlePeripheralListItem(bluetoothDevice);
        listItem.setItemId(listItemId);
        mBluetoothPeripheralListItems.add(listItem);

        //Metto un oggetto figlio fittizio dentro a ogni BleperipheralListItem
        //Ogni gruppo avrà sempre e solo 1 figlio
        Object o = new Object();
        mDeviceConfig.put(listItemId, new ArrayList<PeripheralConfig>());
        int idOptions = mDeviceConfig.size();
        PeripheralConfig d = new PeripheralConfig(idOptions, o);
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
        public ImageView connection_Status;
        public ImageView battery;
        public TextView etichetta_indossato;
        public ImageView indossato;
        public TextView etichetta_fall;
        public TextView fall;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

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
            peripheralListItemView.battery = (ImageView)v.findViewById(R.id.batt);
            peripheralListItemView.etichetta_connection_status = (TextView)v.findViewById(R.id.etichetta_status);
            peripheralListItemView.connection_Status = (ImageView)v.findViewById(R.id.status);
            peripheralListItemView.etichetta_indossato = (TextView)v.findViewById(R.id.etichetta_indossato);
            peripheralListItemView.indossato = (ImageView)v.findViewById(R.id.indossato);
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
        }

        if(sensorList.get(groupPosition).getGroupView() == null) {
            sensorList.get(groupPosition).setGroupView(peripheralListItemView);
        }
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
        public Button disconnect;
        public Button options;
        public ImageView statusLed;
        public ImageView wearLed;
        public ImageView bt;
        public TextView battery;
        public ProgressBar charge;
        public EditText editThresholdProbability;
        public EditText editThresholdInterest;
        public EditText editThresholdWear;
        public Button sendThresholdProbability;
        public Button sendThresholdInterest;
        public Button sendThresholdWear;
    }
    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View v = convertView;
        final ChildViewHolder holder;
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            v = inflater.inflate(R.layout.device_config,null);

            holder = new ChildViewHolder();
            holder.connect = (Button)v.findViewById(R.id.connect);
            holder.disconnect = (Button)v.findViewById(R.id.disconnect);
            holder.options = (Button)v.findViewById(R.id.options);
            holder.statusLed = (ImageView)v.findViewById(R.id.status);
            holder.wearLed = (ImageView)v.findViewById(R.id.wear_led);
            holder.bt = (ImageView) v.findViewById(R.id.bt);
            holder.battery = (TextView) v.findViewById(R.id.battery);
            holder.charge = (ProgressBar) v.findViewById(R.id.charge);
            holder.editThresholdProbability = (EditText)v.findViewById(R.id.editProbabilityThreshold);
            holder.sendThresholdProbability = (Button)v.findViewById(R.id.send_probabilitythreshold);
            holder.editThresholdInterest = (EditText)v.findViewById(R.id.editInterestThreshold);
            holder.sendThresholdInterest = (Button)v.findViewById(R.id.send_interestthreshold);
            holder.editThresholdWear = (EditText)v.findViewById(R.id.editWearThreshold);
            holder.sendThresholdWear = (Button)v.findViewById(R.id.send_wearthreshold);
            Drawable d = context.getDrawable(R.drawable.progress);
            holder.charge.setProgressDrawable(d);
            v.setTag(holder);
        }
        else {
            holder = (ChildViewHolder) v.getTag();
        }
        holder.connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sensorList.get(groupPosition).connect(sensorList.get(groupPosition).context);
                } catch (Exception e){
                    Log.e(TAG,"Error connecting to peripheral");
                    e.printStackTrace();
                }
            }
        });
        holder.disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sensorList.get(groupPosition).disconnect();
                } catch (Exception e){
                    Log.e(TAG,"Error disconnecting to peripheral");
                }
            }
        });
        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorList.get(groupPosition).prova();
            }
        });
        holder.sendThresholdProbability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int percentage = Integer.parseInt(holder.editThresholdProbability.getText().toString());
                if(percentage<=100 && percentage >=0) {
                    Toast.makeText(context, "NEW PERCENTAGE = "+ percentage, Toast.LENGTH_SHORT).show();
                    sensorList.get(groupPosition).updateThresholdProbability(percentage);
                }else {
                    Toast.makeText(context, "Inserire un numero tra 0 e ?100", Toast.LENGTH_SHORT).show();
                }
            }
        });
        holder.sendThresholdInterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float percentage = Float.parseFloat(holder.editThresholdInterest.getText().toString());
                if(percentage<=100000 && percentage >=0) {
                    sensorList.get(groupPosition).updateThresholdInterest(percentage);
                }else {
                    Toast.makeText(context, "Inserire un numero tra 0 e 100000", Toast.LENGTH_SHORT).show();
                }
            }
        });
        holder.sendThresholdWear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float percentage = Float.parseFloat(holder.editThresholdWear.getText().toString());
                if(percentage<=100000 && percentage >=0) {
                    Toast.makeText(context, "NEW PERCENTAGE = "+ percentage, Toast.LENGTH_SHORT).show();
                    sensorList.get(groupPosition).updateThresholdWear(percentage);
                }else {
                    Toast.makeText(context, "Inserire un numero tra 0 e 100000", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(sensorList.get(groupPosition).getChildView() == null) {
            sensorList.get(groupPosition).setChildView(holder);
        }

        return v;
    }

    public View getGroupView(ExpandableListView listView, int groupPosition) {
        long packedPosition = ExpandableListView.getPackedPositionForGroup(groupPosition);
        int flatPosition = listView.getFlatListPosition(packedPosition);
        int first = listView.getFirstVisiblePosition();
        return listView.getChildAt(flatPosition - first);
    }

    public Map<Integer, BlePeripheral> sensorList;
}
