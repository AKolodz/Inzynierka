package kolodziejczyk.olek.inzynierka.emergencyapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class EmergencyListFragment extends ListFragment {

    private ArrayList<EmergencyObject> exampleList;
    private EmergencyObjectAdapter emergencyObjectAdapter;
    private AlertDialog confirmDialogObject;
    private SharedPreferences sharedPreferencesPatternList;
    private SharedPreferences.Editor listEditor;

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        sharedPreferencesPatternList=getActivity().getSharedPreferences(EmergencyDetailActivity.SHARED_PREFS_FILENAME,0);
        Gson gsonGet=new Gson();
        String jsonGet=sharedPreferencesPatternList.getString(EmergencyDetailActivity.FULL_LIST,null);
        Type type=new TypeToken<List<EmergencyObject>>(){}.getType();
        exampleList=gsonGet.fromJson(jsonGet,type);

        emergencyObjectAdapter=new EmergencyObjectAdapter(getContext(),exampleList);
        setListAdapter(emergencyObjectAdapter);

        registerForContextMenu(getListView()); //used for longClickMenu
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id){
        super.onListItemClick(list,view,position,id);
        createConfirmationWindow(position); //creates DialogBox
    }

    private void createConfirmationWindow(final int position){
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.confirmation_title);
        builder.setMessage(R.string.confirmation_message);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selectChosenPattern(position, MainActivity.FragmentToLaunch.VIEW);
                Toast.makeText(getActivity(),"Pattern changed!",Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do nothing here
            }
        });
        confirmDialogObject = builder.create();
        confirmDialogObject.show();
    }

    private void selectChosenPattern(int position, MainActivity.FragmentToLaunch fragmentToLaunch) {
        //catching clicked item from the list and putting to temporary object
        EmergencyObject emergencyObject= (EmergencyObject) getListAdapter().getItem(position);

        Intent intent=new Intent(getActivity(),EmergencyDetailActivity.class);
        intent.putExtra(EmergencyListActivity.EMERGENCY_TITLE_EXTRA,emergencyObject.getTitle());
        intent.putExtra(EmergencyListActivity.EMERGENCY_NUMBER_EXTRA,emergencyObject.getPhoneNumber());
        intent.putExtra(EmergencyListActivity.EMERGENCY_MESSAGE_EXTRA,emergencyObject.getMessage());
        intent.putExtra(BtDeviceList.MAC_ADDRESS,"empty");
        intent.putExtra(HomeScreen.FIRST_RUN_EXTRA,false);

        switch(fragmentToLaunch){
            case VIEW:
                intent.putExtra(EmergencyListActivity.FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.VIEW);
                break;
            case EDIT:
                intent.putExtra(EmergencyListActivity.FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.EDIT);
                intent.putExtra(EmergencyListActivity.POSITION_TO_EDIT,position);
                break;
        }
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, view,menuInfo);
        MenuInflater menuInflater=getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.long_press_list_menu,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int itemPosition=info.position;

        //to get clicked object's ID:
        EmergencyObject emergencyObject= (EmergencyObject) getListAdapter().getItem(itemPosition);

        switch(item.getItemId()){
            case R.id.edit:
                selectChosenPattern(itemPosition, MainActivity.FragmentToLaunch.EDIT);
                break;

            case R.id.delete:
                if(exampleList.size()==1){
                    Toast.makeText(getContext(),"ACTION FORBIDDEN: List must contain minimum 1 pattern",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(),"Deleting element "+ itemPosition, Toast.LENGTH_LONG).show();
                    exampleList.remove(itemPosition);
                    emergencyObjectAdapter.notifyDataSetChanged();

                    Gson gsonPut=new Gson();
                    String jsonPut=gsonPut.toJson(exampleList);
                    listEditor = sharedPreferencesPatternList.edit();
                    listEditor.putString(EmergencyDetailActivity.FULL_LIST,jsonPut);
                    listEditor.commit();
                }
                break;
        }
        return super.onContextItemSelected(item);
    }

}
