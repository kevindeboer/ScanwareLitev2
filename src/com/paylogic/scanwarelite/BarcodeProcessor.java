package com.paylogic.scanwarelite;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.paylogic.scanwarelite.helpers.DatabaseHelper;
import com.paylogic.scanwarelite.models.Barcode;

public class BarcodeProcessor {

	private DatabaseHelper databaseHelper;

	public BarcodeProcessor(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	public Barcode process(String barcode) throws InvalidChecksumException,
			InvalidBarcodeLengthException {
		boolean found;
		boolean validPayment = false;
		boolean seen = false;
		Date seenDate = null;
		boolean allowed = false;
		String productName = null;
		String name = null;

		if (barcode.length() == 13) {
			if (!validChecksum(barcode)) {
				throw new InvalidChecksumException(
						"The checksum did not validate");
			} else {
				barcode = barcode.substring(0, barcode.length() - 1);
			}
		} else if (barcode.length() != 12) {
			throw new InvalidBarcodeLengthException(
					"Barcode must have a 12 or 13 digit length");
		}

		SQLiteDatabase db = databaseHelper.getWritableDatabase();

		String query = "SELECT " + DatabaseHelper.BARCODES_KEY_CODE + ", "
				+ DatabaseHelper.BARCODES_KEY_NAME + ", "
				+ DatabaseHelper.BARCODES_KEY_PAYSTATUS + ", "
				+ DatabaseHelper.BARCODES_KEY_SEEN + ", "
				+ DatabaseHelper.BARCODES_KEY_SEENDATE + ", "
				+ DatabaseHelper.PRODUCTS_KEY_ALLOWED + ", "
				+ DatabaseHelper.PRODUCTS_KEY_TITLE + " FROM "
				+ DatabaseHelper.BARCODES_TABLE + ", "
				+ DatabaseHelper.PRODUCTS_TABLE
				+ " WHERE barcodes.productID = products.id and code = ?";
		Cursor cursor = db.rawQuery(query, new String[] { barcode });

		if (cursor.getCount() == 0) {
			found = false;
			return new Barcode(barcode, found);
		}

		cursor.moveToFirst();
		found = true;

		validPayment = getValidPayment(cursor);
		seen = getSeen(cursor);

		try {
			seenDate = getSeenDate(cursor);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		allowed = getAllowed(cursor);
		productName = getProductName(cursor);
		name = getName(cursor);

		return new Barcode(barcode, found, validPayment, seen, seenDate,
				allowed, productName, name);
	}

	private boolean getValidPayment(Cursor cursor) {
		int payStatus = cursor.getInt(cursor
				.getColumnIndex(DatabaseHelper.BARCODES_KEY_PAYSTATUS));
		return (payStatus == 101 || payStatus == 102);
	}

	private boolean getSeen(Cursor cursor) {
		return cursor.getInt(cursor
				.getColumnIndex(DatabaseHelper.BARCODES_KEY_SEEN)) > 0;
	}

	private boolean getAllowed(Cursor cursor) {
		return cursor.getInt(cursor
				.getColumnIndex(DatabaseHelper.PRODUCTS_KEY_ALLOWED)) > 0;
	}

	private String getProductName(Cursor cursor) {
		return cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.PRODUCTS_KEY_TITLE));
	}

	private String getName(Cursor cursor) {
		return cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.BARCODES_KEY_NAME));
	}

	private Date getSeenDate(Cursor cursor) throws ParseException {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String seenDate = cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.BARCODES_KEY_SEENDATE));

		if (seenDate != null) {
			return df.parse(seenDate);
		} else {
			return null;
		}

	}

	private boolean validChecksum(String barcode) {
		// If barcode is not 13 characters/digits long
		if (barcode.length() != 13) {
			return false;
		}
		// Sort the numbers by even and odd indexes, without the checksum digit
		ArrayList<Integer> oddIndexNumbers = new ArrayList<Integer>();
		ArrayList<Integer> evenIndexNumbers = new ArrayList<Integer>();
		for (int i = 0; i < 12; i++) {
			if (i % 2 == 0) {
				evenIndexNumbers.add(Character.getNumericValue(barcode
						.charAt(i)));
			} else {
				oddIndexNumbers
						.add(Character.getNumericValue(barcode.charAt(i)));
			}
		}
		// Add all odd numbers, multiply by 3
		int addedOdds = 0;
		for (int i = 0; i < evenIndexNumbers.size(); i++) {
			addedOdds += oddIndexNumbers.get(i);
		}
		int multipliedAddedOdds = addedOdds * 3;
		// Add all even numbers
		int addedEvens = 0;
		for (int i = 0; i < evenIndexNumbers.size(); i++) {
			addedEvens += evenIndexNumbers.get(i);
		}

		// Add the results of previous 2 steps
		int total = addedEvens + multipliedAddedOdds;
		// Subtracts the above result from the next multiple of 10
		int nextTen = (int) (Math.ceil((double) total / 10) * 10);

		int checksum = nextTen - total;

		// Assert if checksum is correct
		if (Character.getNumericValue(barcode.charAt(12)) == checksum) {
			return true;
		} else {
			return false;
		}
	}

	public void markBarcodeAsSeen(Barcode barcode) {
		Date now = new Date();

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timeStamp = df.format(now);

		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.BARCODES_KEY_SEEN, true);
		values.put(DatabaseHelper.BARCODES_KEY_SEENDATE, timeStamp);

		String whereClause = DatabaseHelper.BARCODES_KEY_CODE + "= ?";
		String[] whereArgs = { barcode.getBarcode() };

		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		db.update(DatabaseHelper.BARCODES_TABLE, values, whereClause, whereArgs);
	}

	public class InvalidChecksumException extends Exception {
		private static final long serialVersionUID = 3429318677014663182L;

		public InvalidChecksumException(String message) {
			super(message);
		}
	}

	public class InvalidBarcodeLengthException extends Exception {
		private static final long serialVersionUID = 7016976101864851641L;

		public InvalidBarcodeLengthException(String message) {
			super(message);
		}
	}

}