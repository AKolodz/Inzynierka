package kolodziejczyk.olek.inzynierka.emergencyapp;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import android.os.Handler;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothListFragment extends Fragment {

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter=null;

    public BluetoothListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentLayout=inflater.inflate(R.layout.fragment_bluetooth_list,container,false);
        ButterKnife.inject(this,fragmentLayout);

        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        checkBtState();


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
    public void onPause() {
        //getActivity().unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data); //should I comment this line?
        switch(requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode== Activity.RESULT_OK){
                    Toast.makeText(getActivity().getBaseContext(),"Bluetooth is activated!",Toast.LENGTH_SHORT).show();
                }else if(resultCode==Activity.RESULT_CANCELED){
                    Toast.makeText(getActivity().getBaseContext(),"Bluetooth must be enable!",Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }else{
                    Toast.makeText(getActivity().getBaseContext(),"Bluetooth is NOT activated, try again",Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                break;
        }

    }

}
