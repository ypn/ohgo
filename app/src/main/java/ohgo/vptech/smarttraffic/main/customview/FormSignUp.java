package ohgo.vptech.smarttraffic.main.customview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ohgo.vptech.smarttraffic.main.entities.UserRegister;
import ohgo.vptech.smarttraffic.main.R;
import ohgo.vptech.smarttraffic.main.cores.UsersManagers;
import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;
import ohgo.vptech.smarttraffic.main.utilities.Utilities;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by ypn on 8/23/2016.
 */
public class FormSignUp extends Fragment {
    private EditText input_phone,input_first_name,input_password,input_confirm_password;
    private Button btn_create;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_form_sign_up,container,false);
        input_phone = (EditText)view.findViewById(R.id.input_phone);
        input_first_name =(EditText)view.findViewById(R.id.input_first_name);
        input_password = (EditText)view.findViewById(R.id.input_password);
        input_confirm_password = (EditText)view.findViewById(R.id.input_comfirm_password);
        btn_create = (Button)view.findViewById(R.id.btn_create);

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btn_create.setEnabled(false);

                boolean validate_phone = Utilities.checkEmail(input_phone,getString(R.string.email_invalid));
                boolean validate_first_name = Utilities.checkNullInput(input_first_name,getResources().getString(R.string.null_first_name), Constant.DEFAULT_LENGTH_INPUT,Constant.MAX_INPUT_LENGTH);

                String pass_not_match = getResources().getString(R.string.pass_not_match);
                String pattern_password = getResources().getString(R.string.pattern_password);
                Boolean validate_password = Utilities.checkPassword(input_password,input_confirm_password,pass_not_match,pattern_password);

                if(validate_first_name&&validate_phone&&validate_password){
                    UserRegister register = new UserRegister();
                    register.setFirst_name(input_first_name.getText().toString());
                    register.setPassword(input_password.getText().toString());
                    register.setRetype_password(input_confirm_password.getText().toString());
                    register.setPhone(input_phone.getText().toString());
                    register.setEncodeImage("");

                    new UsersManagers(getActivity()).registerUser(register,btn_create);

                }
                else{
                    btn_create.setEnabled(true);
                }

            }
        });
        return view;
    }
}
