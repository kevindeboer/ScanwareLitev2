package com.paylogic.scanwarelite.dialogs.scan;

import android.content.Context;

import com.paylogic.scanwarelite.R;

public class AlreadySeenDialog extends ScanAlertDialog {
	public AlreadySeenDialog(final Context context, String barcode,
			String seenDate) {
		super(context, barcode, R.color.scanInvalid, getTitle(context), getMessage(context, seenDate));

	}

	private static String getTitle(Context context){
		return context.getString(R.string.tv_scan_dialog_title_invalid);
	}

	private static String getMessage(Context context, String seenDate) {
		return String.format(
				context.getString(R.string.tv_scan_dialog_msg_already_scanned),
				seenDate);
	}
}
