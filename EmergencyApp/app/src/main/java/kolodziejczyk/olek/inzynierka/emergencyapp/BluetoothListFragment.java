package kolodziejczyk.olek.inzynierka.emergencyapp;


import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothListFragment extends Fragment {


    @InjectView(R.id.switch_bluetooth_on_off)
    Switch sTurnOnOff;
    @InjectView(R.id.button_connection_new)
    Button bConnect;
    @InjectView(R.id.list_view_BT_devices)
    ListView lvDevices;

    ArrayAdapter<String> devicesListAdapter;
    BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;

    public BluetoothListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentLayout=inflater.inflate(R.layout.fragment_bluetooth_list,container,false);

        ButterKnife.inject(this,fragmentLayout);
        devicesListAdapter=new ArrayAdapter<String>(getActivity().getBaseContext(),android.R.layout.simple_list_item_1,0);
        lvDevices.setAdapter(devicesListAdapter);
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter==null){
            Toast.makeText(getActivity().getBaseContext(),"No bluetooth detected",Toast.LENGTH_SHORT).show();
        }else{
            if(!bluetoothAdapter.isEnabled()){
                Intent enableBtIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
            }
        }
        return fragmentLayout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == getActivity().RESULT_CANCELED){
            Toast.makeText(getActivity().getBaseContext(),"Bluetooth must be enabled to continue",Toast.LENGTH_SHORT).show();
        }
    }
}
