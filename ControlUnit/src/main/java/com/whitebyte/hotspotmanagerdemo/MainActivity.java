/*
 * Copyright 2013 WhiteByte (Nick Russler, Ahmet Yueksektepe).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.whitebyte.hotspotmanagerdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;

import info.whitebyte.hotspotmanager.ClientScanResult;
import info.whitebyte.hotspotmanager.FinishScanListener;
import info.whitebyte.hotspotmanager.WifiApManager;

import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
//import BLE stuff
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import android.Manifest.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcarnival.controllers.SCControlUnitController;
import com.smartcarnival.controllers.BluetoothMetadata;

import java.util.List;
import java.util.UUID;

public class MainActivity extends Activity
{
    TextView textView1;
    WifiApManager wifiApManager;
    SCControlUnitController _controllerInternal;

    //start bluetooth vars
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;


    private void InitBT()
    {

        if (ContextCompat.checkSelfPermission(this,
                permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission.ACCESS_FINE_LOCATION)) {
                // Sroid.Manifest.how an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{permission.ACCESS_FINE_LOCATION},
                        0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mHandler = new Handler();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.i("[SC]ControlUnit", "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        textView1 = (TextView) findViewById(R.id.textView1);

        wifiApManager = new WifiApManager(this);

        // force to show the settings page for demonstration purpose of this method
        //wifiApManager.showWritePermissionSettings(true);

        scan();
        textView1.setText("");
        textView1.append("Staring SC Web Server...\n\n");

        try
        {
            TinyWebServer.startServer("10.231.1.111", 9000, "/");
            _controllerInternal = (SCControlUnitController)TinyWebServer.sc_controller;

            textView1.append("Init Bluetooth()...\n\n");
            InitBT();
            textView1.append("Bluetooth() Init Done...\n\n");
        }

        catch(Exception ex)
        {
            textView1.append(ex.getMessage());
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        wifiApManager.showWritePermissionSettings(false);
        textView1.append("[SC] - OnResume()...\n\n");

        try
        {
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                if (Build.VERSION.SDK_INT >= 21) {
                    mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                    settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build();
                    filters = new ArrayList<ScanFilter>();
                }
                scanLeDevice(true);
            }
        }

        catch(Exception ex)
        {
            textView1.append("[SC]APP onResume() failed "+ex.getMessage());
        }

    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("[SC]APP onLeScan", device.toString());
                            connectToDevice(device);
                        }
                    });
                }
            };

    public void connectToDevice(BluetoothDevice device)
    {
      //  Log.i("[SC]APP", "[SC] - connectToDevice() found device: "+device.getName()+ " "+device.getAddress());

        try
        {
            String DeviceName = device.getName();
            String DeviceAdr = device.getAddress();
            //todo: validate name against known prefix
            if (DeviceName != null && DeviceAdr != "")
            {

                if (mGatt == null && device.getName().contains("Touchdown"))
                {


                    Log.i("[SC]APP", "[SC] - Connecting to ''"+device.getName()+ "'...");
                    textView1.append("[SC] - Connecting to ''"+device.getName()+ "'...");

                    TinyWebServer.sc_controller.AddSensor(device.getName()+"_"+device.getAddress());

                    //stop scan
                    scanLeDevice(false);

                    mGatt = device.connectGatt(this, false, gattCallback);

                }
            }
        }

        catch (Exception ex)
        {
            Log.i("[SC]APP", "[SC] - connectToDevice() error "+ex);
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("[SC]APP", "[SC] - Device CONNECTED!");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i("[SC]APP", "[SC] - Device DISCONNECTED");
                    break;
                default:
                    Log.i("[SC]APP", "[SC] - Device Other State Change");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            Log.i("[SC]APP", "[SC] - onServicesDiscovered()");
            List<BluetoothGattService> services = gatt.getServices();

            for(BluetoothGattService s : services)
            {
                String CurrID = s.getUuid().toString();
                Log.i("[SC]APP", "[SC] - Discovered Service: "+CurrID);

                if (CurrID.equals("6e400001-b5a3-f393-e0a9-e50e24dcca9e"))
                {
                    BluetoothGattCharacteristic scoringCharacteristic = s.getCharacteristic(UUID.fromString("9827DC3E-36E1-4688-B7F5-EA07361B26A8"));
                    Log.i("[SC]APP", "[SC] - got read/notify characteristic "+scoringCharacteristic);

                    gatt.setCharacteristicNotification(scoringCharacteristic, true);

                    //define another descriptor for writting?
                 /*   BluetoothGattCharacteristic scoringWriteCharacteristic = s.getCharacteristic(UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCBEEF"));
                    Log.i("[SC]APP", "[SC] - got write characteristic "+scoringCharacteristic);
                    //apparently this characterisitic has no descriptorss, so write to charecteristic  directly
                    scoringWriteCharacteristic.setValue("hi");
                    gatt.writeCharacteristic(scoringWriteCharacteristic);*/
                    BluetoothMetadata m = new BluetoothMetadata(gatt, s);
                    String SensorID = gatt.getDevice().getName()+"_"+gatt.getDevice().getAddress();
                    _controllerInternal.AssociateGatt(SensorID, m);

                    //notification descriptior is 0x2902
                    BluetoothGattDescriptor descriptor = scoringCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                    Log.i("[SC]APP", "[SC] - got descriptor "+descriptor.getUuid());
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);

                }
            }

            /*gatt.readCharacteristic(services.get(1).getCharacteristics().get
                    (0));*/
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {

            Log.i("[SC]APP", "[SC] - onCharacteristicRead()");

            String value = new String(characteristic.getValue());
            Log.i("[SC]APP", "[SC] - Got Value: "+ value);
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            Log.i("[SC]APP", "[SC] - onCharacteristicChanged()");

            String value = new String(characteristic.getValue());
            Log.i("[SC]APP", "[SC] - Got Value: "+ value);

            textView1.append("[SC] - Got Value: "+ value+"\n\n");
         //   NotifyScoreboard(new String(characteristic.getValue()));
        }

        public void NotifyScoreboard(String value)
        {
            textView1.append("[SC] - Got Value: "+ value+"\n\n");
        }
    };


    private void scanLeDevice(final boolean enable)
    {
        Log.i("[SC]APP", "[SC] - scanLeDevice(), enable "+enable);

        try
        {
            if (enable)
            {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        if (Build.VERSION.SDK_INT < 21) {
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        } else {
                            mLEScanner.stopScan(mScanCallback);

                        }
                    }
                }, SCAN_PERIOD);
                if (Build.VERSION.SDK_INT < 21) {
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                } else {
                    mLEScanner.startScan(filters, settings, mScanCallback);
                }
            }
            else
            {
                if (Build.VERSION.SDK_INT < 21) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                } else {
                    mLEScanner.stopScan(mScanCallback);
                }
            }
        }

        catch(Exception ex)
        {
            Log.i("[SC]APP", "scanLeDevice() failed "+ex);
        }
    }

    private ScanCallback mScanCallback = new ScanCallback()
    {

        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
           // Log.i("[SC]APP", "[SC] - onScanResult()");
            //Log.i("[SC]callbackType", String.valueOf(callbackType));
            //Log.i("[SC]APP", "result: "+result.toString());
            BluetoothDevice btDevice = result.getDevice();
            connectToDevice(btDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            Log.i("[SC]APP", "[SC] - onBatchScanResults()");

            for (ScanResult sr : results) {
                Log.i("[SC]APP", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode)
        {
            Log.i("[SC]APP", "[SC] - onScanFailed() "+ errorCode);

        }
    };


    @Override
    protected void onPause()
    {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy()
    {
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
        super.onDestroy();
    }

    private void scan() {
        wifiApManager.getClientList(false, new FinishScanListener() {

            @Override
            public void onFinishScan(final ArrayList<ClientScanResult> clients) {

                textView1.append("WifiApState: " + wifiApManager.getWifiApState() + "\n\n");
                textView1.append("Clients: \n");
                for (ClientScanResult clientScanResult : clients) {
                    textView1.append("####################\n");
                    textView1.append("IpAddr: " + clientScanResult.getIpAddr() + "\n");
                    textView1.append("Device: " + clientScanResult.getDevice() + "\n");
                    textView1.append("HWAddr: " + clientScanResult.getHWAddr() + "\n");
                    textView1.append("isReachable: " + clientScanResult.isReachable() + "\n");
                }
            }
        });

    }

    private void identifySensorTest()
    {
        String SensorID = "Touchdown sensor1_30:AE:A4:1B:71:BA";

        textView1.append("Invoking sc_controller.Identify()\n\n");
        try {
            TinyWebServer.sc_controller.IdentifySensor(SensorID);
        }
        catch(Exception ex)
        {
            textView1.append(ex.getMessage());
        }
    }

    private  void getSensors()
    {
        try {

            textView1.append("Invoking sc_controller.GetSensors()\n\n");
            ArrayList<String> SensorList = TinyWebServer.sc_controller.GetSensors();
            ObjectMapper om = new ObjectMapper();
            String jsonResponse = om.writeValueAsString(SensorList);
            textView1.append(jsonResponse+"\n\n");

            Log.i("[SC]APP", "[SC] - Invoking sc_controller.GetSensors() "+ jsonResponse);
        }

        catch (Exception ex)
        {
            textView1.append(ex.getMessage());
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Get Clients");
        menu.add(0, 1, 0, "Open AP");
        menu.add(0, 2, 0, "Close AP");
        menu.add(0, 3, 0, "Get Sensors");
        menu.add(0, 4, 0, "Identify Sensor Test");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                scan();
                break;
            case 1:
                wifiApManager.setWifiApEnabled(null, true);
                break;
            case 2:
                wifiApManager.setWifiApEnabled(null, false);
                break;
            case 3:
                getSensors();
                break;
            case 4:
                identifySensorTest();
                break;
        }

        return super.onMenuItemSelected(featureId, item);
    }
}
