package com.paylogic.scanwarelite.dialogs.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.ScanwareLiteApplication;
import com.paylogic.scanwarelite.activities.CommonActivity;
import com.paylogic.scanwarelite.activities.SettingsActivity;
import com.paylogic.scanwarelite.helpers.PreferenceHelper;
import com.paylogic.scanwarelite.helpers.ScanwareLiteOpenHelper;

public class DeleteDataDialog extends AlertDialog.Builder {
	private ScanwareLiteApplication app;
	private SettingsActivity settingsActivity;
	private CommonActivity commonActivity;
	private SharedPreferences settings;
	private String userFile;

	public DeleteDataDialog(final Context context) {
		super(context);
		settingsActivity = (SettingsActivity) context;
		commonActivity = (CommonActivity) context;
		
		app = (ScanwareLiteApplication) settingsActivity.getApplication();
		settings = settingsActivity.getSharedPreferences(
				PreferenceHelper.PREFS_FILE, Context.MODE_PRIVATE);
		userFile = settings.getString(PreferenceHelper.KEY_USER_FILE, null);

		setTitle(context.getString(R.string.dialog_title_delete_data));
		setMessage(context.getString(R.string.dialog_msg_delete_data));

		setPositiveButton(context.getString(R.string.dialog_btn_ok),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						settingsActivity.deleteFile(userFile);
						settingsActivity
								.deleteDatabase(ScanwareLiteOpenHelper.DATABASE_NAME);

						app.setUserId(-1);
						app.setUsername(null);
						app.setPassword(null);

						commonActivity.logout();
					}
				});

		setNegativeButton(context.getString(R.string.dialog_btn_cancel),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						settingsActivity.dismissDialog();
					}
				});
	}
}
