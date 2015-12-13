
/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.nordicsemi.nrfUARTv2;



import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


import com.nordicsemi.nrfUARTv2.UartService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.Image;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener{
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    //private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect;

    private GridviewAdapter mAdapter;
    private ArrayList<String> listInfo;
    private ArrayList<Integer> listIcon;

    private GridView gridView;

    private String weight="";
    int open;
    int damage;
    int spillage;
    String lat="1.3401";
    String lng="103.9630";
    int checkOpen=0;
    int checkDamage=0;
    int checkSpillage=0;
    public final static String EXTRA_LAT = "com.nordicsemi.nrfUARTv2.EXTRA_LAT";
    public final static String EXTRA_LNG = "com.nordicsemi.nrfUARTv2.EXTRA_LNG";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        prepareList();


        // prepared arraylist and passed it to the Adapter class
        mAdapter = new GridviewAdapter(this, listInfo, listIcon);

        // Set custom adapter to gridview
        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(mAdapter);

//        // Implement On Item click listener
//        gridView.setOnItemClickListener(new OnItemClickListener()
//        {
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
//                                    long arg3) {
//                Toast.makeText(GridViewExampleActivity.this, mAdapter.getItem(position), Toast.LENGTH_SHORT).show();
//            }
//        });

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        //GridView messageListView = (GridView) findViewById(R.id.gridView);
        //listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        //messageListView.setAdapter(listAdapter);

        ImageButton showMap = (ImageButton) findViewById(R.id.imageButton);
        showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    intent.putExtra(EXTRA_LAT, lat);
                    intent.putExtra(EXTRA_LNG, lng);
                    startActivity(intent);
                } catch (NullPointerException ex){
                    Toast.makeText(getApplicationContext(),"Arduino is not connected.",Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnConnectDisconnect = (Button) findViewById(R.id.btn_select);
        service_init();


        // Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    if (btnConnectDisconnect.getText().equals("Connect")) {

                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    } else {
                        //Disconnect button pressed
                        if (mDevice != null) {
                            mService.disconnect();

                        }
                    }
                }
            }
        });
        // Set initial UI state

    }

    public void prepareList() {
        listInfo = new ArrayList<String>();

        listInfo.add("india");
        listInfo.add("Brazil");
        listInfo.add("Canada");
        listInfo.add("China");


        listIcon = new ArrayList<Integer>();

        listIcon.add(R.drawable.closed);
        listIcon.add(R.drawable.damage_ok);
        listIcon.add(R.drawable.spillage_ok);
        listIcon.add(R.drawable.weight_check);

    }



    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
        		mService = ((UartService.LocalBinder) rawBinder).getService();
        		Log.d(TAG, "onServiceConnected mService= " + mService);
        		if (!mService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }

        }

        public void onServiceDisconnected(ComponentName classname) {
       ////     mService.disconnect(mDevice);
        		mService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        
        //Handler events that received from UART service 
        public void handleMessage(Message msg) {
  
        }
    };





    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
           //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.d(TAG, "UART_CONNECT_MSG");
                             btnConnectDisconnect.setText("Disconnect");
                          ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - ready");
                             //listAdapter.clear();
                             //listAdapter.add("[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                        	 	//messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                             mState = UART_PROFILE_CONNECTED;
                     }
            	 });
            }
           
          //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                    	 	 String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.d(TAG, "UART_DISCONNECT_MSG");
                             btnConnectDisconnect.setText("Connect");
                             ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                             //listAdapter.clear();
                             //listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                             mState = UART_PROFILE_DISCONNECTED;
                             mService.close();
                            //setUiState();
                         
                     }
                 });
            }
            
          
          //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
             	 mService.enableTXNotification();
            }
          //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
              
                 final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                 runOnUiThread(new Runnable() {
                     public void run() {
                         try {
                             String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             String text = new String(txValue, "UTF-8");
                         	//String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             String[] statuses = text.split(" ");
                             listIcon.clear();
                             if (statuses[0].charAt(0)=='0'){
                                 open = R.drawable.closed;
                             } else {
                                 open = R.drawable.opened;
                                 if (checkOpen == 0){
                                     NotificationCompat.Builder mBuilder =
                                             new NotificationCompat.Builder(getApplicationContext())
                                                     .setSmallIcon(R.drawable.nrfuart_hdpi_icon)
                                                     .setVisibility(Notification.VISIBILITY_PUBLIC)
                                                     .setContentTitle("Opened already")
                                                     .setContentText("At: "+currentDateTimeString);
                                        // Sets an ID for the notification
                                     int mNotificationId = 001;
                                        // Gets an instance of the NotificationManager service
                                     NotificationManager mNotifyMgr =
                                             (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                        // Builds the notification and issues it.
                                     mNotifyMgr.notify(mNotificationId, mBuilder.build());
                                     Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                     v.vibrate(500);
                                     checkOpen += 1;
                                 }
                             }
                             if (statuses[0].charAt(1) =='0') {
                                 damage = R.drawable.damage_ok;
                             } else {
                                 damage = R.drawable.damage_notok;
                                 if (checkDamage == 0) {
                                     NotificationCompat.Builder mBuilder =
                                             new NotificationCompat.Builder(getApplicationContext())
                                                     .setSmallIcon(R.drawable.nrfuart_hdpi_icon)
                                                     .setVisibility(Notification.VISIBILITY_PUBLIC)
                                                     .setContentTitle("Damaged already")
                                                     .setContentText("At: "+currentDateTimeString);
                                     // Sets an ID for the notification
                                     int mNotificationId = 001;
                                     // Gets an instance of the NotificationManager service
                                     NotificationManager mNotifyMgr =
                                             (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                     // Builds the notification and issues it.
                                     mNotifyMgr.notify(mNotificationId, mBuilder.build());
                                     Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                     v.vibrate(500);
                                     checkDamage += 1;
                                 }
                             }
                             if (statuses[0].charAt(2) =='0') {
                                 spillage = R.drawable.spillage_ok;
                             } else {
                                 spillage = R.drawable.spillage_notok;
                                 if (checkSpillage == 0) {
                                     NotificationCompat.Builder mBuilder =
                                             new NotificationCompat.Builder(getApplicationContext())
                                                     .setSmallIcon(R.drawable.nrfuart_hdpi_icon)
                                                     .setVisibility(Notification.VISIBILITY_PUBLIC)
                                                     .setContentTitle("Damaged already")
                                                     .setContentText("At: "+currentDateTimeString);
                                     // Sets an ID for the notification
                                     int mNotificationId = 001;
                                     // Gets an instance of the NotificationManager service
                                     NotificationManager mNotifyMgr =
                                             (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                     // Builds the notification and issues it.
                                     mNotifyMgr.notify(mNotificationId, mBuilder.build());
                                     Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                     v.vibrate(500);
                                     checkSpillage += 1;
                                 }
                             }
                             weight = statuses[0].substring(3,statuses[0].length())+"\n";
                             lat = statuses[1];
                             lng = statuses[2];

                             listIcon.add(open);
                             listIcon.add(damage);
                             listIcon.add(spillage);
                             listIcon.add(R.drawable.weight_check);
                             gridView.setAdapter(mAdapter);

                         } catch (Exception e) {
                             Log.e(TAG, e.toString());
                         }
                     }
                 });
             }
           //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
            	showMessage("Device doesn't support UART. Disconnecting");
            	mService.disconnect();
            }
        }
    };


    public void weight(View view){
        Toast.makeText(this, "Weight is " + weight + " kg.", Toast.LENGTH_SHORT).show();
        final Button  a = (Button) findViewById(R.id.weight);
        a.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    a.setBackgroundColor(0x10000000);
                    return false;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    a.setBackgroundColor(0x00000000);
                    return false;
                }

                return false;
            }
        });
    }

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
  
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        
        try {
        	LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        } 
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;
       
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
 
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

        case REQUEST_SELECT_DEVICE:
        	//When the DeviceListActivity return, with the selected device address
            if (resultCode == Activity.RESULT_OK && data != null) {
                String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
               
                Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                mService.connect(deviceAddress);
                            

            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        default:
            Log.e(TAG, "wrong request code");
            break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
       
    }

    
    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
  
    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.popup_title)
            .setMessage(R.string.popup_message)
            .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
   	                finish();
                }
            })
            .setNegativeButton(R.string.popup_no, null)
            .show();
        }
    }
}
