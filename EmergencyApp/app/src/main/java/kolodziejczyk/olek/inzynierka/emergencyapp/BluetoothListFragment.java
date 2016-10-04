package kolodziejczyk.olek.inzynierka.emergencyapp;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothListFragment extends Fragment {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECTION = 2;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final int MESSAGE_READ = 9999;
    private static final int SUCCESS_CONNECT = 0;
    public static final String WHAT_TO_DO="kolodziejczyk.olek.inzynierka.emergencyapp.whattodo";
    public static final String SHARED_PREFS_MAC_ADDRESS="kolodziejczyk.olek.inzynierka.emergencyapp.SharedPrefsMac";
    private static final String TAG = "kolodziejczyk.olek";

    private BluetoothAdapter bluetoothAdapter=null;
    private boolean isConnected=false;
    private static String macAddress=null;
    private static String deviceName=null;

    private SharedPreferences sharedPreferencesMacAddress;
    private SharedPreferences.Editor macAddressEditor;

    BluetoothService myService;
    boolean isBound=false;

    public enum BtAction{GET_PAIRED,DISCOVER_NEW}

    @InjectView(R.id.button_paired)
    Button bConnectToPairedDevices;
    @InjectView(R.id.button_discover)
    Button bDiscoverNewDevices;
    @InjectView(R.id.button_confirmation)
    Button bConfirmation;
    @InjectView(R.id.text_view_device_information_name)
    TextView tvDeviceName;
    @InjectView(R.id.text_view_device_information_mac)
    TextView tvDeviceMac;
    @InjectView(R.id.text_view_bt_off_usage)
    TextView tvBtOff;

    public BluetoothListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentLayout=inflater.inflate(R.layout.fragment_bluetooth_list,container,false);
        ButterKnife.inject(this,fragmentLayout);

        sharedPreferencesMacAddress=getActivity().getSharedPreferences(EmergencyDetailActivity.SHARED_PREFS_FILENAME,0);
        macAddress=sharedPreferencesMacAddress.getString(BluetoothListFragment.SHARED_PREFS_MAC_ADDRESS,"null");
        tvDeviceMac.setText(macAddress);

        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        checkBtState();

        bConnectToPairedDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isConnected){
                    //connectThread.cancel();
                }else{
                    Intent intentList=new Intent(getActivity().getBaseContext(),BtDeviceList.class);
                    intentList.putExtra(BluetoothListFragment.WHAT_TO_DO,BtAction.GET_PAIRED);
                    startActivityForResult(intentList,REQUEST_CONNECTION);
                }
            }
        });

        bDiscoverNewDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentList=new Intent(getActivity().getBaseContext(),BtDeviceList.class);
                intentList.putExtra(BluetoothListFragment.WHAT_TO_DO,BtAction.DISCOVER_NEW);
                startActivityForResult(intentList,REQUEST_CONNECTION);
            }
        });

        bConfirmation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serviceIntent=new Intent(getActivity().getApplicationContext(), BluetoothService.class);
                serviceIntent.putExtra(BtDeviceList.MAC_ADDRESS,macAddress);
                getActivity().startService(serviceIntent);

                Intent intent = new Intent(getActivity().getApplicationContext(),EmergencyDetailActivity.class);
                intent.putExtra(EmergencyListActivity.FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.VIEW);
                intent.putExtra(HomeScreen.FIRST_RUN_EXTRA,true);
                startActivity(intent);
                getActivity().finish();
            }
        });
        tvBtOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(),EmergencyDetailActivity.class);
                intent.putExtra(EmergencyListActivity.FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.VIEW);
                intent.putExtra(HomeScreen.FIRST_RUN_EXTRA,true);
                startActivity(intent);
                getActivity().finish();
            }
        });
        return fragmentLayout;
    }

    private void checkBtState() {
        if(bluetoothAdapter==null){
            Toast.makeText(getActivity().getBaseContext(),R.string.lack_of_BTmodule,Toast.LENGTH_LONG).show();
            getActivity().finish();
        }else{
            if(!bluetoothAdapter.isEnabled()){
                turnOnBluetooth();
            }
        }
    }

    private void turnOnBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data); //should I comment this line?
        switch(requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode== Activity.RESULT_OK){
                    Toast.makeText(getActivity().getBaseContext(),"Bluetooth is activated!",Toast.LENGTH_SHORT).show();
                }else if(resultCode==Activity.RESULT_CANCELED){
                    Toast.makeText(getActivity().getBaseContext(),"Bluetooth should be enabled if you want to connect with remote device!",Toast.LENGTH_LONG).show();
                }else{
                    Log.i(TAG,"ENABLING FAIL: Else reason");
                    Toast.makeText(getActivity().getBaseContext(),"Bluetooth is NOT activated, try again",Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                break;
            case REQUEST_CONNECTION:
                if(resultCode==Activity.RESULT_OK){
                    Log.i(TAG,"RESULT OK");

                    macAddress=data.getExtras().getString(BtDeviceList.MAC_ADDRESS);
                    deviceName=data.getExtras().getString(BtDeviceList.DEVICE_NAME);
                    tvDeviceName.setText(deviceName);
                    tvDeviceMac.setText(macAddress);

                }else{
                    Log.i(TAG,"CONNECTING FAIL: Else reason");
                    Toast.makeText(getActivity().getBaseContext(),"Obtaining MAC address FAILED",Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

}