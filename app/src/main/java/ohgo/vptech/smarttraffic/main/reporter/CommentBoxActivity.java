package ohgo.vptech.smarttraffic.main.reporter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import ohgo.vptech.smarttraffic.main.R;
import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;
import android.widget.EditText;
import android.widget.TextView;

public class CommentBoxActivity extends AppCompatActivity {

    EditText edt_entry_new_comment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_box);

        edt_entry_new_comment = (EditText)findViewById(R.id.edt_entry_new_comment);

        Bundle extra = getIntent().getExtras();
        if(extra!=null){
            edt_entry_new_comment.setText(extra.getString(Constant.EXTRA_CURRENT_COMMENT_REPORT));
            edt_entry_new_comment.setSelection(edt_entry_new_comment.getText().length());
        }

        edt_entry_new_comment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    Intent output = new Intent();
                    output.putExtra(Constant.EXTRA_RESPONSE_RESULT_COMMENT_REPORT,edt_entry_new_comment.getText().toString());
                    setResult(RESULT_OK,output);
                    finish();
                    overridePendingTransition(R.anim.reverse_slide_in_right,R.anim.reverse_slide_out_left);
                }
                return  true;
            }
        });

       /* edt_entry_new_comment.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction()== KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                    Intent output = new Intent();
                    output.putExtra(Constant.EXTRA_RESPONSE_RESULT_COMMENT_REPORT,edt_entry_new_comment.getText().toString());
                    setResult(RESULT_OK,output);
                    finish();
                    overridePendingTransition(R.anim.reverse_slide_in_right,R.anim.reverse_slide_out_left);
                    return true;
                }else
                return false;
            }
        });*/
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.reverse_slide_in_right,R.anim.reverse_slide_out_left);
    }
}
