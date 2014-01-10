package com.paylogic.scanwarelite.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.paylogic.scanwarelite.R;

public class CommonAlertDialog extends AlertDialog {

	public CommonAlertDialog(Context context) {
		super(context);
		setCanceledOnTouchOutside(false);

		setButton(BUTTON_POSITIVE, context.getString(R.string.dialog_btn_ok),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
	}
}
