package kolodziejczyk.olek.inzynierka.emergencyapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by sasza_000 on 2016-08-03.
 */
public class EmergencyObjectAdapter extends ArrayAdapter<EmergencyObject>{

    @InjectView(R.id.custom_list_title)
    TextView tvListTitle;
    @InjectView(R.id.custom_list_number)
    TextView tvListNumber;

    public EmergencyObjectAdapter(Context context, ArrayList<EmergencyObject> list){
        super(context,0,list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        EmergencyObject emergencyObject=getItem(position);
        if(convertView==null){
            convertView=LayoutInflater.from(getContext()).inflate(R.layout.custom_list_layout,parent,false);
        }
        ButterKnife.inject(this,convertView);
        tvListTitle.setText(emergencyObject.getTitle());
        tvListNumber.setText(emergencyObject.getPhoneNumber());
        return convertView;
    }
}
