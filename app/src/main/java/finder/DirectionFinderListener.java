package finder;

import java.util.List;

/**
 * Created by ypn on 4/3/2016.
 */
public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
    void onLocationNotFound();
    void onRouteNotFound();
}
