package ohgo.vptech.smarttraffic.main.adapters;

import android.content.Context;

import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;

import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ypn on 8/29/2016.
 * Desc: Customize marker in cluster
 */
public class MarkerRender extends DefaultClusterRenderer<MarkerReport>{
   // private Context mContext;

    public MarkerRender (Context context, GoogleMap map, ClusterManager<MarkerReport> clusterManager){
        super(context,map,clusterManager);
        //mContext = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(MarkerReport item, MarkerOptions markerOptions) {
        //markerOptions.draggable(true);
        markerOptions.snippet(item.getSnippet());
        try {
            JSONObject jsonObject = new JSONObject(item.getSnippet());

            int type = jsonObject.getInt("type");

            switch (type){
                case Constant.REPORT_TYPE_POLICE:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(item.getIcon()));
                    break;
                case Constant.REPORT_TYPE_CAMERA:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(item.getIcon()));
                    break;
                case Constant.REPORT_TYPE_ACCIDENT:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(item.getIcon()));
                    break;
                case Constant.REPORT_TYPE_TRAFFIC_JAM:
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(item.getIcon()));
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        super.onBeforeClusterItemRendered(item, markerOptions);
    }
}
