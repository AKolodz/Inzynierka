package kolodziejczyk.olek.inzynierka.emergencyapp;



import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;


import java.lang.reflect.Type;
import java.util.List;


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

    private ArrayList<EmergencyObject> patternList;

    SharedPreferences sharedPrefsList;
    SharedPreferences.Editor listEditor;

    public EmergencyViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentLayout=inflater.inflate(R.layout.fragment_emergency_view,container,false);
        ButterKnife.inject(this,fragmentLayout);

        patternList=new ArrayList<EmergencyObject>();
        sharedPrefsList = getActivity().getSharedPreferences(EmergencyDetailActivity.SHARED_PREFS_FILENAME,0);

        //GET EXTRAS FROM PREVIOUS ACTIVITY
        Intent intent=getActivity().getIntent();
        firstRun=intent.getExtras().getBoolean(HomeScreen.FIRST_RUN_EXTRA,false);

        //CHECKS IF IT IS FIRST RUN - IF IT'S SO THEN WE SHOULD LOAD LAST OBJECT FROM SHAREDPREFERENCES
        if(firstRun){
            Toast.makeText(getActivity().getApplicationContext(),"FIRST RUN!",Toast.LENGTH_SHORT).show();
            //GET FULL LIST OF SAVED PATTERNS
            patternList=getPatternListFromSharedPreferences();

            if(patternList==null || patternList.size()==0){
                //gdy nie istnieje lub jest pusta: dodaj domyślny. Drugi warunek zabezpiecza przed ewentualnym wyczyszczeniem w jakiś sposób listy schematów
                Toast.makeText(getActivity().getBaseContext(),"PUSTA LISTA",Toast.LENGTH_LONG).show();
                EmergencyObject newFirstObject=new EmergencyObject("Custom Pattern","112","I need help");
                patternList=new ArrayList<EmergencyObject>();
                patternList.add(newFirstObject);

                Gson gsonPut=new Gson();
                String jsonPut=gsonPut.toJson(patternList);
                listEditor=sharedPrefsList.edit();
                listEditor.putString(EmergencyDetailActivity.FULL_LIST,jsonPut);
                listEditor.commit();
            }

            //GET IT AGAIN IN CASE IT WAS EMPTY
            patternList=getPatternListFromSharedPreferences();

            EmergencyObject firstPatternFromList=patternList.get(0);
            tvTitle.setText(firstPatternFromList.getTitle());
            tvNumber.setText(firstPatternFromList.getPhoneNumber());
            tvMessage.setText(firstPatternFromList.getMessage());

        }else{
            tvTitle.setText(intent.getExtras().getString(EmergencyListActivity.EMERGENCY_TITLE_EXTRA));
            tvNumber.setText(intent.getExtras().getString(EmergencyListActivity.EMERGENCY_NUMBER_EXTRA));
            tvMessage.setText(intent.getExtras().getString(EmergencyListActivity.EMERGENCY_MESSAGE_EXTRA));
        }

        if (tvNumber.getText().equals("")){
            Toast.makeText(getActivity(),"Please, select emergency pattern that contains emergency number",Toast.LENGTH_LONG).show();
        }

        sendMessageAndNumberToService();

        bChangePattern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(),EmergencyListActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        return fragmentLayout;
    }

    private void sendMessageAndNumberToService() {
        Intent intentPassData=new Intent(getActivity().getBaseContext(),BluetoothService.class);
        intentPassData.putExtra(BluetoothService.USERS_MESSAGE,tvMessage.getText());
        intentPassData.putExtra(BluetoothService.USERS_NUMBER,tvNumber.getText());
        getActivity().startService(intentPassData);
    }

    private ArrayList<EmergencyObject> getPatternListFromSharedPreferences() {
        Gson gsonGet=new Gson();
        String jsonGet=sharedPrefsList.getString(EmergencyDetailActivity.FULL_LIST,null);
        Type type=new TypeToken<List<EmergencyObject>>(){}.getType();
        patternList=gsonGet.fromJson(jsonGet,type);
        return patternList;
    }
}
