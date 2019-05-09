package com.example.gateway.ble.callbacks;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public abstract class BleScanCallbackv21 extends ScanCallback {
    @Override
    public abstract void onScanResult(int callbackType, ScanResult result);

    @Override
    public abstract void onBatchScanResults(List<ScanResult> results);

    @Override
    public abstract void onScanFailed(int errorCode);

    public abstract  void  onScanComplete();
}
