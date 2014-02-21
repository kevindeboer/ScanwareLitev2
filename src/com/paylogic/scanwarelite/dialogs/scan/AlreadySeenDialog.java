package com.paylogic.scanwarelite.dialogs.scan;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.models.Barcode;

public class AlreadySeenDialog extends ScanAlertDialog {
	public AlreadySeenDialog(Context context, Barcode barcode) {
		super(context, barcode, R.color.scanInvalid, getTitle(context),
				getMessage(context, barcode.getSeenDate()));

	}

	private static String getTitle(Context context) {
		return context.getString(R.string.tv_scan_dialog_title_invalid);
	}

	private static String getMessage(Context context, Date seenDate) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return String.format(
				context.getString(R.string.tv_scan_dialog_msg_already_scanned),
				df.format(seenDate));
	}
}
