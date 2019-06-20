package com.smartcarnival.controllers;


import com.smartcarnival.models.Amusement;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.hardware.Sensor;
import android.util.Log;

public class SCControlUnitController implements  ISCControlUnitController
{
    private ConcurrentHashMap<String, Integer> _internalSensorMap;

    private ConcurrentHashMap<String, BluetoothMetadata> _internalGattMap;

    public SCControlUnitController()
    {
        //SensorGUID_SensorName, SensorValue
        _internalSensorMap = new ConcurrentHashMap<String,Integer>();
        _internalGattMap = new ConcurrentHashMap<String,BluetoothMetadata>();
    }

    //todo: not sure what update behavior is with this structure..need to test
    public int AssociateGatt(String SensorID, BluetoothMetadata m)
    {
        _internalGattMap.put(SensorID, m);

        return 0;
    }

    @Override
    public int AddSensor(String SensorID)
    {
        Log.i("[SC]APP", "CU::AddSensor()");
        //0 initial score value
        _internalSensorMap.put(SensorID, 0);

        Log.i("[SC]APP", "CU::AddSensor() "+_internalSensorMap.size());
        return 0;
    }

    @Override
    public ArrayList<Integer> GetAmusements()
    {
        ArrayList<Integer> amusementList = new ArrayList<Integer>();

        amusementList.add(1234);

        return amusementList;
    }

    @Override
    public int IdentifySensor(String SensorID)
    {
        Log.i("[SC]APP", "CU::IdentifySensor() "+SensorID);
        BluetoothMetadata metadata = _internalGattMap.get(SensorID);

        if (metadata == null)
        {
            return -1;
        }

        Log.i("[SC]APP", "CU::IdentifySensor() metadata: "+metadata.toString());

        BluetoothGatt gatt = metadata.getBluetoothGatt();
        BluetoothGattService bluetoothService = metadata.getBluetoothService();
        BluetoothGattCharacteristic scoringWriteCharacteristic = bluetoothService.getCharacteristic(UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCBEEF"));

        //apparently this characterisitic has no descriptorss, so write to charecteristic  directly
        scoringWriteCharacteristic.setValue("BLINK");
        gatt.writeCharacteristic(scoringWriteCharacteristic);

        Log.i("[SC]APP", "CU::WroteChar() "+SensorID);
        return 0;
    }

    @Override
    public int SetConfiguration(Amusement amusementConfig)
    {
        return 0;
    }


    @Override
    public ArrayList<String> GetSensors() {

        ArrayList<String> sensorValues = new ArrayList<String>();


        for (String s: _internalSensorMap.keySet())
        {
            sensorValues.add(s);
        }

        return sensorValues;
    }

    public boolean ContainsSensor(String SensorID)
    {
        return _internalGattMap.containsKey(SensorID);
    }

    public int RemoveSensor(String SensorID)
    {
        Log.i("[SC]APP", "CU::ContainsSensor() "+SensorID);

        if (_internalGattMap.containsKey(SensorID))
        {
            _internalGattMap.remove(SensorID);
            return 0;
        }

        return 1;
    }
}


