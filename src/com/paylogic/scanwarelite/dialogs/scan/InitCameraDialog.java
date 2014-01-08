package com.paylogic.scanwarelite.dialogs.scan;

import android.app.ProgressDialog;
import android.content.Context;

import com.paylogic.scanwarelite.R;

public class InitCameraDialog extends ProgressDialog {

	public InitCameraDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_loading_camera));
		setMessage(context.getString(R.string.dialog_msg_loading_camera));

	}
}
