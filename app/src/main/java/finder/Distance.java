package finder;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ypn on 4/3/2016.
 */
public class Distance implements Parcelable {
    public String text;
    public int value;

    public Distance(String text, int value) {
        this.text = text;
        this.value = value;
    }

    protected Distance(Parcel in) {
        text = in.readString();
        value = in.readInt();
    }

    public static final Creator<Distance> CREATOR = new Creator<Distance>() {
        @Override
        public Distance createFromParcel(Parcel in) {
            return new Distance(in);
        }

        @Override
        public Distance[] newArray(int size) {
            return new Distance[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeInt(value);
    }
}
