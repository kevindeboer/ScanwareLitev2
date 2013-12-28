package com.paylogic.scanwarelite.dialogs.menu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.activities.CommonActivity;

public class ExitDialog extends AlertDialog.Builder {
	private CommonActivity commonActivity;
	
	public ExitDialog(final Context context) {
		super(context);
		commonActivity = (CommonActivity) context;
		
		setTitle(context.getString(R.string.dialog_title_confirm_exit));
		setMessage(context.getString(R.string.dialog_msg_confirm_exit));
	
		setPositiveButton(context.getString(R.string.dialog_btn_exit),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						commonActivity.logout();
						commonActivity.moveTaskToBack(true);
					}
				});

		setNegativeButton(context.getString(R.string.dialog_btn_cancel),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
	}
}
