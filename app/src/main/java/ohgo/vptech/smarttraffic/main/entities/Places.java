package ohgo.vptech.smarttraffic.main.entities;

import android.graphics.Bitmap;

import com.mikepenz.materialdrawer.view.BezelImageView;

/**
 * Created by ypn on 7/15/2016.
 */
public class Places {
    Bitmap icon;
    String name;
    String summary;
    Bitmap action;

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public void setAction(Bitmap action) {
        this.action = action;
    }

    public Bitmap getAction() {
        return action;
    }
}
