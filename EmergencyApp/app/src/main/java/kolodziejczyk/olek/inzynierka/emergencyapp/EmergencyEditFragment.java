package kolodziejczyk.olek.inzynierka.emergencyapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A simple {@link Fragment} subclass.
 */
public class EmergencyEditFragment extends Fragment {

    @InjectView(R.id.edit_text_edit_title)
    EditText etTitle;
    @InjectView(R.id.edit_text_edit_number)
    EditText etNumber;
    @InjectView(R.id.edit_text_edit_message)
    EditText etMessage;
    @InjectView(R.id.button_save)
    Button bSave;

    private boolean addEmergencyObject=false;
    private long emergencyObjectId=0;
    private int editingPosition=0;

    private AlertDialog confirmDialogObject;

    private ArrayList<EmergencyObject> patternList;

    private SharedPreferences sharedPrefsList;
    private SharedPreferences.Editor listEditor;

    public EmergencyEditFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle =this.getArguments();
        if(bundle!=null){
            addEmergencyObject=bundle.getBoolean(EmergencyDetailActivity.NEW_OBJECT_EXTRA,false);
        }

        View fragmentLayout=inflater.inflate(R.layout.fragment_emergency_edit,container,false);
        ButterKnife.inject(this,fragmentLayout);
        Intent intent=getActivity().getIntent();

        etTitle.setText(intent.getExtras().getString(EmergencyListActivity.EMERGENCY_TITLE_EXTRA));
        etNumber.setText(intent.getExtras().getString(EmergencyListActivity.EMERGENCY_NUMBER_EXTRA));
        etMessage.setText(intent.getExtras().getString(EmergencyListActivity.EMERGENCY_MESSAGE_EXTRA));
        emergencyObjectId=intent.getExtras().getLong(EmergencyListActivity.EMERGENCY_ID_EXTRA,0);
        editingPosition=intent.getExtras().getInt(EmergencyListActivity.POSITION_TO_EDIT,0);

        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createConfirmationWindow();
            }
        });
        return fragmentLayout;
    }

    private void createConfirmationWindow(){
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.confirmation_title1);
        builder.setMessage(R.string.confirmation_message1);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                /*EmergencyDatabaseAdapter dbAdapter= new EmergencyDatabaseAdapter(getActivity().getBaseContext());
                dbAdapter.open();*/
                sharedPrefsList=getActivity().getSharedPreferences(EmergencyDetailActivity.SHARED_PREFS_FILENAME,0);
                patternList=getPatternListFromSharedPreferences();
                if(addEmergencyObject){
                    patternList.add(0,new EmergencyObject(etTitle.getText()+"",etNumber.getText()+"",etMessage.getText()+""));
                    //dbAdapter.createEmergencyObject(etTitle.getText()+"",etNumber.getText()+"",etMessage.getText()+"");
                }else{
                    //dbAdapter.updateEmergencyObject(emergencyObjectId,etTitle.getText()+"",etNumber.getText()+"",etMessage.getText()+"");
                    patternList.remove(editingPosition);
                    patternList.add(0,new EmergencyObject(etTitle.getText()+"",etNumber.getText()+"",etMessage.getText()+""));
                }
                Gson gsonPut=new Gson();
                String jsonPut=gsonPut.toJson(patternList);
                listEditor=sharedPrefsList.edit();
                listEditor.putString(EmergencyDetailActivity.FULL_LIST,jsonPut);
                listEditor.commit();
                //dbAdapter.close();
                Intent intent = new Intent(getActivity().getApplicationContext(),EmergencyListActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do nothing here
            }
        });

        confirmDialogObject=builder.create();
        confirmDialogObject.show();
    }

    private ArrayList<EmergencyObject> getPatternListFromSharedPreferences() {
        ArrayList<EmergencyObject> list=new ArrayList<EmergencyObject>();
        Gson gsonGet=new Gson();
        String jsonGet=sharedPrefsList.getString(EmergencyDetailActivity.FULL_LIST,null);
        Type type=new TypeToken<List<EmergencyObject>>(){}.getType();
        list=gsonGet.fromJson(jsonGet,type);
        return list;
    }
}
