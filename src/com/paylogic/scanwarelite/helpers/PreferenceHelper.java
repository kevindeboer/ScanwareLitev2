package com.paylogic.scanwarelite.helpers;

import javax.inject.Inject;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHelper {
	
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	
	private final String prefsFile = "ScanwareLitePrefs";
	
	private final String keyShowAllEvents = "showAll";
	
	@Inject
	public PreferenceHelper(Context context){
		settings = context.getSharedPreferences(prefsFile,
				Context.MODE_PRIVATE);
	}
	
	public boolean showAllEvents(){
		return settings.getBoolean(keyShowAllEvents, false);
	}
	
	public void setShowAllEvents(boolean showAllEvents){
		editor = settings.edit();
		editor.putBoolean(keyShowAllEvents, showAllEvents);
		editor.commit();
	}
}
