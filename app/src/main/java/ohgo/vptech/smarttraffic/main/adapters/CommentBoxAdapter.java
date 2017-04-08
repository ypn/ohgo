package ohgo.vptech.smarttraffic.main.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ohgo.vptech.smarttraffic.main.entities.CommentItem;
import ohgo.vptech.smarttraffic.main.R;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by ypn on 8/15/2016.
 */
public class CommentBoxAdapter extends RecyclerView.Adapter<CommentBoxAdapter.RecycleViewHolder> {

    private  LayoutInflater inflater;
    List<CommentItem> data = Collections.emptyList();

    public CommentBoxAdapter(Context context,List<CommentItem> data){
        inflater = LayoutInflater.from(context);
        this.data = data;
    }


    @Override
    public RecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_comment,parent,false);
        RecycleViewHolder holder = new RecycleViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(RecycleViewHolder holder, int position) {
        CommentItem ci = data.get(position);
        holder.userName.setText(ci.getUserName());
        holder.content.setText(ci.getContent());
        holder.userAvatar.setImageBitmap(ci.getUserAvatar());
        holder.time.setText(ci.getTimeDiff());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class  RecycleViewHolder extends RecyclerView.ViewHolder{
        ImageView userAvatar;
        TextView userName;
        TextView time;
        TextView content;

        public RecycleViewHolder(View itemView){
            super(itemView);

            userAvatar = (ImageView)itemView.findViewById(R.id.img_avatar);
            userName =(TextView)itemView.findViewById(R.id.tv_user_name);
            content =(TextView)itemView.findViewById(R.id.report_add_comment);
            time =(TextView)itemView.findViewById(R.id.comment_time_diff);
        }


    }
}
