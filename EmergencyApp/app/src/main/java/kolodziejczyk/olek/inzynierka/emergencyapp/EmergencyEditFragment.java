package kolodziejczyk.olek.inzynierka.emergencyapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EmergencyDatabaseAdapter dbAdapter=new EmergencyDatabaseAdapter(getActivity().getBaseContext());
                dbAdapter.open();
                if(addEmergencyObject){
                    //if we're creating new object in our database
                    dbAdapter.createEmergencyObject(etTitle.getText()+"",etNumber.getText()+"",etMessage.getText()+"");
                    Toast.makeText(getContext(),"added!",Toast.LENGTH_SHORT).show();
                }else{
                    //in other case update an old object in database
                    dbAdapter.updateEmergencyObject(emergencyObjectId,etTitle.getText()+"",etNumber.getText()+"",etMessage.getText()+"");
                }
                dbAdapter.close();
                Intent intent = new Intent(getActivity().getApplicationContext(),EmergencyListActivity.class);
                startActivity(intent);
            }
        });
        return fragmentLayout;
    }


}
