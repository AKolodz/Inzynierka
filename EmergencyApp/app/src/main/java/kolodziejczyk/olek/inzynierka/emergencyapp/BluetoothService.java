package kolodziejczyk.olek.inzynierka.emergencyapp;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.UUID;

public class BluetoothService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String USERS_MESSAGE = "kolodziejczyk.olek.inzynierka.emergencyapp.UsersMessage";
    public static final String USERS_NUMBER = "kolodziejczyk.olek.inzynierka.emergencyapp.UsersNumber";

    private final IBinder myBinder=new MyBinder();
    private BluetoothAdapter bluetoothAdapter=null;
    private static String macAddress=null;
    private BluetoothDevice deviceToConnectWith;
    private ConnectThread connectThread=null;
    private ConnectedThread connectedThread=null;
    private SharedPreferences sharedPreferencesMacAddress;
    private SharedPreferences.Editor macAddressEditor;

    private static final int REQUEST_CHECK_SETTINGS = 1000;
    private GoogleApiClient mGoogleApiClient = null;
    private LocationRequest mLocationRequest=null;
    private Location mLastLocation=null;
    private PendingResult<LocationSettingsResult> result = null;
    private static int UPDATE_INTERVAL=10000;
    private static int FASTEST_INTERVAL=5000;
    private static int DISPLACEMENT=10;
    private static long TIME_FOR_LOCATION_UPDATE=30000;
    private double latitude;
    private double longitude;
    private String emergencyNumber;
    private String emergencyMessage;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public static final String TAG = "kolodziejczyk.olek";
    //private final String defaultMac="20:15:12:29:63:33";
    private final String noSavedMac="lackOfMac";
    public static final String TAG_BUFFER = "buffer";
    public static final String SHARED_PREFS_MAC_ADDRESS="kolodziejczyk.olek.inzynierka.emergencyapp.SharedPrefsMac";

    private static final int SUCCESS_CONNECT = 0;
    private static final int UNSUCCESS_CONNECT = 11;
    protected static final int MESSAGE_READ = 9999;

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){

                case SUCCESS_CONNECT:
                    Log.i(TAG,"Handler: SUCCESS CONNECT");
                    sharedPreferencesMacAddress=getSharedPreferences(EmergencyDetailActivity.SHARED_PREFS_FILENAME,0);
                    macAddressEditor=sharedPreferencesMacAddress.edit();
                    macAddressEditor.putString(BluetoothListFragment.SHARED_PREFS_MAC_ADDRESS,macAddress);
                    macAddressEditor.commit();
                    break;

                case UNSUCCESS_CONNECT:
                    Log.i(TAG,"Handler: Connection Failed");
                    Toast.makeText(getApplicationContext(),"Device/MAC address invalid: "+macAddress,Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(getApplicationContext(),BluetoothListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    stopSelf();
                    break;

                case MESSAGE_READ:
                    Log.i(TAG_BUFFER, "HANDLER: "+ msg.obj);

                    byte[] readBuff=(byte[]) msg.obj;
                    String receivedMsg=new String(readBuff);

                    if(!receivedMsg.equals(null)) {
                        if(checkGPState()){
                            updateLocationAndSendMessage();
                        }else{
                            Log.i(TAG,"MESSAGE: GPS OFF");
                            sendMessageWithoutUpdatedLocation();
                        }
                    }
                    break;
            }
        }
    };

    public BluetoothService() {
    }

    @Override
    public void onCreate() {
        Log.i(TAG,"OnCreate SERVICE");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"OnStartCommand SERVICE");

        deviceToConnectWith=null; //has to be here to work correctly after changing working mode from "with external device" to "without"
        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();

        getMacAndExtrasFromViewFragment(intent);

        Log.i(TAG,"SERVICE Mac: "+macAddress);
        Log.i(TAG,"SERVICE Message: "+emergencyMessage);
        Log.i(TAG,"SERVICE Number: "+emergencyNumber);

        checkMacAddressAndGetVirtualBtDevice();

        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"New Thread");
                if(deviceToConnectWith!=null){  //if there is representation of device that we can connect with
                    connectThread=new ConnectThread(deviceToConnectWith); //try to make a connection
                    connectThread.start();
                }

                if(checkPlayServices()){
                    buildGoogleApiClient();
                    createLocationRequest();
                }

                if (mGoogleApiClient != null) {
                    mGoogleApiClient.connect();
                    Log.i(TAG,"Connect Client");
                }
            }
        };
        Thread thread=new Thread(runnable);
        thread.start();

        return Service.START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy()");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        connectedThread.cancel();
        stopSelf();
    }

    private void getMacAndExtrasFromViewFragment(Intent intent) {
        macAddress=intent.getExtras().getString(BtDeviceList.MAC_ADDRESS); //get MAC Address send from BluetoothListFragment
        emergencyNumber=intent.getExtras().getString(BluetoothService.USERS_NUMBER);
        emergencyMessage = intent.getExtras().getString(BluetoothService.USERS_MESSAGE);
    }

    private void checkMacAddressAndGetVirtualBtDevice() {
        if(macAddress==null || macAddress.equals("no-address")){    //if nothing was sent from BluetoothListFragment
            loadMacFromSharedPrefs();
            Log.i(TAG,"onStartCommand "+macAddress);

            if(macAddress==null){               //if still (after updating info from saved xml) we do not have correct MAC
                Log.i(TAG,"Saved macAddress is incorrect "+macAddress);
                mHandler.obtainMessage(UNSUCCESS_CONNECT).sendToTarget();
            }
        }else if(!macAddress.equals("empty")){   //if Confirm button was the user's choice
            Log.i(TAG,"macAddress not 'empty' ");
            deviceToConnectWith=bluetoothAdapter.getRemoteDevice(macAddress);   //create representation of BT device based on MAC Address
            bluetoothAdapter.cancelDiscovery();
        }else if(macAddress.equals("nul")){
            Log.i(TAG,"nul MAC");
        }
    }

    private void loadMacFromSharedPrefs() {
        sharedPreferencesMacAddress=getSharedPreferences(EmergencyDetailActivity.SHARED_PREFS_FILENAME,0);
        macAddress=sharedPreferencesMacAddress.getString(BluetoothService.SHARED_PREFS_MAC_ADDRESS,null);
    }

    protected void createLocationRequest() {
        mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private boolean checkPlayServices() {
        int resultCode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode!= ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                Toast.makeText(getApplicationContext(),"You must have GooglePlayServices App on your phone",Toast.LENGTH_LONG).show();
                Log.i(TAG,"Recoverable error occured: "+resultCode);
            }else{
                Log.i(TAG,"This device doesn't support GPS");
            }
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(Bundle bundle) {
        getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected void sendMessageWithoutUpdatedLocation() {
        getLocation();
        sendSMS();
    }

    protected PendingResult<LocationSettingsResult> getSettingsResult(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        return result;
    }


    protected void updateLocationAndSendMessage() {
        longitude=0;
        latitude=0;

        startLocationUpdates();

        Runnable run=new Runnable() {
            @Override
            public void run() {
                stopLocationUpdate();
                getLocation();
                showLocation();
                sendSMS();
        }
        };
        mHandler.postDelayed(run,TIME_FOR_LOCATION_UPDATE);
    }

    protected void sendSMS() {
        SmsManager smsManager=SmsManager.getDefault();
        smsManager.sendTextMessage(emergencyNumber,null,emergencyMessage,null,null);

        if(checkGPState()){
            smsManager.sendTextMessage(emergencyNumber,null,"My present location:\nLATITUDE: "+latitude+"\nLONGITUDE: "+longitude,null,null);
        }else{
            smsManager.sendTextMessage(emergencyNumber,null,"My last known location:\nLATITUDE: "+latitude+"\nLONGITUDE: "+longitude,null,null);
        }

        //podzielone ze względu na pojawiający się błąd Android  java.lang.SecurityException: Requires READ_PHONE_STATE: Neither user 10042 nor current process has android.permission.READ_PHONE_STATE.
        Log.i(TAG,"Sms");
}

    private void getLocation() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            latitude= mLastLocation.getLatitude();
            longitude=mLastLocation.getLongitude();
            Log.i(TAG,latitude +"\t"+longitude);

        }else{
            Log.i(TAG,"Couldn't get the location, make sure that GPS is enabled");
        }
    }

    protected boolean checkGPState() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.i(TAG,"GPS OFF");
            return false;
        }else{
            Log.i(TAG,"GPS ON");
            return true;
        }
    }

    protected void showLocation() {
        Toast.makeText(getApplicationContext(),latitude +"\t"+longitude,Toast.LENGTH_SHORT).show();
    }

    private void stopLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder {
        BluetoothService getService(){
            return BluetoothService.this;
        }
    }

                                                /* LOCAL CLASSES*/

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                Log.i(TAG,"ConnectThread(run) Connecting");
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    Log.i(TAG,"ConnectThread(run) Connection Failure");
                    mHandler.obtainMessage(UNSUCCESS_CONNECT).sendToTarget();
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }
            // Do work to manage the connection (in a separate thread)
            mHandler.obtainMessage(SUCCESS_CONNECT).sendToTarget();
            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                Toast.makeText(getBaseContext(),"Device disconnected",Toast.LENGTH_SHORT).show();
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private void manageConnectedSocket(BluetoothSocket mmSocket) {
        connectedThread=new ConnectedThread(mmSocket);
        connectedThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                Log.i(TAG_BUFFER, "WHILE");

                try {
                    // Read from the InputStream
                    //The number of bytes actually read is returned as an integer. This method blocks until input data is available, end of file is detected, or an exception is thrown.
                    bytes = mmInStream.read(buffer);
                    Log.i(TAG_BUFFER, "RUN bytes: "+String.valueOf(bytes));
                    Log.i(TAG_BUFFER, "RUN buffer: "+String.valueOf(buffer));
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                    /*if(true){

                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget();
                    }*/
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
