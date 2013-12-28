package com.paylogic.scanwarelite.dialogs.login;

import android.app.AlertDialog;
import android.content.Context;

import com.paylogic.scanwarelite.R;

public class NoLocalDataDialog extends AlertDialog.Builder {

	public NoLocalDataDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_no_local_data));
		setMessage(context.getString(R.string.dialog_msg_no_local_data));
	}
}
