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
import java.util.Date;


import com.nordicsemi.nrfUARTv2.UartService;

import android.os.CountDownTimer;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;



import static android.widget.CompoundButton.*;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    public static final String TAG = "nRFUART";
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    //private ListView messageListView;
    //private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect, btnVibration, btnStop;
    private ToggleButton btnEffect1, btnEffect2, btnEffect3, btnEffect4, btnEffect5,
            btnEffect6, btnMusic1, btnMusic2, btnMusic3;
    private MyCountDownTimer countDownTimer=null;

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
                        //edtMessage.setEnabled(true);
                        btnVibration.setEnabled(true);
                        btnStop.setEnabled(true);
                        btnEffect1.setEnabled(true);
                        btnEffect2.setEnabled(true);
                        btnEffect3.setEnabled(true);
                        btnEffect4.setEnabled(true);
                        btnEffect5.setEnabled(true);
                        btnEffect6.setEnabled(true);
                        btnMusic1.setEnabled(true);
                        btnMusic2.setEnabled(true);
                        btnMusic3.setEnabled(true);

                        ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - ready");
                        //listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                        //	messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
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
                        //edtMessage.setEnabled(false);
                        btnVibration.setEnabled(false);
                        btnStop.setEnabled(false);
                        btnEffect1.setEnabled(false);
                        btnEffect2.setEnabled(false);
                        btnEffect3.setEnabled(false);
                        btnEffect4.setEnabled(false);
                        btnEffect5.setEnabled(false);
                        btnEffect6.setEnabled(false);
                        btnMusic1.setEnabled(false);
                        btnMusic2.setEnabled(false);
                        btnMusic3.setEnabled(false);

                        ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
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
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            //listAdapter.add("["+currentDateTimeString+"] RX: "+text);
                            //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);

                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }


        }
    };

    private EditText edtMessage;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //messageListView = (ListView) findViewById(R.id.listMessage);
        //listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        //messageListView.setAdapter(listAdapter);
        //messageListView.setDivider(null);
        btnConnectDisconnect = (Button) findViewById(R.id.btn_select);
        btnEffect1 = (ToggleButton) findViewById(R.id.Effect1Button);
        btnEffect2 = (ToggleButton) findViewById(R.id.Effect2Button);
        btnEffect3 = (ToggleButton) findViewById(R.id.Effect3Button);
        btnEffect4 = (ToggleButton) findViewById(R.id.Effect4Button);
        btnEffect5 = (ToggleButton) findViewById(R.id.Effect5Button);
        btnEffect6 = (ToggleButton) findViewById(R.id.Effect6Button);
        btnVibration = (Button) findViewById(R.id.VibrationButton);
        btnStop = (Button) findViewById(R.id.StopButton);
        btnMusic1 = (ToggleButton) findViewById(R.id.Music1Button);
        btnMusic2 = (ToggleButton) findViewById(R.id.Music2Button);
        btnMusic3 = (ToggleButton) findViewById(R.id.Music3Button);
        btnEffect1.setChecked(false);
        btnEffect2.setChecked(false);
        btnEffect3.setChecked(false);
        btnEffect4.setChecked(false);
        btnEffect5.setChecked(false);
        btnEffect6.setChecked(false);
        btnMusic1.setChecked(false);
        btnMusic2.setChecked(false);
        btnMusic3.setChecked(false);
        countDownTimer=new MyCountDownTimer(10000, 1000);
        //edtMessage = (EditText) fibtnMusic1.setChecked(false);ndViewById(R.id.sendText);
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
                    if (btnConnectDisconnect.getText().equals("CONECTAR")) {

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
        // Handle Effect1 button
        btnEffect1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnEffect2.setChecked(false);
                    btnEffect3.setChecked(false);
                    btnEffect4.setChecked(false);
                    btnEffect5.setChecked(false);
                    btnEffect6.setChecked(false);
                    //EditText editText = (EditText) findViewById(R.id.Effect1Button);
                    String message = "[2]";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        //listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        //edtMessage.setText("");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {
                }
            }
        });
        // Handle Effect1 button
        btnEffect2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnEffect1.setChecked(false);
                    btnEffect3.setChecked(false);
                    btnEffect4.setChecked(false);
                    btnEffect5.setChecked(false);
                    btnEffect6.setChecked(false);
                    //EditText editText = (EditText) findViewById(R.id.Effect1Button);
                    String message = "[3]";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        //listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        //edtMessage.setText("");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {

                }
            }
        });
        // Handle Effect1 button
        btnEffect3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnEffect1.setChecked(false);
                    btnEffect2.setChecked(false);
                    btnEffect4.setChecked(false);
                    btnEffect5.setChecked(false);
                    btnEffect6.setChecked(false);
                    //EditText editText = (EditText) findViewById(R.id.Effect1Button);
                    String message = "[4]";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        //listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        //edtMessage.setText("");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {

                }
            }
        });
        // Handle Effect1 button
        btnEffect4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnEffect1.setChecked(false);
                    btnEffect2.setChecked(false);
                    btnEffect3.setChecked(false);
                    btnEffect5.setChecked(false);
                    btnEffect6.setChecked(false);
                    //EditText editText = (EditText) findViewById(R.id.Effect1Button);
                    String message = "[5]";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        //listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        //edtMessage.setText("");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {

                }
            }
        });
        // Handle Effect1 button
        btnEffect5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnEffect1.setChecked(false);
                    btnEffect2.setChecked(false);
                    btnEffect3.setChecked(false);
                    btnEffect4.setChecked(false);
                    btnEffect6.setChecked(false);
                    //EditText editText = (EditText) findViewById(R.id.Effect1Button);
                    String message = "[6]";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        //listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        //edtMessage.setText("");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {

                }
            }
        });
        // Handle Effect1 button
        btnEffect6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnEffect1.setChecked(false);
                    btnEffect2.setChecked(false);
                    btnEffect3.setChecked(false);
                    btnEffect4.setChecked(false);
                    btnEffect5.setChecked(false);
                    //EditText editText = (EditText) findViewById(R.id.Effect1Button);
                    String message = "[7]";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        //listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        //edtMessage.setText("");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {

                }
            }
        });

        btnVibration.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                //EditText editText = (EditText) findViewById(R.id.PlayButton);
                String message = "[B]";
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    //listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    //edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        btnStop.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                btnEffect1.setChecked(false);
                btnEffect2.setChecked(false);
                btnEffect3.setChecked(false);
                btnEffect4.setChecked(false);
                btnEffect5.setChecked(false);
                btnEffect6.setChecked(false);
                //EditText editText = (EditText) findViewById(R.id.PlayButton);
                String message = "[0]";
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    //listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    //edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        btnMusic1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnMusic2.setChecked(false);
                    btnMusic3.setChecked(false);
                    countDownTimer.start(); //Automatically set checked btnMusic1 to false
                    //EditText editText = (EditText) findViewById(R.id.PlayButton);
                    String message = "[8]";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        //listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        //edtMessage.setText("");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {

                }
            }
        });
        btnMusic2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnMusic1.setChecked(false);
                    btnMusic3.setChecked(false);
                    countDownTimer.start(); //Automatically set checked btnMusic2 to false
                    //EditText editText = (EditText) findViewById(R.id.PlayButton);
                    String message = "[9]";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        //listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        //edtMessage.setText("");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {

                }
            }
        });


        btnMusic3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnMusic1.setChecked(false);
                    btnMusic2.setChecked(false);
                    countDownTimer.start(); //Automatically set checked btnMusic3 to false
                    //EditText editText = (EditText) findViewById(R.id.PlayButton);
                    String message = "[A]";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        //listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        //edtMessage.setText("");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {

                }
            }
        });
        // Set initial UI state

    }

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
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
        mService = null;

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
                    ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - connecting");
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
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.popup_no, null)
                    .show();
        }
    }


    public class MyCountDownTimer extends CountDownTimer {
        private long starttime;
        private boolean isrunning = false;

        public MyCountDownTimer(long startTime, long interval) {

            super(startTime, interval);
            this.starttime = startTime;
        }
        @Override
        public void onTick(long millisUntilFinished)
        {
            //do nothing
        }


        @Override
        public void onFinish() {
            btnMusic1.setChecked(false);
            btnMusic2.setChecked(false);
            btnMusic3.setChecked(false);

        }


    }
}

