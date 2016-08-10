package kolodziejczyk.olek.inzynierka.emergencyapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class EmergencyListFragment extends ListFragment {

    private ArrayList<EmergencyObject> exampleList;
    private EmergencyObjectAdapter emergencyObjectAdapter;
    private AlertDialog confirmDialogObject;

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        EmergencyDatabaseAdapter dbAdapter=new EmergencyDatabaseAdapter(getActivity().getBaseContext());
        dbAdapter.open();
        exampleList=dbAdapter.getAllEmergencyObjects();
        dbAdapter.close();

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
        intent.putExtra(EmergencyListActivity.EMERGENCY_ID_EXTRA,emergencyObject.getObjectId());
        intent.putExtra(HomeScreen.FIRST_RUN_EXTRA,false);

        switch(fragmentToLaunch){
            case VIEW:
                intent.putExtra(EmergencyListActivity.FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.VIEW);
                break;
            case EDIT:
                intent.putExtra(EmergencyListActivity.FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.EDIT);
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
                Toast.makeText(getContext(),"Delete", Toast.LENGTH_LONG).show();
                EmergencyDatabaseAdapter dbAdapter=new EmergencyDatabaseAdapter(getActivity().getBaseContext());
                dbAdapter.open();
                dbAdapter.deleteEmergencyObject(emergencyObject.getObjectId());

                exampleList.clear();
                exampleList.addAll(dbAdapter.getAllEmergencyObjects());
                emergencyObjectAdapter.notifyDataSetChanged();

                dbAdapter.close();
                break;
        }
        return super.onContextItemSelected(item);
    }

}
