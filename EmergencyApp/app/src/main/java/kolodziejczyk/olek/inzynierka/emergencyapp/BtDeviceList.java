package kolodziejczyk.olek.inzynierka.emergencyapp;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

/**
 * Created by sasza_000 on 2016-09-17.
 */
public class BtDeviceList extends ListActivity{

    public static final String MAC_ADDRESS = "kolodziejczyk.olek.inzynierka.emergencyapp.MAC";
    public static final String DEVICE_NAME = "kolodziejczyk.olek.inzynierka.emergencyapp.NAME";
    private static final String RECIEVER_TAG = "reciever_tag";

    private String macAddress=null;
    private String deviceName=null;

    private ArrayAdapter<String> arrayAdapter=null;
    private BluetoothAdapter bluetoothAdapter=null;
    private BroadcastReceiver mReceiver=null;
    private IntentFilter filter=null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();

        Intent intent=getIntent();
        BluetoothListFragment.BtAction whatAction= (BluetoothListFragment.BtAction) intent.getSerializableExtra(BluetoothListFragment.WHAT_TO_DO);

        switch(whatAction) {
            case GET_PAIRED:
                getPairedDevices();
                break;
            case DISCOVER_NEW:
                getDiscoveredDevices();
                bluetoothAdapter.cancelDiscovery();
                bluetoothAdapter.startDiscovery();
                break;
        }

        setListAdapter(arrayAdapter);
    }

    private void getDiscoveredDevices() {
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    arrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        };
// Register the BroadcastReceiver
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
    }

    private void getPairedDevices() {
        Set<BluetoothDevice> pairedDevices=bluetoothAdapter.getBondedDevices();

        if(pairedDevices.size()>0){
            for(BluetoothDevice device:pairedDevices){
                String deviceName=device.getName();
                String deviceAddress=device.getAddress();
                arrayAdapter.add(deviceName+"\n"+deviceAddress);
            }
        }
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id){
        super.onListItemClick(listView,view,position,id);

        bluetoothAdapter.cancelDiscovery();
        String generalInfo=((TextView)view).getText().toString();
        macAddress=generalInfo.substring(generalInfo.length()-17); //podział na podciągi
        deviceName=generalInfo.substring(0,generalInfo.length()-17);

        returnInfo();
        finish();
    }

    private void returnInfo() {
        Intent intentMacReturn=new Intent();
        intentMacReturn.putExtra(MAC_ADDRESS,macAddress);
        intentMacReturn.putExtra(DEVICE_NAME,deviceName);
        setResult(RESULT_OK,intentMacReturn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(RECIEVER_TAG,"onDestroy");
        if(mReceiver!=null){
            unregisterReceiver(mReceiver);
            Log.i(RECIEVER_TAG,"mReciever destroyed");
        }
    }
}
