package com.paylogic.scanwarelite.dialogs.events;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.activities.ProductsActivity;
import com.paylogic.scanwarelite.dialogs.CommonAlertDialog;

public class OnlyReuseDialog extends CommonAlertDialog {

	public OnlyReuseDialog(final Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_only_reuse));
		setMessage(context.getString(R.string.dialog_msg_only_reuse));

		setButton(BUTTON_POSITIVE,
				context.getString(R.string.dialog_btn_reuse),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(context,
								ProductsActivity.class);
						context.startActivity(intent);
					}

				});

		setButton(BUTTON_NEGATIVE,
				context.getString(R.string.dialog_btn_cancel),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
	}

}
