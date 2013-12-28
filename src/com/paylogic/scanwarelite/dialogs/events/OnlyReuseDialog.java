package com.paylogic.scanwarelite.dialogs.events;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.activities.ProductsActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;

public class OnlyReuseDialog extends AlertDialog.Builder {

	public OnlyReuseDialog(final Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_only_reuse));
		setMessage(context.getString(R.string.dialog_msg_only_reuse));

		setPositiveButton(context.getString(R.string.dialog_btn_reuse),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(context,
								ProductsActivity.class);
						context.startActivity(intent);
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
