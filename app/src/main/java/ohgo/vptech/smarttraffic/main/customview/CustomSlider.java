package ohgo.vptech.smarttraffic.main.customview;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ohgo.vptech.smarttraffic.main.R;
import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ypn on 9/28/2016.
 */
public class CustomSlider extends Preference {

    private SeekBar seekBar;
    private TextView tv_current_val;
    private Context mContext;
   // private static final String RANGE_NOTICE = "RANGE_NOTICE";

    public CustomSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v  = layoutInflater.inflate(R.layout.custom_slider,parent,false);

        seekBar = (SeekBar)v.findViewById(R.id.seekbar);
        tv_current_val = (TextView)v.findViewById(R.id.tv_current_val);

        final SharedPreferences setting = mContext.getSharedPreferences("LANGS",0);
        int range = setting.getInt(Constant.CODE_RANGE,5000); //default radius is 1000m.
        tv_current_val.setText(Math.round(range/1000)+"km");

        seekBar.setMax(10000);
        seekBar.setProgress(range);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int buff = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                buff =  progress;
                tv_current_val.setText(Math.round(progress/1000) + "km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = setting.edit();
                editor.putInt(Constant.CODE_RANGE,buff);
                editor.commit();
                Toast.makeText(mContext,mContext.getString(R.string.config_change),Toast.LENGTH_SHORT).show();
            }
        });


        return  v;
    }
}
