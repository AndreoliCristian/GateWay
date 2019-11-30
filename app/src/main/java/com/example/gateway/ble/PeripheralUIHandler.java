package com.example.gateway.ble;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.Selection;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gateway.R;
import com.example.gateway.adapters.BleGattProfileListAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class PeripheralUIHandler extends Activity {

    private final Drawable battery_charge_25;
    private final Drawable battery_charge_50;
    private final Drawable battery_charge_75;
    private final Drawable battery_charge_100;
    private final Drawable bt5;
    private final Drawable bt25;
    private final Drawable bt50;
    private final Drawable bt75;
    private final Drawable bt100;
    private final Drawable red_led;
    private final Drawable green_led;
    private final Drawable red_led_small;
    private final Drawable green_led_small;

    public BleGattProfileListAdapter.GroupViewHolder groupView;
    public BleGattProfileListAdapter.ChildViewHolder childView;

    private Context context;
    private Activity a;
    private String serial;
    private String mac;
    private int fallPercentage;
    private int warningPercentage;

    private AlertDialog dialog;

    private TextView logger;

    public PeripheralUIHandler(Context context, Activity a, String serial, String mac) {
        this.context = context;
        this.a = a;
        this.serial = serial;
        this.mac = mac;
        battery_charge_25 = context.getDrawable(R.mipmap.battery_charge_25);
        battery_charge_50 = context.getDrawable(R.mipmap.battery_charge_50);
        battery_charge_75 = context.getDrawable(R.mipmap.battery_charge_75);
        battery_charge_100 = context.getDrawable(R.mipmap.battery_charge_100);
        bt5 = context.getDrawable(R.mipmap.forebt5);
        bt25 = context.getDrawable(R.mipmap.forebt25);
        bt50 = context.getDrawable(R.mipmap.forebt50);
        bt75 = context.getDrawable(R.mipmap.forebt75);
        bt100 = context.getDrawable(R.mipmap.forebt100);
        red_led = context.getDrawable(R.drawable.red_led);
        green_led = context.getDrawable(R.drawable.green_led);
        red_led_small = context.getDrawable(R.drawable.red_led_small);
        green_led_small = context.getDrawable(R.drawable.green_led_small);

        logger = (TextView)a.findViewById(R.id.logger);
        logger.setMovementMethod(new ScrollingMovementMethod());
        logger.setGravity(Gravity.BOTTOM);
    }

    public void onBleConnected(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupView.connection_Status.setImageDrawable(green_led_small);
                childView.statusLed.setImageDrawable(green_led);
                childView.connect.setEnabled(false);
                childView.disconnect.setEnabled(true);
                childView.sendThresholdInterest.setEnabled(true);
                childView.sendThresholdWear.setEnabled(true);
                childView.sendThresholdProbability.setEnabled(true);
                childView.options.setEnabled(true);
            }
        });
        updateLogger("ST " + serial + " CONNECTED");
    }

    public void onBleDisconnected(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupView.connection_Status.setImageDrawable(red_led_small);
                childView.statusLed.setImageDrawable(red_led);
                childView.connect.setEnabled(true);
                childView.disconnect.setEnabled(false);
                childView.sendThresholdInterest.setEnabled(false);
                childView.sendThresholdWear.setEnabled(true);
                childView.sendThresholdProbability.setEnabled(true);
                groupView.indossato.setImageDrawable(red_led_small);
                childView.wearLed.setImageDrawable(red_led);
                childView.options.setEnabled(false);
            }
        });
        updateLogger("ST " + serial + " DISCONNECTED");
    }

    public void updateBatteryLevel(final int percentage){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(percentage<=5){
                    childView.bt.setImageDrawable(bt5);
                    groupView.battery.setImageDrawable(battery_charge_25);
                    childView.charge.setProgressDrawable(context.getResources().getDrawable(R.drawable.redprogress));
                }
                else if(percentage<=25){
                    childView.bt.requestLayout();
                    childView.bt.getLayoutParams().height = 504;
                    childView.bt.getLayoutParams().width = 700;
                    childView.bt.setImageDrawable(bt25);
                    groupView.battery.setImageDrawable(battery_charge_25);
                    childView.charge.setProgressDrawable(context.getResources().getDrawable(R.drawable.redprogress));
                }else if (percentage<=50){
                    childView.bt.requestLayout();
                    childView.bt.getLayoutParams().height = 504;
                    childView.bt.getLayoutParams().width = 700;
                    childView.bt.setImageDrawable(bt50);
                    groupView.battery.setImageDrawable(battery_charge_50);
                }else if (percentage<=75){
                    childView.bt.requestLayout();
                    childView.bt.getLayoutParams().height = 504;
                    childView.bt.getLayoutParams().width = 700;
                    childView.bt.setImageDrawable(bt75);
                    groupView.battery.setImageDrawable(battery_charge_75);
                }else{
                    childView.bt.setImageDrawable(bt100);
                    groupView.battery.setImageDrawable(battery_charge_100);
                }
                childView.charge.setProgress(percentage);
                childView.percentuale.setText(Integer.toString(percentage)+"%");
                //childView.battery.setText(percentage);
            }
        });
        updateLogger("ST " + serial + " BATTERY = " + percentage);
    }

    public void updateFall(final String fall){
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupView.etichetta_fall.setText("Fall");
                groupView.fall.setText(fall);
                if(dialog != null){
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                }
                if(Integer.parseInt(fall)>fallPercentage) {
                    if (dialog != null) {
                        dialog.cancel();
                    }
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(a).setTitle("FALL DETECTED").setMessage("SensorTile = " + SensorTile + "\n" + "MAC = " + mac + "\n" + "PERCENTAGE = " + fall).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            fallPercentage = 0;
                        }
                    }).setIcon(android.R.drawable.ic_dialog_alert);
                    //AlertDialog dialog = alertDialog.create();
                    dialog = alertDialog.create();
                    dialog.show();
                }
                fallPercentage = Integer.parseInt(fall);
            }
        });*/
        updateLogger("ST " + serial + " FALL = " + fall);
    }

    public void updateProbability(final String warning){
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupView.etichetta_fall.setText("War");
                groupView.fall.setText(warning);
                if(dialog != null){
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                }
                if(Integer.parseInt(warning) > warningPercentage) {
                    if(dialog != null){
                        dialog.cancel();
                    }
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(a).setCancelable(false).setTitle("WARNING DETECTED").setMessage("SensorTile = " + SensorTile + "\n" + "MAC = " + mac + "\n" + "PERCENTAGE = " + warning).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            warningPercentage = 0;
                        }
                    }).setIcon(android.R.drawable.ic_dialog_alert);
                    dialog = alertDialog.create();
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
                warningPercentage = Integer.parseInt(warning);
            }
        });*/
        updateLogger("ST " + serial + " WARNING = " + warning);
    }

    public  void updateStatus(final boolean[] bits){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(bits[GateWayConfig.wearBit]){
                    groupView.indossato.setImageDrawable(green_led_small);
                    childView.wearLed.setImageDrawable(green_led);
                    updateLogger("ST " + serial + " INDOSSATO");
                }else {
                    groupView.indossato.setImageDrawable(red_led_small);
                    childView.wearLed.setImageDrawable(red_led);
                    updateLogger("ST " + serial + " NON INDOSSATO");
                }
                if(bits[GateWayConfig.errorBit]) {
                    Toast.makeText(context, "Malfunzionamento HardWare", Toast.LENGTH_SHORT).show();
                    groupView.deviceName.setTextColor(Color.parseColor("#FF0000"));
                    updateLogger("ST " + serial + " INTERNAL HARDWARE ERROR");
                }else{
                    groupView.deviceName.setTextColor(Color.parseColor("#000000"));
                }
            }
        });
    }

    public void updateLogger(final String string) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Calendar now = Calendar.getInstance();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss a");
                //logger.setTextColor(0xffff0000);
                logger.append(formatter.format(now.getTime()) + " " +string + "\n");
                //logger.setTextColor(0xffff0000);
                Editable editable = logger.getEditableText();
                Selection.setSelection(editable, editable.length());
            }
        });
    }
}
