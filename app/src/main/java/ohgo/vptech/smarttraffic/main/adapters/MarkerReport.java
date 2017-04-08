package ohgo.vptech.smarttraffic.main.adapters;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;

/**
 * Created by ypn on 8/29/2016.
 */
@SuppressWarnings("serial")
public class MarkerReport implements ClusterItem {
    private final LatLng rpPosition;
    private final String mTitle;
    private final String mSnippet;

    private final int mIcon;

    public MarkerReport(double lat,double lng, String t, String s,int icon){
        rpPosition = new LatLng(lat,lng);
        mTitle = t;
        mSnippet =s;
        mIcon = icon;
    }

    @Override
    public LatLng getPosition() {
        return rpPosition;
    }

    public String getTitle(){
        return  mTitle;
    }

    public String getSnippet(){
        return mSnippet;
    }

    public int getIcon() {
        return mIcon;
    }


}
