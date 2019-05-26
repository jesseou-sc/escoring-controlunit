package com.smartcarnival.controllers;
import java.util.ArrayList;
import com.smartcarnival.models.Amusement;

public interface ISCControlUnitController
{
    //GET /amusements
    //	Return list of current amusements
    //	Array of objects (amusement id)
    public ArrayList<Integer> GetAmusements();

    //GET /sensors
    //	Return list of sensors. All available sensors seen by control unit
    public  ArrayList<String> GetSensors();

    //Commands control unit to send a message to the indicated sensor to flash led(s).
    //Method  posts sensor id to config app -> config app sends BLE message to sensor
    //return 0 if successful, else error code
    public int IdentifySensor(String SensorID);

    //POST /config
    public int SetConfiguration(Amusement amusementConfig);

    public int AddSensor(String SensorGuid);


}
