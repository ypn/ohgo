package ohgo.vptech.smarttraffic.main.entities;

import android.graphics.Bitmap;

/**
 * Created by ypn on 8/19/2016.
 */
public class Users {
    int _Id;
    String _firstName;
    Bitmap _profileImage;
    String _email;
    String _phoneNumber;
    String _urlProfileImage;
    boolean notice;
    String _address;


    public void set_Id(int _Id) {
        this._Id = _Id;
    }

    public void set_firstName(String _firstName) {
        this._firstName = _firstName;
    }


    public void set_profileImage(Bitmap _profileImage) {
        this._profileImage = _profileImage;
    }

    public void set_email(String _email) {
        this._email = _email;
    }

    public void set_phoneNumber(String _phoneNumber) {
        this._phoneNumber = _phoneNumber;
    }

    public int get_Id() {
        return _Id;
    }

    public String get_firstName() {
        return _firstName;
    }


    public Bitmap get_profileImage() {
        return _profileImage;
    }

    public String get_email() {
        return _email;
    }

    public String get_phoneNumber() {
        return _phoneNumber;
    }

    public void set_urlProfileImage(String _urlProfileImage) {
        this._urlProfileImage = _urlProfileImage;
    }

    public String get_urlProfileImage() {
        return _urlProfileImage;
    }

    public void setNotice(boolean notice) {
        this.notice = notice;
    }

    public boolean isNotice() {
        return notice;
    }

    public void set_address(String _address) {
        this._address = _address;
    }

    public String get_address() {
        return _address;
    }
}
