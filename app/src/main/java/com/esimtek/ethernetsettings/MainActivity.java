package com.esimtek.ethernetsettings;

import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";
    private EditText mEtGateway;
    private EditText mEtIp;
    private EditText mEtPrefixLength;
    private EditText mEtDns1;
    private EditText mEtDns2;
    private EthernetHelper mEtherInstance;
    private Button mBtnSave;
    private RadioButton mRbDhcp;
    private RadioButton mRbStaticIp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        mEtherInstance = EthernetHelper.getInstance();

        mEtherInstance.init(this);



        Log.d(TAG, "getIpAssignment-->" + mEtherInstance.getIpAssignment());
        Log.d(TAG, "getIpAssignment-->" + mEtherInstance.getStaticIpConfiguration());



    }

    private void initViews() {
        mEtIp = (EditText) findViewById(R.id.et_ip);
        mEtPrefixLength = (EditText) findViewById(R.id.et_prefix_length);
        mEtDns1 = (EditText) findViewById(R.id.et_dns1);
        mEtDns2 = (EditText) findViewById(R.id.et_dns2);
        mEtGateway = (EditText) findViewById(R.id.et_gateway);
        mBtnSave = (Button) findViewById(R.id.btn_save);

        mRbStaticIp = (RadioButton) findViewById(R.id.rb_static_ip);
        mRbDhcp = (RadioButton) findViewById(R.id.rb_dhcp);

        mBtnSave.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        saveChanges();
    }

    private void saveChanges() {

        new Thread() {


        String res = mEtherInstance.setStaticConfig(mRbStaticIp.isChecked(),
                mEtIp.getText().toString(), mEtPrefixLength.getText().toString(),
                mEtGateway.getText().toString(), mEtDns1.getText().toString(),
                mEtDns2.getText().toString());
        }.start();

//        Toast.makeText(this, res, Toast.LENGTH_LONG).show();
    }
}
