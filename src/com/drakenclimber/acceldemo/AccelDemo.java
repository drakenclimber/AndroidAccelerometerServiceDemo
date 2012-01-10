/*******************************************************************************
*
*  Copyright 2011 Drakenclimber, LLC.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
********************************************************************************
* Revision information.  DO NOT MODIFY.  This is auto-generated by the SCM.
*
* $Date: 2011-11-14 21:38:18 -0600 (Mon, 14 Nov 2011) $
* $Revision: 14 $
*
*******************************************************************************/
/**
 * Doxygen comment block
 * @file
 *
 * @brief   Android Demo Accelerometer Activity Class
 *
 */

package com.drakenclimber.acceldemo;

/*******************************************************************************
 * Imports
 ******************************************************************************/
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.drakenclimber.accelservice.AccelerometerService;
import com.drakenclimber.accelservice.IAccelerometerListener;
import com.drakenclimber.accelservice.IAccelerometerMonitor;

/*******************************************************************************
 * Classes
 ******************************************************************************/
public class AccelDemo extends Activity {
    /***************************************************************************
     * Final Variables
     **************************************************************************/
    static final String TAG = "AccelDemo";

    /***************************************************************************
     * Public Variables
     **************************************************************************/

    /***************************************************************************
     * Private Classes
     **************************************************************************/
    private class RetainedData {
        public boolean                  mRetainedServiceIsBound;
        public Intent                   mRetainedIntent;
    }

    /***************************************************************************
     * Private Variables
     **************************************************************************/
    /* widgets */
    private Button      mStartButton        = null;
    private Button      mStopButton         = null;
    private TextView    mXText              = null;
    private TextView    mYText              = null;
    private TextView    mZText              = null;

    /* variables for working with the accelerometer service */
    private Intent      mIntent             = null;
    private boolean     mServiceIsBound     = false;
    private IAccelerometerListener mAccelListener = new IAccelerometerListener() {
        public void onDataReceived(SensorEvent event) {
            Log.d(TAG, String.format("Accel, x %3.1f, y %3.1f, z %3.1f",
                    event.values[0], event.values[1], event.values[2]));

            updateDataFields(event.values[0], event.values[1], event.values[2]);
        }
    };

    private IAccelerometerMonitor mAccelService = null;
    private ServiceConnection mAccelServiceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            mAccelService = (IAccelerometerMonitor) binder;

            try {
                mAccelService.registerListener(mAccelListener);
                Log.d(TAG, "Listener registered.");

            } catch (Throwable t) {
                Log.e(TAG, "Exception in call to registerListener()", t);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mAccelService = null;
        }
    };

    /***************************************************************************
     * Methods
     **************************************************************************/

    /** method to handle the creation of the accelerometer demo activity */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accel_demo_layout);

        mXText = (TextView) findViewById(R.id.WidgetXAccel);
        mYText = (TextView) findViewById(R.id.WidgetYAccel);
        mZText = (TextView) findViewById(R.id.WidgetZAccel);
        
        RetainedData data = (RetainedData) getLastNonConfigurationInstance();
        if (data == null) {
            /* we are starting up for the first time.  start the accel service */
            mIntent = new Intent(AccelDemo.this, AccelerometerService.class);
            
            startService(mIntent);
        }
        else {
            /* we are re-starting, likely due to an orientation change */
            restoreRetainedData(data);

            if(mServiceIsBound) {
                bindToAccel();                
            }
            
        }

        /***********************************************************************
         * Start Button Methods
         **********************************************************************/
        mStartButton = (Button) findViewById(R.id.WidgetStartButton);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bindToAccel();
                Log.d(TAG, "Start button pressed.");

            } /* onClick() */
        }); /* startButton.setOnClickListener() */

        /***********************************************************************
         * Stop Button Methods
         **********************************************************************/
        mStopButton = (Button) findViewById(R.id.WidgetStopButton);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                unbindFromAccel();
                Log.d(TAG, "Stop button pressed.");

            } /* onClick() */
        }); /* stopButton.setOnClickListener() */
    }; /* onCreate() */

    /** method to handle the destruction of this activity */
    @Override
    public void onDestroy() {
        if(mServiceIsBound) {
            unbindFromAccel();
        }
        
        if (isFinishing()) {
            stopService(mIntent);

        }

        super.onDestroy();
    }

    /** method to handle the restart of this activity */
    @Override
    public Object onRetainNonConfigurationInstance() {
        final RetainedData myRetainedData = setRetainedData();
        return myRetainedData;
    }

    /**
     * Method to store data between creates and destroys (typically orientation
     * changes)
     */
    private RetainedData setRetainedData() {
        RetainedData data = new RetainedData();

        data.mRetainedServiceIsBound = mServiceIsBound;
        data.mRetainedIntent         = mIntent;

        return data;
    }

    /** Method to restore stored data */
    private void restoreRetainedData(RetainedData data) {
        mServiceIsBound = data.mRetainedServiceIsBound;
        mIntent         = data.mRetainedIntent;

    } /* restoreRetainedData() */

    private void bindToAccel() {
        mServiceIsBound = true;
        bindService(new Intent(AccelDemo.this, AccelerometerService.class),
                mAccelServiceConn, BIND_AUTO_CREATE);
        
    } /* bindToAccel() */

    private void unbindFromAccel() {
        try {
            mServiceIsBound = false;
            mAccelService.unregisterListener(mAccelListener);
            unbindService(mAccelServiceConn);

        } catch (Throwable t) {
            Log.d(TAG, "Unregistration of the service failed");

        }

    } /* unbindFromAccel() */
    
    
    private void updateDataFields(float xAccel, float yAccel, float zAccel) {
        mXText.setText(String.format("X: %5.2f m/s^2", xAccel));
        mYText.setText(String.format("Y: %5.2f m/s^2", yAccel));
        mZText.setText(String.format("Z: %5.2f m/s^2", zAccel));
        
    }   /* updateDataFields() */

} /* AccelDemo() class */
