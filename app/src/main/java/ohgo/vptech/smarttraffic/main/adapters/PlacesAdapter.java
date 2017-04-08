package ohgo.vptech.smarttraffic.main.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ohgo.vptech.smarttraffic.main.R;
import ohgo.vptech.smarttraffic.main.SettingPlaceActivity;

import ohgo.vptech.smarttraffic.main.entities.Places;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by ypn on 10/5/2016.
 */
public class PlacesAdapter extends ArrayAdapter {

    private Context mContext;
    private int mLayout;
    private ArrayList<Places> mListPlace = new ArrayList<>();

    public PlacesAdapter(Context context, int resource, List<Places> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mListPlace = new ArrayList<>(objects);
        this.mLayout = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemPlace = convertView;
        if(itemPlace==null){
            LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemPlace = layoutInflater.inflate(mLayout,null);

        }

        ImageView _action = (ImageView)itemPlace.findViewById(R.id.iv_action);
        if(_action!=null){
            _action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext, SettingPlaceActivity.class));
                }
            });

        }
         ImageView _icon = (ImageView) itemPlace.findViewById(R.id.iv_icon);
         TextView _name = (TextView)itemPlace.findViewById(R.id.tv_name);
         TextView _summary = (TextView)itemPlace.findViewById(R.id.tv_summary);

        Places place = mListPlace.get(position);
        _icon.setImageBitmap(place.getIcon());
        _name.setText(place.getName());
        _summary.setText(place.getSummary());

        return itemPlace;
    }

}
