package kolodziejczyk.olek.inzynierka.emergencyapp;



import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class EmergencyViewFragment extends Fragment {

    @InjectView(R.id.text_view_emergency_title)
    TextView tvTitle;
    @InjectView(R.id.text_view_emergency_number)
    TextView tvNumber;
    @InjectView(R.id.text_view_emergency_message)
    TextView tvMessage;
    @InjectView(R.id.button_change)
    Button bChangePattern;

    private boolean firstRun=false;

    public EmergencyViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentLayout=inflater.inflate(R.layout.fragment_emergency_view,container,false);
        ButterKnife.inject(this,fragmentLayout);

        Intent intent=getActivity().getIntent();
        tvTitle.setText(intent.getExtras().getString(EmergencyListActivity.EMERGENCY_TITLE_EXTRA));
        tvNumber.setText(intent.getExtras().getString(EmergencyListActivity.EMERGENCY_NUMBER_EXTRA));
        tvMessage.setText(intent.getExtras().getString(EmergencyListActivity.EMERGENCY_MESSAGE_EXTRA));
        firstRun=intent.getExtras().getBoolean(HomeScreen.FIRST_RUN_EXTRA,false);

        //CHECKS IF IT IS FIRST RUN - IF IT'S SO THEN WE SHOULD LOAD THE NEWEST OBJECT FROM DATABASE
        if(firstRun){
            Toast.makeText(getActivity().getApplicationContext(),"FIRST!",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getActivity().getApplicationContext(),"NOT FIRST- FROM LIST!",Toast.LENGTH_SHORT).show();
        }

        if (tvNumber.getText().equals("")){
            Toast.makeText(getActivity(),"Please, select emergency pattern that contains emergency number",Toast.LENGTH_LONG).show();
        }
        bChangePattern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(),EmergencyListActivity.class);
                startActivity(intent);
            }
        });
        return fragmentLayout;
    }
}
