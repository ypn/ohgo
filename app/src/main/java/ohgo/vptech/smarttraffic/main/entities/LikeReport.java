package ohgo.vptech.smarttraffic.main.entities;

/**
 * Created by ypn on 9/5/2016.
 */
public class LikeReport {
    private  int count_like;
    private  int count_dis_like;
    private  boolean like;
    private  boolean dislike;
    private  int count_comment;

    public void setLike(boolean like) {
        this.like = like;
    }

    public void setDislike(boolean dislike) {
        this.dislike = dislike;
    }

    public boolean isLike() {
        return like;
    }

    public boolean isDislike() {
        return dislike;
    }

    public void setCount_dis_like(int count_dis_like) {
        this.count_dis_like = count_dis_like;
    }

    public int getCount_dis_like() {
        return count_dis_like;
    }

    public void setCount_like(int count_like) {
        this.count_like = count_like;
    }

    public int getCount_like() {
        return count_like;
    }

    public void setCount_comment(int count_comment) {
        this.count_comment = count_comment;
    }

    public int getCount_comment() {
        return count_comment;
    }

}
