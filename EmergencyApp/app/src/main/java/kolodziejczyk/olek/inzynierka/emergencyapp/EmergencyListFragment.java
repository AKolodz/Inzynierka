package kolodziejczyk.olek.inzynierka.emergencyapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class EmergencyListFragment extends ListFragment {

    private ArrayList<EmergencyObject> exampleList;
    private EmergencyObjectAdapter emergencyObjectAdapter;
    private EmergencyObject exampleObject;

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        exampleList=new ArrayList<EmergencyObject>();
        exampleList=fillWithExampleObjects(exampleList);
        emergencyObjectAdapter=new EmergencyObjectAdapter(getContext(),exampleList);
        setListAdapter(emergencyObjectAdapter);
    }

    private ArrayList<EmergencyObject> fillWithExampleObjects(ArrayList<EmergencyObject> exampleList) {
        exampleObject=new EmergencyObject("Son","515159540","");
        exampleList.add(exampleObject);
        exampleObject=new EmergencyObject("Wife","604005009","");
        exampleList.add(exampleObject);
        exampleObject=new EmergencyObject("Grandson","(22)8433486","");
        exampleList.add(exampleObject);
        return exampleList;
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id){
        super.onListItemClick(list,view,position,id);

    }

}
