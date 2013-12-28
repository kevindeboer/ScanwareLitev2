package com.paylogic.scanwarelite.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.helpers.DialogHelper;
import com.paylogic.scanwarelite.helpers.ScanwareLiteOpenHelper;

public class SettingsActivity extends CommonActivity {
	private Button deleteAllDataButton;
	private String userFileName = "user";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		deleteAllDataButton = (Button) findViewById(R.id.button_delete_all_data);

	}

	protected void onResume() {
		super.onResume();
		deleteAllDataButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				positiveButtonListener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						deleteFile(userFileName);
						deleteDatabase(ScanwareLiteOpenHelper.DATABASE_NAME);

						app.setUserId(-1);
						app.setUsername(null);
						app.setPassword(null);

						Intent intent = new Intent(SettingsActivity.this,
								LoginActivity.class);
						startActivity(intent);
						finish();
					}
				};

				negativeButtonListener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						alertDialog.dismiss();
					}
				};

				dialogHandlers.put(
						DialogHelper.DIALOG_POSITIVE_BUTTON_LISTENER,
						positiveButtonListener);
				dialogHandlers.put(
						DialogHelper.DIALOG_NEGATIVE_BUTTON_LISTENER,
						negativeButtonListener);

				alertDialog = DialogHelper.createAlertDialogById(
						SettingsActivity.this, DialogHelper.DELETE_DATA_DIALOG,
						dialogHandlers);
				alertDialog.show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true;
	}


	public boolean onOptionsItemSelected(MenuItem item) {
		 super.onOptionsItemSelected(item);
		 return false;
	}
}
