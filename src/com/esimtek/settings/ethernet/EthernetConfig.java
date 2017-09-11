package com.esimtek.settings.ethernet;

import android.content.Context;
import android.net.EthernetManager;
import android.net.IpConfiguration;
import android.util.Log;

public class EthernetConfig {

	private final static String TAG = "EthernetConfig";

	private final EthernetManager mEthernetManager;
	private IpConfiguration mIpConfiguration;
	private static EthernetConfig mEthernetConfig;

	private EthernetConfig(Context context) {
		mEthernetManager = (EthernetManager) context.getSystemService(Context.ETHERNET_SERVICE);
		mIpConfiguration = new IpConfiguration();
	}

	public static EthernetConfig getInstance(Context context) {
		if (mEthernetConfig == null) {
			mEthernetConfig = new EthernetConfig(context);
		}
		return mEthernetConfig;
	}

	public void setIpConfiguration(IpConfiguration configuration) {
		mIpConfiguration = configuration;
	}

	public IpConfiguration getIpConfiguration() {
		return mIpConfiguration;
	}

	/**
	 * Save IpConfiguration
	 */
	public void save() {
		try {
			mEthernetManager.setConfiguration(mIpConfiguration);
		} catch (Exception e) {
			Log.e(TAG, "Saving IP configuration error:" + e);
		}
	}

	/**
	 * Load IpConfiguration from system
	 */
	public void load() {
		try {
			mIpConfiguration = mEthernetManager.getConfiguration();
		} catch (Exception e) {
			Log.e(TAG, "Loading IP configuration error:" + e);
		}

	}
}
