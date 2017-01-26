package kolodziejczyk.olek.inzynierka.emergencyapp;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;


public class EmergencyDetailActivity extends AppCompatActivity {

    private static final int REQUEST_CHECK_SETTINGS = 1000;
    BluetoothService bluetoothService;
    private PendingResult<LocationSettingsResult> result = null;

    public static final String NEW_OBJECT_EXTRA = "New Emergency Object";
    public static final String SHARED_PREFS_FILENAME = "EmergencyObjectsList";
    public static final String FULL_LIST="SharedList";

    private boolean mBound=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_detail);
        createAndAddFragment();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent i=new Intent(this,BluetoothService.class);
        bindService(i,myConnection, Context.BIND_AUTO_CREATE); //bind here because we want be bound only when activity is visible
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(myConnection);
            mBound = false;
        }
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
                showDialogWindow(this);
            }
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showDialogWindow(final Activity activity){
        result=bluetoothService.getSettingsResult(); //catch the object that contains info about Location API settings
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();   //what is GPS state
                try {
                    Log.i(BluetoothService.TAG,"GPS Dialog");
                    status.startResolutionForResult(activity,REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
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
            mBound=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound=false;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        bluetoothService.updateLocationAndSendMessage();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        bluetoothService.sendMessageWithoutUpdatedLocation();
                        break;
                    default:
                        break;
                }
                break;
        }
    }
}
