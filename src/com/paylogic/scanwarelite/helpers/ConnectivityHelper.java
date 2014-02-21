package com.paylogic.scanwarelite.helpers;

import javax.inject.Inject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityHelper {
	
	private Context context;
	
	@Inject
	public ConnectivityHelper(Context context){
		this.context = context;
	}
	
	public boolean isConnected() {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}

