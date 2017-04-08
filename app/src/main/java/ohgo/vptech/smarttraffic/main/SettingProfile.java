package ohgo.vptech.smarttraffic.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;

import ohgo.vptech.smarttraffic.main.entities.UserRegister;
import ohgo.vptech.smarttraffic.main.cores.Original;
import ohgo.vptech.smarttraffic.main.cores.UsersManagers;
import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;
import ohgo.vptech.smarttraffic.main.utilities.Utilities;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;


public class SettingProfile extends Original implements View.OnClickListener {

    private ImageView btn_upload_avatar,img_avatar;
    private EditText edt_fullname,edt_phone,edt_pass,edt_confirm_pass,edt_email;
    private TextView send;
    /*Switch notice;*/
    Bitmap bitmap;
    UserRegister userRegister = new UserRegister();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btn_upload_avatar = (ImageView) findViewById(R.id.btn_upload_avatar);
        img_avatar = (ImageView) findViewById(R.id.img_avatar);

        send = (TextView)findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validate_email = Utilities.checkEmail(edt_email,getString(R.string.email_invalid));
                boolean validate_name = Utilities.checkNullInput(edt_fullname, getResources().getString(R.string.null_first_name), Constant.DEFAULT_LENGTH_INPUT, Constant.MAX_INPUT_LENGTH);
                boolean validate_password = true;



                if (!edt_pass.getText().toString().equals("")) {
                    String pass_not_match = getResources().getString(R.string.pass_not_match);
                    String pattern_password = getResources().getString(R.string.pattern_password);
                    validate_password = Utilities.checkPassword(edt_pass, edt_confirm_pass, pass_not_match, pattern_password);
                }

                if (bitmap != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] array = stream.toByteArray();
                    userRegister.setEncodeImage(Base64.encodeToString(array, Base64.DEFAULT));
                }
                if (validate_email && validate_name && validate_password) {
                    userRegister.setFirst_name(edt_fullname.getText().toString());
                    userRegister.setReal_phone(edt_phone.getText().toString());
                    userRegister.setPassword(edt_pass.getText().toString());
                    userRegister.setRetype_password(edt_confirm_pass.getText().toString());
                    userRegister.setPhone(edt_email.getText().toString());
                    userRegister.set_id(currentUser.get_Id());
              /*  userRegister.setReceiveNotice(notice.isChecked());*/
                    new UsersManagers(SettingProfile.this).updateUser(userRegister);
                }
            }
        });

        edt_fullname = (EditText)findViewById(R.id.input_user_name);
        edt_fullname.setText(currentUser.get_firstName());
        edt_phone = (EditText)findViewById(R.id.input_phone);

        if(currentUser.get_phoneNumber()!="null")edt_phone.setText(currentUser.get_phoneNumber());
        edt_pass = (EditText)findViewById(R.id.input_password);
        edt_confirm_pass = (EditText)findViewById(R.id.input_confirm_passwordd);
        edt_email = (EditText)findViewById(R.id.input_email);
        edt_email.setText(currentUser.get_email());

       /* notice  = (Switch)findViewById(R.id.switch_notice);*/
      /*  notice.setChecked(currentUser.isNotice());*/
        img_avatar.setImageBitmap(currentUser.get_profileImage());

        btn_upload_avatar.setOnClickListener(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_upload_avatar:
                Crop.pickImage(this);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(result.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            try{
                handleCrop(resultCode, result);
            }catch (OutOfMemoryError ex){
                Toast.makeText(this,"Out of memory",Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            img_avatar.setImageDrawable(null);
            img_avatar.setImageURI(Crop.getOutput(result));
            bitmap = ((BitmapDrawable)img_avatar.getDrawable()).getBitmap();
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("avatar",bitmap);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState.getParcelable("avatar")!=null){
            bitmap = savedInstanceState.getParcelable("avatar");
            img_avatar.setImageBitmap(bitmap);
        }

    }
}
