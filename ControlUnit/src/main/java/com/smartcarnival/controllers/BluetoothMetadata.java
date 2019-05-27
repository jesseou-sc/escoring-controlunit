package com.smartcarnival.controllers;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

public class BluetoothMetadata
{
    private BluetoothGatt _gatt;
    private BluetoothGattService _gattService;

    public BluetoothMetadata(BluetoothGatt gatt, BluetoothGattService service)
    {
        _gatt = gatt;
        _gattService = service;
    }

    public BluetoothGatt getBluetoothGatt()
    {
        return _gatt;
    }

    public BluetoothGattService getBluetoothService()
    {
        return _gattService;
    }
}
