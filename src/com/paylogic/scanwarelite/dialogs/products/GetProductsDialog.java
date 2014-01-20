package com.paylogic.scanwarelite.dialogs.products;

import com.paylogic.scanwarelite.R;

import android.app.ProgressDialog;
import android.content.Context;

public class GetProductsDialog extends ProgressDialog {

	public GetProductsDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_retrieving_products));
		setMessage(context.getString(R.string.dialog_msg_retrieving_products));
		setIndeterminate(true);
		setCancelable(false);
	}

}
