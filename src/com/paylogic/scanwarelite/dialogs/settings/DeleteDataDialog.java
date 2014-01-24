package com.paylogic.scanwarelite.dialogs.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.ScanwareLiteApplication;
import com.paylogic.scanwarelite.activities.CommonActivity;
import com.paylogic.scanwarelite.dialogs.CommonAlertDialog;
import com.paylogic.scanwarelite.helpers.OfflineLoginHelper;
import com.paylogic.scanwarelite.helpers.PreferenceHelper;
import com.paylogic.scanwarelite.helpers.ScanwareLiteOpenHelper;

public class DeleteDataDialog extends CommonAlertDialog {
	private ScanwareLiteApplication app;
	private CommonActivity commonActivity;
	private OfflineLoginHelper olHelper;

	public DeleteDataDialog(final Context context) {
		super(context);
		commonActivity = (CommonActivity) context;
		olHelper = new OfflineLoginHelper(context);
		app = (ScanwareLiteApplication) commonActivity.getApplication();

		setTitle(context.getString(R.string.dialog_title_delete_data));
		setMessage(context.getString(R.string.dialog_msg_delete_data));
		
		setButton(BUTTON_POSITIVE, context.getString(R.string.dialog_btn_ok), new OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				olHelper.deleteUserFile();
				commonActivity
						.deleteDatabase(ScanwareLiteOpenHelper.DATABASE_NAME);

				app.setUser(null);

				commonActivity.logout();
			}
		});
		
		setButton(BUTTON_NEGATIVE, context.getString(R.string.dialog_btn_cancel), new OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		
	}
}
