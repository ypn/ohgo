package ohgo.vptech.smarttraffic.main.entities;

import android.graphics.Bitmap;

/**
 * Created by ypn on 8/15/2016.
 */
public class CommentItem {
    Bitmap userAvatar;
    String userName;
    String content;

    String timeDiff;

    public void setUserAvatar(Bitmap userAvatar) {
        this.userAvatar = userAvatar;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Bitmap getUserAvatar() {
        return userAvatar;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    public void setTimeDiff(String timeDiff) {
        this.timeDiff = timeDiff;
    }

    public String getTimeDiff() {
        return timeDiff;
    }
}
