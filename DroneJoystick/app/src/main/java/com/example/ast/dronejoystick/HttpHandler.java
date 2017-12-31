package com.example.ast.dronejoystick;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by ast on 27/11/17.
 */

public class HttpHandler {
    private static HttpHandler mSingletonInstance;
    private RequestQueue mRequestQueue;
    private static Context mContext;

    private HttpHandler(Context context){
        mContext = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized HttpHandler getInstance(Context context) {
        if (mSingletonInstance == null) {
            mSingletonInstance = new HttpHandler(context);
        }
        return mSingletonInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(tag);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

}
