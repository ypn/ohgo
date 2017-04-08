package ohgo.vptech.smarttraffic.main.entities;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ypn on 8/11/2016.
 * Desc : Define marker that show a report.
 */
public class Markers {
    private LatLng latLng;      //Position report
    private String name;        //Name of location (may be is geographic coordinates)
    private String address;     //Business address of location
    private int type;           //Type report (police,camera,accident...)
    private String imageCode;   //Capture photo from user that may be evidence
    private String comment = "";     //More information from user when they comment
    private String marker_id;   //Place id return form google map API
    private int state = -1;     //State of report. Default -1 when initialize

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setImageCode(String imageCode) {
        this.imageCode = imageCode;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMarker_id(String marker_id) {
        this.marker_id = marker_id;
    }



    public LatLng getLatLng() {
        return latLng;
    }

    public String getAddress() {
        return address;
    }

    public int getType() {
        return type;
    }

    public String getImageCode() {
        return imageCode;
    }

    public String getComment() {
        return comment;
    }

    public String getName() {
        return name;
    }

    public String getMarker_id() {
        return marker_id;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
