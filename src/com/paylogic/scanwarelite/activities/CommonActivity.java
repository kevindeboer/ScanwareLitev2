package com.paylogic.scanwarelite.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.paylogic.scanwarelite.ExceptionHandler;
import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.ScanwareLiteApplication;
import com.paylogic.scanwarelite.dialogs.menu.ExitDialog;
import com.paylogic.scanwarelite.helpers.ScanwareLiteOpenHelper;

public class CommonActivity extends Activity {

	protected ScanwareLiteApplication app;

	protected ProgressDialog progressDialog;
	protected AlertDialog alertDialog;
	protected SparseArray<DialogInterface.OnClickListener> dialogHandlers;
	protected DialogInterface.OnClickListener positiveButtonListener;
	protected DialogInterface.OnClickListener negativeButtonListener;
	protected DialogInterface.OnClickListener neutralButtonListener;
	protected ScanwareLiteOpenHelper scanwareLiteOpenHelper;
	protected SQLiteDatabase db;

	protected Resources resources;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (ScanwareLiteApplication) getApplicationContext();
		resources = getResources();
		dialogHandlers = new SparseArray<DialogInterface.OnClickListener>();
		scanwareLiteOpenHelper = new ScanwareLiteOpenHelper(
				CommonActivity.this, ScanwareLiteOpenHelper.DATABASE_NAME,
				null, ScanwareLiteOpenHelper.DATABASE_VERSION);
		
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
	}

	protected void onStart() {
		super.onStart();
	}

	protected void onRestart() {
		super.onRestart();
	}

	protected void onResume() {
		super.onResume();
	}

	protected void onPause() {
		super.onPause();
	}

	protected void onStop() {
		super.onStop();
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
			alertDialog = new ExitDialog(CommonActivity.this).create();
			alertDialog.show();
			break;
		}
		return true;
	}

	public void logout() {
		Intent intent = new Intent(CommonActivity.this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}
}
