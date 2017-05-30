package Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.CloudieNetwork.GadgetFlow.R;

import java.util.ArrayList;

import Model.SpinnerItem;

/** Created by Vasilis Fouroulis on 8/7/2016. **/
public class CustomSpinnerAdapter extends ArrayAdapter {
    public ArrayList CustomListViewValuesArr;
    LayoutInflater inflater;
    Typeface myTypeFace;
    SpinnerItem item = null;


    public CustomSpinnerAdapter(Context context, int textViewResourceId,  ArrayList CustomListViewValuesArr) {
        super(context, textViewResourceId, CustomListViewValuesArr);
        this.CustomListViewValuesArr = CustomListViewValuesArr;
        this.inflater   = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.myTypeFace = Typeface.createFromAsset(context.getAssets(),"fonts/OpenSans-Regular.ttf");

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomDropDownView(position, convertView, parent);
    }

    public View getCustomDropDownView(int position, View convertView, ViewGroup parent) {
//        Log.d("","CustomListViewValuesArr" + position);
        /********** Inflate spinner_rows.xml file for each row ( Defined below ) ************/
        View row = inflater.inflate(R.layout.simple_spinner_dropdown_item, parent, false);
        item = (SpinnerItem) CustomListViewValuesArr.get(position);
        /***** Get each Model object from Arraylist ********/
        TextView label = (TextView)row.findViewById(R.id.spinner_drop_down);
        // Default selected Spinner item
        label.setText(item.getFilterName());
        label.setTypeface(myTypeFace);
        return row;
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        /********** Inflate spinner_rows.xml file for each row ( Defined below ) ************/
        View row = inflater.inflate(R.layout.spinner_item, parent, false);
        item = (SpinnerItem) CustomListViewValuesArr.get(position);
        /***** Get each Model object from Arraylist ********/
        TextView label = (TextView)row.findViewById(R.id.spinner_item);
        TextView hint = (TextView)row.findViewById(R.id.spinner_hint);
        hint.setTypeface(myTypeFace);
        // Default selected Spinner item
        label.setText(item.getFilterName());
        label.setTypeface(myTypeFace);
        return row;
    }

}
