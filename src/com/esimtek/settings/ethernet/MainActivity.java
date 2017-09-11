package com.esimtek.settings.ethernet;

import java.net.Inet4Address;
import java.net.InetAddress;

import com.esimtek.settings.ethernet.R;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener, OnCheckedChangeListener{

	private static final String TAG = "MainActivity";

	private static final boolean DEBUG = true;

	private EditText mEtGateway;
	private EditText mEtIp;
	private EditText mEtPrefixLength;
	private EditText mEtDns1;
	private EditText mEtDns2;
	private Button mBtnSave;
	private RadioButton mRbDhcp;
	private RadioButton mRbStaticIp;

	private IpConfiguration mIpConfiguration;
	private EthernetConfig mEthernetConfig;
	private StaticIpConfiguration mStaticIpConfiguration;
	private IpAssignment mIpAssignment;

	private RadioGroup mRgConnectType;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
			setContentView(R.layout.layout_unsupported);
			return;
		}

		setContentView(R.layout.activity_main);

		initEthernet();

		initViews();

		initData();

	}


	private void initEthernet() {
		mEthernetConfig = EthernetConfig.getInstance(this);
		mEthernetConfig.load();

		mIpConfiguration = mEthernetConfig.getIpConfiguration();

		mIpAssignment = mIpConfiguration.getIpAssignment();
		mStaticIpConfiguration = mIpConfiguration.getStaticIpConfiguration();

		logger("mIpAssignment--->" + mIpAssignment + "\nmStaticIpConfiguration--->" + mStaticIpConfiguration);

	}

	private void initViews() {
		mEtIp = (EditText) findViewById(R.id.et_ip);
		mEtPrefixLength = (EditText) findViewById(R.id.et_prefix_length);
		mEtDns1 = (EditText) findViewById(R.id.et_dns1);
		mEtDns2 = (EditText) findViewById(R.id.et_dns2);
		mEtGateway = (EditText) findViewById(R.id.et_gateway);
		mBtnSave = (Button) findViewById(R.id.btn_save);
		mRgConnectType = (RadioGroup) findViewById(R.id.rg_connect_type);

		mRbStaticIp = (RadioButton) findViewById(R.id.rb_static_ip);
		mRbDhcp = (RadioButton) findViewById(R.id.rb_dhcp);

		mBtnSave.setOnClickListener(this);
		mRgConnectType.setOnCheckedChangeListener(this);

	}

	private void initData() {
		if (IpAssignment.DHCP.equals(mIpAssignment)) {
			mRbDhcp.setChecked(true);
		} else {
			mRbStaticIp.setChecked(true);

			try {
				String addr = mStaticIpConfiguration.ipAddress.toString();
				mEtIp.setText(addr.substring(0, addr.indexOf("/")));
				mEtPrefixLength.setText(addr.substring(addr.indexOf("/") + 1));
				mEtGateway.setText(mStaticIpConfiguration.gateway.getHostAddress());
				if (mStaticIpConfiguration.dnsServers.size() > 0)
					mEtDns1.setText(((InetAddress) mStaticIpConfiguration.dnsServers.get(0)).getHostAddress());
				if (mStaticIpConfiguration.dnsServers.size() > 1)
					mEtDns2.setText(((InetAddress) mStaticIpConfiguration.dnsServers.get(1)).getHostAddress());
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private void enableEdittext(boolean isEnable) {
		mEtIp.setEnabled(isEnable);
		mEtPrefixLength.setEnabled(isEnable);
		mEtGateway.setEnabled(isEnable);
		mEtDns1.setEnabled(isEnable);
		mEtDns2.setEnabled(isEnable);
	}

	@Override
	public void onClick(View v) {
		saveChanges();
	}

	private void saveChanges() {
		int strId = setConfig(mRbStaticIp.isChecked(),
				mEtIp.getText().toString(), mEtPrefixLength.getText().toString(),
				mEtGateway.getText().toString(), mEtDns1.getText().toString(),
				mEtDns2.getText().toString());

		// Save success.
		if (strId == 0) {
			mEthernetConfig.setIpConfiguration(mIpConfiguration);
			mEthernetConfig.save();

			logger("After saving IP configuration:\nIpconfiguration---> " +
					mEthernetConfig.getIpConfiguration());

			clearEdittextFocus();
			Toast.makeText(this, getString(R.string.save_complete), Toast.LENGTH_LONG).show();
			return;
		}

		// Save failed.
		Toast.makeText(this, getString(strId), Toast.LENGTH_LONG).show();
	}

	private void clearEdittextFocus() {
		mEtIp.clearFocus();
		mEtPrefixLength.clearFocus();
		mEtGateway.clearFocus();
		mEtDns1.clearFocus();
		mEtDns2.clearFocus();
	}

	private int setConfig(boolean hasIpSettings, String ip, String prefixLen,
			String gateway, String dns1, String dns2) {
		mIpConfiguration.setIpAssignment(hasIpSettings ? IpAssignment.STATIC : IpAssignment.DHCP);

		if (hasIpSettings) {
			StaticIpConfiguration staticConfig = new StaticIpConfiguration();
			mIpConfiguration.setStaticIpConfiguration(staticConfig);

			if (TextUtils.isEmpty(ip))
				return R.string.invalid_ip_address;

			Inet4Address inetAddr = null;
			try {
				inetAddr = (Inet4Address) NetworkUtils.numericToInetAddress(ip);
			} catch (IllegalArgumentException e) {
				return R.string.invalid_ip_address;
			}

			int networkPrefixLength = -1;
			try {
				networkPrefixLength = Integer.parseInt(prefixLen);
				if (networkPrefixLength < 0 || networkPrefixLength > 32) {
					return R.string.wifi_ip_settings_invalid_network_prefix_length;
				}
				staticConfig.ipAddress = new LinkAddress(inetAddr, networkPrefixLength);
			} catch (NumberFormatException e) {
				return R.string.wifi_ip_settings_invalid_ip_address;
			}

			if (!TextUtils.isEmpty(gateway)) {
				try {
					staticConfig.gateway =
							(Inet4Address) NetworkUtils.numericToInetAddress(gateway);
				} catch (IllegalArgumentException|ClassCastException e) {
					return R.string.wifi_ip_settings_invalid_gateway;
				}
			} else {
				return R.string.wifi_ip_settings_invalid_gateway;
			}

			if (!TextUtils.isEmpty(dns1)) {
				try {
					staticConfig.dnsServers.add(
							(Inet4Address) NetworkUtils.numericToInetAddress(dns1));
				} catch (IllegalArgumentException|ClassCastException e) {
					return R.string.wifi_ip_settings_invalid_dns;
				}
			} else {
				return R.string.wifi_ip_settings_invalid_dns;
			}

			if (!TextUtils.isEmpty(dns2)) {
				try {
					staticConfig.dnsServers.add(
							(Inet4Address) NetworkUtils.numericToInetAddress(dns2));
				} catch (IllegalArgumentException|ClassCastException e) {
					return R.string.wifi_ip_settings_invalid_dns;
				}
			}

		} else {
			mIpConfiguration.setStaticIpConfiguration(null);
		}

		logger("Ip configuration--->" + mIpConfiguration);

		return 0;
	}

	@Override
	public void onCheckedChanged(RadioGroup rg, int id) {
		int checkedId = rg.getCheckedRadioButtonId();

		if (checkedId == R.id.rb_dhcp) {
			logger("Now dchp is on focus!");
			enableEdittext(false);
			return;
		}

		if (checkedId == R.id.rb_static_ip) {
			logger("Now dchp is on focus!");
			enableEdittext(true);
		}
	}

	private void logger(String string) {
		if (DEBUG) Log.e(TAG, string);
	}

	// ------------------------------------- Hide soft input---------------------------------------------------
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			View view = getCurrentFocus();
			if (isHideInput(view, ev)) {
				hideSoftInput(view.getWindowToken());
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	private boolean isHideInput(View v, MotionEvent ev) {
		if (v != null && (v instanceof EditText)) {
			int[] l = { 0, 0 };
			v.getLocationInWindow(l);
			int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
					+ v.getWidth();
			if (ev.getX() > left && ev.getX() < right && ev.getY() > top
					&& ev.getY() < bottom) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	private void hideSoftInput(IBinder token) {
		if (token != null) {
			InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			manager.hideSoftInputFromWindow(token,
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}



}
