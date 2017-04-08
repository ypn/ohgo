package ohgo.vptech.smarttraffic.main.customview;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ohgo.vptech.smarttraffic.main.R;
import ohgo.vptech.smarttraffic.main.cores.UsersManagers;
import ohgo.vptech.smarttraffic.main.forgotpass.ForgotPasswordActivity;
import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;
import ohgo.vptech.smarttraffic.main.utilities.Utilities;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by ypn on 8/23/2016.
 */
public class FormSignIn extends Fragment {
    private Button btn_sign_in;
    private EditText input_user_name,input_password;
    private TextView tv_forgot_password;

    private ProgressBar progressBar;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_form_sign_in,container,false);

        input_user_name = (EditText)view.findViewById(R.id.input_user_name);
        input_password  =(EditText)view.findViewById(R.id.input_password);
        progressBar = (ProgressBar)view.findViewById(R.id.progress_bar);
        tv_forgot_password = (TextView)view.findViewById(R.id.tv_forgot_password);

        if(getActivity().getIntent().hasExtra(Constant.EXTRA_FORGOT_EMAIL)){
            input_user_name.setText(getActivity().getIntent().getStringExtra(Constant.EXTRA_FORGOT_EMAIL));
        }

        btn_sign_in = (Button)view.findViewById(R.id.btn_login);
        btn_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            boolean nullPhone = Utilities.checkEmail(input_user_name,getString(R.string.email_invalid));
            boolean nullPass = Utilities.checkNullInput(input_password,getResources().getString(R.string.sign_in_password),Constant.DEFAULT_LENGTH_INPUT,Constant.MAX_INPUT_LENGTH);
            if(nullPhone&&nullPass){
                progressBar.setVisibility(View.VISIBLE);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new UsersManagers(getActivity()).signin(input_user_name.getText().toString(),input_password.getText().toString(),progressBar);
                    }
                },1000);
            }
            }
        });

        tv_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ForgotPasswordActivity.class ));
            }
        });

        return  view;
    }
}
