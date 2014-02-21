package com.paylogic.scanwarelite;

import android.content.Context;

import com.paylogic.scanwarelite.activities.EventsActivity;
import com.paylogic.scanwarelite.activities.LoginActivity;
import com.paylogic.scanwarelite.activities.ProductsActivity;
import com.paylogic.scanwarelite.activities.ScanActivity;
import com.paylogic.scanwarelite.activities.SettingsActivity;
import com.paylogic.scanwarelite.activities.StatisticsActivity;
import com.paylogic.scanwarelite.helpers.ConnectivityHelper;
import com.paylogic.scanwarelite.helpers.DatabaseHelper;
import com.paylogic.scanwarelite.helpers.PreferenceHelper;

import dagger.Module;
import dagger.Provides;

@Module(injects = { LoginActivity.class, EventsActivity.class,
		ProductsActivity.class, ScanActivity.class, SettingsActivity.class,
		StatisticsActivity.class })
public class ScanwareLiteModule {
	private ScanwareLiteApplication application;
	private Context context;

	public ScanwareLiteModule(ScanwareLiteApplication application) {
		this.application = application;
		this.context = (Context) this.application;
	}

	// @Provides
	// public Context provideApplicationContext() {
	// return this.context;
	// }

	@Provides
	public ConnectivityHelper provideConnectivityHelper() {
		return new ConnectivityHelper(context);
	}

	@Provides
	public PreferenceHelper providePreferenceHelper() {
		return new PreferenceHelper(context);
	}

	@Provides
	public DatabaseHelper provideDatabaseHelper() {
		return new DatabaseHelper(context, DatabaseHelper.DATABASE_NAME, null,
				DatabaseHelper.DATABASE_VERSION);
	}

}
