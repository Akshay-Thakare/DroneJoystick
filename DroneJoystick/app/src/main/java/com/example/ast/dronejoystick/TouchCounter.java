package com.example.ast.dronejoystick;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ast on 27/11/17.
 */

public class TouchCounter extends View {

    private final String TAG = "TouchCounter";

    public TouchCounter(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int numTouches = event.getPointerCount();
        Log.e(TAG, numTouches+"");

        return super.onTouchEvent(event);
    }
}
