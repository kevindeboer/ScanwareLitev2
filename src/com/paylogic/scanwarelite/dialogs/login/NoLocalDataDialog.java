package com.paylogic.scanwarelite.dialogs.login;

import android.content.Context;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.dialogs.CommonAlertDialog;

public class NoLocalDataDialog extends CommonAlertDialog {

	public NoLocalDataDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_no_local_data));
		setMessage(context.getString(R.string.dialog_msg_no_local_data));
	}
}
