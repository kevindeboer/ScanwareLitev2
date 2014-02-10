package com.paylogic.scanwarelite.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.paylogic.scanwarelite.ExceptionHandler;
import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.ScanwareLiteApplication;
import com.paylogic.scanwarelite.dialogs.menu.ExitDialog;
import com.paylogic.scanwarelite.helpers.ConnectivityHelper;
import com.paylogic.scanwarelite.helpers.DatabaseHelper;
import com.paylogic.scanwarelite.helpers.PreferenceHelper;

public class CommonActivity extends Activity {

	protected ScanwareLiteApplication app;

	protected ProgressDialog progressDialog;
	protected AlertDialog alertDialog;
	protected DatabaseHelper databaseHelper;
	protected ConnectivityHelper connHelper;
	protected PreferenceHelper preferenceHelper;
	protected SQLiteDatabase db;

	protected Resources resources;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (ScanwareLiteApplication) getApplicationContext();
		resources = getResources();
		databaseHelper = new DatabaseHelper(
				CommonActivity.this, DatabaseHelper.DATABASE_NAME,
				null, DatabaseHelper.DATABASE_VERSION);
		setPreferenceHelper(new PreferenceHelper(CommonActivity.this));
		setConnectivityHelper(new ConnectivityHelper(CommonActivity.this));
		
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(
				CommonActivity.this));
	}

	protected void onStart() {
		super.onStart();
	}

	protected void onRestart() {
		super.onRestart();
	}

	protected void onResume() {
		super.onResume();
		app.setRunning(true);
		if (app.isEncrypted()) {
			// Toast.makeText(this, "Decrypt", Toast.LENGTH_SHORT).show();
			app.setEncrypted(false);
		}
	}

	protected void onPause() {
		super.onPause();
		app.setRunning(false);
	}

	protected void onStop() {
		super.onStop();
		if (!app.isRunning()) {
			// Toast.makeText(this, "Encrypt", Toast.LENGTH_SHORT).show();
			app.setEncrypted(true);
		}
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		if (alertDialog != null) {
			alertDialog.dismiss();
		}
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_common, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_logout:
			logout();
			break;

		case R.id.menu_exit:
			alertDialog = new ExitDialog(CommonActivity.this);
			alertDialog.show();
			break;
		}
		return true;
	}

	public void setPreferenceHelper(PreferenceHelper preferenceHelper) {
		this.preferenceHelper = preferenceHelper;
	}

	public void setConnectivityHelper(ConnectivityHelper connHelper) {
		this.connHelper = connHelper;
	}

	public void setDatabaseHelper(
			DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	public void dismissDialog() {
		alertDialog.dismiss();
	}

	public void logout() {
		Intent intent = new Intent(CommonActivity.this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

}