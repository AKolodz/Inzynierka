package kolodziejczyk.olek.inzynierka.emergencyapp;



import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class EmergencyDetailActivity extends AppCompatActivity {

    BluetoothService bluetoothService;


    public static final String NEW_OBJECT_EXTRA = "New Emergency Object";
    public static final String SHARED_PREFS_FILENAME = "EmergencyObjectsList";
    public static final String FULL_LIST="SharedList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_detail);
        createAndAddFragment();

        Intent i=new Intent(this,BluetoothService.class);
        bindService(i,myConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }else if(id==R.id.bluetooth){
            if(bluetoothService.checkGPState()) {
                bluetoothService.updateLocationAndSendMessage();
            }else{
                Toast.makeText(getApplicationContext(),"GPS is not activated",Toast.LENGTH_SHORT).show();
                createTurningOnGpsWindow();
            }
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createTurningOnGpsWindow() {
        Log.i(BluetoothService.TAG,"GPS Window");
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(R.string.GPS_confirmation_title);
        builder.setMessage(R.string.GPS_confirmation_message);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog confirmDialogObject = builder.create();
        confirmDialogObject.show();
    }

    private void createAndAddFragment(){
        Intent intent=getIntent();
        MainActivity.FragmentToLaunch fragmentToLaunch= (MainActivity.FragmentToLaunch) intent.getSerializableExtra(EmergencyListActivity.FRAGMENT_TO_LOAD_EXTRA);
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        switch(fragmentToLaunch) {
            case VIEW:
                setTitle(R.string.title_emergency_view);
                EmergencyViewFragment emergencyViewFragment = new EmergencyViewFragment();
                fragmentTransaction.add(R.id.emergency_object_container,emergencyViewFragment);
                break;
            case ADD:
                setTitle(R.string.title_emergency_add);
                EmergencyEditFragment emergencyAddFragment= new EmergencyEditFragment();

                Bundle bundle=new Bundle();
                bundle.putBoolean(NEW_OBJECT_EXTRA,true);
                emergencyAddFragment.setArguments(bundle);

                fragmentTransaction.add(R.id.emergency_object_container,emergencyAddFragment);
                break;
            case EDIT:
                setTitle(R.string.title_emergency_edit);
                EmergencyEditFragment emergencyEditFragment=new EmergencyEditFragment();
                fragmentTransaction.add(R.id.emergency_object_container,emergencyEditFragment);
                break;
        }

        fragmentTransaction.commit();
    }

    private ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothService.MyBinder myBinder=(BluetoothService.MyBinder) iBinder;
            bluetoothService=myBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

}
