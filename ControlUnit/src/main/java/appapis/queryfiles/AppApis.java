/*
 * The MIT License
 *
 * Copyright 2018 Sonu Auti http://sonuauti.com twitter @SonuAuti
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package appapis.queryfiles;

import com.whitebyte.hotspotmanagerdemo.TinyWebServer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.ArrayList;

import android.icu.util.Currency;
import android.util.Log;

/**
 *
 * @author cis
 */
public class AppApis {

    public AppApis(){
    }

    //start CU controller methods

    /*GET /sensors
	Return list of sensors. All available sensors seen by control unit
      */
    public String sensors(HashMap qparms)
    {
        Log.i("[SC]ControlUnit", "/sensors");
        try
        {
            ArrayList<String> SensorList = TinyWebServer.sc_controller.GetSensors();
            ObjectMapper om = new ObjectMapper();
            String jsonResponse = om.writeValueAsString(SensorList);

            Log.i("[SC]ControlUnit", "Return from /sensors size: "+SensorList.size());

            return jsonResponse;
        }

        catch(Exception ex)
        {
            return ex.getMessage();
        }
    }

    public String amusements(HashMap qparms) {
        Log.i("[SC]ControlUnit", "/amusements");
        try {
            ArrayList<String> AmusementList = new ArrayList<String>();
            AmusementList.add("12345");
            ObjectMapper om = new ObjectMapper();
            String jsonResponse = om.writeValueAsString(AmusementList);
            return jsonResponse;
        }

        catch(Exception ex)
        {
            return ex.getMessage();
        }


    }


    public String identify(HashMap qparms) {
        Log.i("[SC]ControlUnit", "/identify?id={id}");

        int returnCode = 0;

        try
        {

            if(qparms!=null)
            {
                String CurrSensorID  = qparms.get("id") + "";
                 returnCode = TinyWebServer.sc_controller.IdentifySensor(CurrSensorID);
            }

            if (returnCode ==0)
            {

                return "IDENTIFY SUCCESS";
            }

            else
            {
                return "IDENTFIY FAIL";
            }
        }

        catch(Exception ex)
        {
            return ex.getMessage();
        }



    }

    public String configuration(HashMap qparms)
    {
    Log.i("[SC]ControlUnit", "/configuration");

        String p="";
        if(qparms!=null){
            p=qparms.get("_POST")+"";
        }

        return p;
    }

    public String helloworld(HashMap qparms){
        //demo of simple html webpage from controller method

        com.whitebyte.hotspotmanagerdemo.TinyWebServer.CONTENT_TYPE="text/html";
        return "<html><head><title>Simple HTML and Javascript Demo</title>\n" +
                "  <script>\n" +
                "  \n" +
                "</script>\n" +
                "  \n" +
                "  </head><body style=\"text-align:center;margin-top: 5%;\" cz-shortcut-listen=\"true\" class=\"\">\n" +
                "    <h3>Say Hello !</h3>\n" +
                "<div style=\"text-align:center;margin-left: 29%;\">\n" +
                "<div id=\"c1\" style=\"width: 100px;height: 100px;color: gray;background: gray;border-radius: 50%;float: left;\"></div>\n" +
                "<div id=\"c2\" style=\"width: 100px;height: 100px;color: gray;background: yellow;border-radius: 50%;float: left;\"></div>\n" +
                "<div id=\"c3\" style=\"width: 100px;height: 100px;color: gray;background: skyblue;border-radius: 50%;float: left;\"></div>\n" +
                "<div id=\"c4\" style=\"width: 100px;height: 100px;color: gray;background: yellowgreen;border-radius: 50%;float: left;\"></div>\n" +
                "<div id=\"c5\" style=\"width: 100px;height: 100px;color: gray;background: red;border-radius: 50%;position: ;position: ;float: left;\" class=\"\"></div></div>\n" +
                "  </body></html>";
    }

    public String simplejson(HashMap qparms){
        //simple json output demo from controller method
        String json = "{\"name\":\"sonu\",\"age\":29}";
        return json.toString();
    }

    public String simplegetparm(HashMap qparms){
        /*
        qparms is hashmap of get and post parameter

        simply use qparms.get(key) to get parameter value
        user _POST as key for post data
        e.g to get post data use qparms.get("_POST"), return will be post method
        data
        */

        System.out.println("output in simplehelloworld "+qparms);
        String p="";
        if(qparms!=null){
            p=qparms.get("age")+"";
        }
        String json = "{\"name\":\"sonu\",\"age\":"+p+",\"isp\":yes}";
        return json.toString();
    }


    //implement web callback here and access them using method name
}
