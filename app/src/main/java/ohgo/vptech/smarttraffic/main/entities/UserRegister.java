package ohgo.vptech.smarttraffic.main.entities;

/**
 * Created by ypn on 8/1/2016.
 */
public class UserRegister {
    private int _id;
    private String first_name ;
    private String password ;
    private String retype_password ;
    private String phone ;//This property is email. don't change;
    private String real_phone ;//This property is phone. don't change;
    private String encode_image;
    private boolean receiveNotice;


    public String getEncodeImage() {
        return encode_image;
    }

    public void setEncodeImage(String encode_image) {
        this.encode_image = encode_image;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }



    public void setPassword(String password) {
        this.password = password;
    }

    public void setRetype_password(String retype_password) {
        this.retype_password = retype_password;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFirst_name() {
        return first_name;
    }


    public String getPassword() {
        return password;
    }

    public String getRetype_password() {
        return retype_password;
    }

    public String getPhone() {
        return phone;
    }

    public String getReal_phone() {
        return real_phone;
    }

    public void setReceiveNotice(boolean receiveNotice) {
        this.receiveNotice = receiveNotice;
    }

    public boolean isReceiveNotice() {
        return receiveNotice;
    }

    public void setReal_phone(String real_phone) {
        this.real_phone = real_phone;
    }



    public void setEncode_image(String encode_image) {
        this.encode_image = encode_image;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int get_id() {
        return _id;
    }

    public String getEncode_image() {
        return encode_image;
    }
}
