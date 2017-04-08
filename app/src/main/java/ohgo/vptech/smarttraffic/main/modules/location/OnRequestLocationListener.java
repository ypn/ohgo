package ohgo.vptech.smarttraffic.main.modules.location;

import android.location.Location;

/**
 * Created by ypn on 11/11/2016.
 */
public interface OnRequestLocationListener {
    void getLocationSuccess(Location location);
    void locationTurnOff();
    void getLocationFails();
    void checkPermissionFails();
}
