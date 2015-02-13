/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */

package com.thalmic.android.sample.background;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;

public class BackgroundService extends Service {
    private static final String TAG = "BackgroundService";

    private Hub _hub;
    private Toast mToast;

    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {
        @Override
        public void onConnect(Myo myo, long timestamp) {
            showToast(getString(R.string.connected));
            _hub.detach(myo.getMacAddress());

        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            new Thread() {
                public void run() {
                    try

                    {
                        Thread.sleep(10000);
                        showToast("Ready");
                        Thread.sleep(3000);
                    } catch (
                            Exception e
                            )

                    {

                    }

                    _hub.attachToAdjacentMyo();
                }
            }.start();
        }

        // onPose() is called whenever the Myo detects that the person wearing it has changed their pose, for example,
        // making a fist, or not making a fist anymore.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Show the name of the pose in a toast.
            //showToast(getString(R.string.pose, pose.toString()));

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // First, we initialize the Hub singleton with an application identifier.
        _hub = Hub.getInstance();
        if (!_hub.init(this, getPackageName())) {
            showToast("Couldn't initialize Hub");
            stopSelf();
            return;
        }

        // Disable standard Myo locking policy. All poses will be delivered.
        _hub.setLockingPolicy(Hub.LockingPolicy.NONE);

        // Next, register for DeviceListener callbacks.
        _hub.addListener(mListener);

        showToast("Myo Background Online!");

        // Finally, scan for Myo devices and connect to the first one found that is very near.
        _hub.attachToAdjacentMyo();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // We don't want any callbacks when the Service is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);

        Hub.getInstance().shutdown();
    }

    private void showToast(String text) {
        Log.w(TAG, text);
        if (mToast == null) {
            mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }
        mToast.show();
    }
}
