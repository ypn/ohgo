package ohgo.vptech.smarttraffic.main.adapters;

import android.app.Activity;
import android.view.View;
import ohgo.vptech.smarttraffic.main.R;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by ypn on 8/12/2016.
 */
public class InfoWindows extends Activity implements GoogleMap.InfoWindowAdapter {
    private final  View contentView;

    public InfoWindows(){
        contentView = getLayoutInflater().inflate(R.layout.popup,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return contentView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
