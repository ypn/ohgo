package ohgo.vptech.smarttraffic.main.entities;

/**
 * Created by ypn on 8/22/2016.
 */
public class ShortUsers {
    private String firstName;
    private String lastName;
    private String urlImageProfile;

    private int _id;

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setUrlImageProfile(String urlImageProfile) {
        this.urlImageProfile = urlImageProfile;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUrlImageProfile() {
        return urlImageProfile;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }
}
