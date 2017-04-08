package ohgo.vptech.smarttraffic.main.reporter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import ohgo.vptech.smarttraffic.main.R;
import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;


/**
 * Created by ypn on 9/6/2016.
 */
public class FragmentReportPolice extends Fragment {
    private RadioGroup rd_report_state;
    private StateReport stateReport;
    private int layoutResource;
    private int opt1,opt2,opt3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        int _type = getArguments().getInt("type");
        switch (_type){
            case Constant.REPORT_TYPE_POLICE:
                opt1 = Constant.POLICE_VISIBLE;
                opt2 = Constant.POLICE_HIDE;
                opt3 = Constant.POLICE_OTHESIDE;
                layoutResource = R.layout.fragment_report_police;
                break;
            case Constant.REPORT_TYPE_CAMERA:
                opt1 = Constant.CAM_SPEED;
                opt2 = Constant.CAM_RED_LIGHT;
                opt3 = Constant.CAM_FAKE;
                layoutResource = R.layout.fragment_report_camera;
                break;
            case Constant.REPORT_TYPE_TRAFFIC_JAM:
                opt1 = Constant.TRAFFIC_MODERATE;
                opt2 = Constant.TRAFFIC_HEAVY;
                opt3 = Constant.TRAFFIC_STANDTILL;
                layoutResource = R.layout.fragment_report_traffic_jam;
                break;
            case Constant.REPORT_TYPE_ACCIDENT:
                opt1 = Constant.ACCIDENT_MIRROR;
                opt2 = Constant.ACCIDENT_MAJOR;
                opt3 = Constant.ACCIDENT_OTHER_SIDE;
                layoutResource = R.layout.fragment_report_accident;
                break;
        }
        View v = inflater.inflate(layoutResource,container,false);
        final RadioButton rd_visible = (RadioButton)v.findViewById(R.id.rd_visible);
        final RadioButton rd_hide = (RadioButton)v.findViewById(R.id.rd_hide);
        final RadioButton rd_otherside = (RadioButton)v.findViewById(R.id.rd_otherside);
        rd_report_state = (RadioGroup)v.findViewById(R.id.rd_report_state);

        rd_report_state.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.rd_visible:
                        stateReport.backData(opt1);
                        break;
                    case R.id.rd_hide:
                        stateReport.backData(opt2);
                        break;
                    case R.id.rd_otherside:
                        stateReport.backData(opt3);
                        break;
                }
            }
        });

        setHeightRadio(rd_hide);
        setHeightRadio(rd_otherside);
        setHeightRadio(rd_visible);

        return v;
    }

    //Set height equal width for view
    private void setHeightRadio(final RadioButton radioButton){
        ViewTreeObserver vto = radioButton.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                int x;
                radioButton.getViewTreeObserver().removeOnPreDrawListener(this);
                x = radioButton.getMeasuredWidth();
                radioButton.setLayoutParams(new LinearLayout.LayoutParams(x,x));
                return true;
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        stateReport = (StateReport)context;
    }
}
