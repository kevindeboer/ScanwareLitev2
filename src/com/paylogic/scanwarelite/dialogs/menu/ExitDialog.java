package com.paylogic.scanwarelite.dialogs.menu;

import android.content.Context;
import android.content.DialogInterface;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.activities.CommonActivity;
import com.paylogic.scanwarelite.dialogs.CommonAlertDialog;

public class ExitDialog extends CommonAlertDialog {
	private CommonActivity commonActivity;
	
	public ExitDialog(final Context context) {
		super(context);
		
		commonActivity = (CommonActivity) context;

		setTitle(context.getString(R.string.dialog_title_confirm_exit));
		setMessage(context.getString(R.string.dialog_msg_confirm_exit));
		
		setButton(BUTTON_POSITIVE, context.getString(R.string.dialog_btn_exit), new OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				commonActivity.logout();
				commonActivity.moveTaskToBack(true);
			}
		});
		
		setButton(BUTTON_NEGATIVE, context.getString(R.string.dialog_btn_cancel), new OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
	}
}
