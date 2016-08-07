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

    public EmergencyEditFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentLayout=inflater.inflate(R.layout.fragment_emergency_edit,container,false);
        ButterKnife.inject(this,fragmentLayout);
        Intent intent=getActivity().getIntent();

        etTitle.setText(intent.getExtras().getString(EmergencyListActivity.EMERGENCY_TITLE_EXTRA));
        etNumber.setText(intent.getExtras().getString(EmergencyListActivity.EMERGENCY_NUMBER_EXTRA));
        etMessage.setText(intent.getExtras().getString(EmergencyListActivity.EMERGENCY_MESSAGE_EXTRA));

        return fragmentLayout;
    }


}
