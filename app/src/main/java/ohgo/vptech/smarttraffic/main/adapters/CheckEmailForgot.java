package ohgo.vptech.smarttraffic.main.adapters;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.util.DisplayMetrics;
import android.view.View;
import ohgo.vptech.smarttraffic.main.R;

/**
 * Created by ypn on 10/17/2016.
 */
public class CheckEmailForgot extends BottomSheetDialogFragment {
    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback(){
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if(newState ==  BottomSheetBehavior.STATE_HIDDEN);
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View contentView = View.inflate(getContext(), R.layout.check_forgot_email,null);
        dialog.setContentView(contentView);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenHeight = displaymetrics.heightPixels;


        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        params.height = screenHeight;

        CoordinatorLayout.Behavior behavior = params.getBehavior();


        if( behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(bottomSheetCallback);
        }
    }
}
