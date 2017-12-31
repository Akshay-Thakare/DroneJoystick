package com.example.ast.dronejoystick;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by ast on 27/11/17.
 */

public class ControlProcessor {

    TextView throttleText, pitchText, rollText;

    // Constructor
    ControlProcessor(Context context, TextView throttleText, TextView pitchText, TextView rollText){
        this.context = context;
        this.throttleText = throttleText; this.pitchText = pitchText; this.rollText = rollText;
    }

    // For logger
    private final String TAG = "ControlProcessor";

    // Global variables
    private enum CMD {
        ARM(10), THR(11), AIL(12), ELE(13), RUD(14), AUX(15), STP(20), MIN(16), SCD(17), SCU(18), MAX(19);

        private int numVal;

        CMD(int numVal) {
            this.numVal = numVal;
        }

        public int getNumVal() {
            return numVal;
        }

    };
    private int progressValue;
    private String DRONE_IP = "192.168.0.111";
    private Context context;
    private double pitchStrength, rollStrength;

    // ESC calibration values
    private final int KK_THROTTLE_MIN = 1150;
    private final int KK_THROTTLE_MAX = 1940;
    private final int KK_OTHERS_MIN = 1090;
    private final int KK_OTHERS_MID = 1500;
    private final int KK_OTHERS_MAX = 1910;

    // Pre-computed scale values
    private final double PRECOMPUTED_THROTTLE_SCALE = (KK_THROTTLE_MAX-KK_THROTTLE_MIN)/200.0;
    private final double PRECOMPUTED_OTHERS_SCALE_LEFT = (KK_OTHERS_MID-KK_OTHERS_MIN)/100.0;
    private final double PRECOMPUTED_OTHERS_SCALE_RIGHT = (KK_OTHERS_MAX-KK_OTHERS_MID)/100.0;
    private final double PRECOMPUTED_OTHERS_SCALE = (KK_OTHERS_MAX-KK_OTHERS_MIN)/100.0;

    protected void processThrottle(int strength, int angle){

        // Process strength based on angle
        if (strength>100) strength=100;
        if (angle==90){
            // 0 to 100
            strength += 100;
        } else if (angle==270){
            // 100 to 0
            strength = 100 - strength;
        }

        Log.v(TAG, strength+"");

        progressValue = (int)((strength*PRECOMPUTED_THROTTLE_SCALE)+KK_THROTTLE_MIN);
        Log.d("Throttle", progressValue+"");
        eventProcessor(CMD.THR, progressValue);
    }

    protected void processPitchAndRoll(int strength, double angle){

        if (strength==0 && angle==0){
//            eventProcessor(CMD.AIL, processGreaterThanMid(strength));
//            eventProcessor(CMD.ELE, processGreaterThanMid(strength));
            eventAxisProcessor(CMD.AIL, processGreaterThanMid(strength), CMD.ELE, processGreaterThanMid(strength));
        }

        if(strength>0) {
            if (angle == 0 || angle==360) {
                // Roll/AIL(MAX) right
                eventProcessor(CMD.AIL, processGreaterThanMid(strength));
            }
            else if (angle > 0 && angle < 90) {                    // First quadrant

                // Calc pitch and roll strength
                pitchStrength = Math.sin(angle)*strength;
                rollStrength = Math.cos(angle)*strength;

                // Roll/AIL(MAX) right + Pitch/ELE(MIN) forward
//                eventProcessor(CMD.AIL, processGreaterThanMid(rollStrength));
//                eventProcessor(CMD.ELE, processLessThanMid(pitchStrength));
                eventAxisProcessor(CMD.AIL, processGreaterThanMid(rollStrength), CMD.ELE, processLessThanMid(pitchStrength));
            }
            else if (angle == 90) {
                // Pitch/ELE(MIN) forward
                eventProcessor(CMD.ELE, processLessThanMid(strength));
            }
            else if (angle > 90 && angle < 180) {                 // Second quadrant
                // Calc pitch and roll strength
                angle = 180-angle;
                pitchStrength = Math.cos(angle)*strength;
                rollStrength = Math.sin(angle)*strength;

                // Roll/AIL(MIN) left + Pitch/ELE(MIN) forward
//                eventProcessor(CMD.AIL, processLessThanMid(rollStrength));
//                eventProcessor(CMD.ELE, processGreaterThanMid(pitchStrength));
                eventAxisProcessor(CMD.AIL, processLessThanMid(rollStrength), CMD.ELE, processGreaterThanMid(pitchStrength));
            }
            else if (angle == 180) {
                // Roll/AIL(MIN) left
                eventProcessor(CMD.AIL, processLessThanMid(strength));
            }
            else if (angle > 180 && angle < 270) {                 // Third quadrant
                // Calc pitch and roll strength
                angle = angle-180;
                pitchStrength = Math.sin(angle)*strength;
                rollStrength = Math.cos(angle)*strength;

                // Roll/AIL(MIN) left + Pitch/ELE(MAX) back
//                eventProcessor(CMD.AIL, processLessThanMid(rollStrength));
//                eventProcessor(CMD.ELE, processGreaterThanMid(pitchStrength));
                eventAxisProcessor(CMD.AIL, processLessThanMid(rollStrength), CMD.ELE, processGreaterThanMid(pitchStrength));
            }
            else if (angle == 270) {
                // Pitch/ELE(MAX) back
                eventProcessor(CMD.ELE, processGreaterThanMid(strength));
            }
            else if (angle > 270 && angle < 360) {                  // Fourth quadrant
                // Calc pitch and roll strength
                angle = 360-angle;
                pitchStrength = Math.cos(angle)*strength;
                rollStrength = Math.sin(angle)*strength;

                // Roll/AIL(MAX) right + Pitch/ELE(MAX) back
//                eventProcessor(CMD.AIL, processGreaterThanMid(rollStrength));
//                eventProcessor(CMD.ELE, processGreaterThanMid(pitchStrength));

                eventAxisProcessor(CMD.AIL, processGreaterThanMid(rollStrength), CMD.ELE, processGreaterThanMid(pitchStrength));
            }
            else {
                // No clue why this happened
                Log.e(TAG, "ERROR: While processing pitch and roll");
            }
        }

    }

    protected int processGreaterThanMid(double strength){
        progressValue = (int)((strength*PRECOMPUTED_OTHERS_SCALE_RIGHT)+KK_OTHERS_MID);
        Log.d("processGreaterThanMid", progressValue+"");
        return progressValue;
    }

    protected int processLessThanMid(double strength){
        progressValue = (int)(KK_OTHERS_MID-(strength*PRECOMPUTED_OTHERS_SCALE_LEFT));
        Log.d("processLessThanMid", progressValue+"");
        return progressValue;
    }

    protected void processAux(boolean desiredState){
        Log.v(TAG, "Process aux state : "+desiredState);
        if (desiredState){
            eventProcessor(CMD.AUX, KK_OTHERS_MAX);
        } else {
            eventProcessor(CMD.AUX, KK_OTHERS_MIN);
        }
    }

    protected void processArm(boolean desiredState){
        Log.v(TAG, "Process arm state : "+desiredState);
        if (desiredState){
            eventProcessor(CMD.ARM,0);
        } else {
            eventProcessor(CMD.STP,0);
        }
    }

    protected void ultraForceStop(){
        Log.v("ultraForceStop","Mayday Mayday Mayday");
        while(true) {
            eventProcessor(CMD.STP, 0);
        }
    }

    private void eventProcessor(CMD primary_cmd, int value){
        String baseUrl = "http://"+DRONE_IP+"/drone?cmd="+primary_cmd.getNumVal();
        if (!(primary_cmd==CMD.ARM || primary_cmd==CMD.STP)){
            baseUrl+="&mod=17";
            baseUrl+=("&val="+value);
        }

        volleyStringRequest(baseUrl);

        switch (primary_cmd){
            case AIL: rollText.setText("Roll: "+value);
                break;
            case ELE: pitchText.setText("Pitch: "+value);
                break;
            case THR: throttleText.setText("Throttle: "+value);
                break;
        }
    }

    private void eventAxisProcessor(CMD primary_cmd_1, int value_1, CMD primary_cmd_2, int value_2){
        String baseUrl_1 = "http://"+DRONE_IP+"/drone?cmd="+primary_cmd_1.getNumVal();
        if (!(primary_cmd_1==CMD.ARM || primary_cmd_1==CMD.STP)){
            baseUrl_1+="&mod=17";
            baseUrl_1+=("&val="+value_1);
        }
        String baseUrl_2 = "http://"+DRONE_IP+"/drone?cmd="+primary_cmd_2.getNumVal();
        if (!(primary_cmd_2==CMD.ARM || primary_cmd_2==CMD.STP)){
            baseUrl_2+="&mod=17";
            baseUrl_2+=("&val="+value_2);
        }

        volleyBatchRequest(baseUrl_1, baseUrl_2);

        switch (primary_cmd_1){
            case AIL: rollText.setText("Roll: "+value_1);
                break;
            case ELE: pitchText.setText("Pitch: "+value_1);
                break;
            case THR: throttleText.setText("Throttle: "+value_1);
                break;
        }

        switch (primary_cmd_2){
            case AIL: rollText.setText("Roll: "+value_2);
                break;
            case ELE: pitchText.setText("Pitch: "+value_2);
                break;
            case THR: throttleText.setText("Throttle: "+value_2);
                break;
        }
    }

    private void volleyStringRequest(String url){

        String  REQUEST_TAG_GENERAL = "GENERAL";

        HttpHandler.getInstance(context).cancelPendingRequests(REQUEST_TAG_GENERAL);

        StringRequest strReq = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });

        // Adding String request to request queue
        HttpHandler.getInstance(context).addToRequestQueue(strReq, REQUEST_TAG_GENERAL);

    }

    private void volleyBatchRequest(String url_1, String url_2){

        String  REQUEST_TAG_AXIS = "AXIS";

        HttpHandler.getInstance(context).cancelPendingRequests(REQUEST_TAG_AXIS);

        StringRequest strReq_1 = new StringRequest(url_1, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });

        StringRequest strReq_2 = new StringRequest(url_2, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });

        // Adding String request to request queue
        HttpHandler.getInstance(context).addToRequestQueue(strReq_1, REQUEST_TAG_AXIS);
        HttpHandler.getInstance(context).addToRequestQueue(strReq_2, REQUEST_TAG_AXIS);

    }

}
