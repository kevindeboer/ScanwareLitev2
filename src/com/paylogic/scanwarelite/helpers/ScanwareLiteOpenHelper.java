package com.paylogic.scanwarelite.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.paylogic.scanwarelite.utils.FileUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class ScanwareLiteOpenHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "event.spq";
	public static final String DATABASE_PATH = "/data/data/com.paylogic.scanwarelite/databases/";
	public static final String DATABASE_FILEPATH = DATABASE_PATH
			+ DATABASE_NAME;
	public static final int DATABASE_VERSION = 1;

	public static final String EVENTS_TABLE = "module";
	public static final String PRODUCTS_TABLE = "products";
	public static final String BARCODES_TABLE = "barcodes";
	public static final String USER_TABLE = "user";

	public static final String EVENT_KEY_ID = "id";
	public static final String EVENT_KEY_TITLE = "title";

	public static final String PRODUCTS_KEY_ID = "id";
	public static final String PRODUCTS_KEY_TITLE = "title";
	public static final String PRODUCTS_KEY_ALLOWED = "allowed";


	public static final String BARCODES_KEY_CODE = "code";
	public static final String BARCODES_KEY_NAME = "name";
	public static final String BARCODES_KEY_PAYSTATUS = "payStatus";
	public static final String BARCODES_KEY_PRODUCTID = "productID";
	public static final String BARCODES_KEY_SEEN = "seen";
	public static final String BARCODES_KEY_SEENDATE = "seenDate";
	public static final String BARCODES_KEY_SCANNER = "scanner";

	public static final String USER_KEY_ID = "id";
	public static final String USER_KEY_SUPERUSER = "superUser";
	public static final String USER_KEY_SCANNER = "scanner";

	public ScanwareLiteOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public boolean importDatabase(String dbFilePath) throws IOException {

		// Close the SQLiteOpenHelper so it will commit the created empty
		// database to internal storage.
		close();
		File newDb = new File(dbFilePath);
		File oldDb = new File(DATABASE_FILEPATH);

		if (newDb.exists()) {
			// Create the databases directory if it doesnt exist yet
			File dbPath = new File(DATABASE_PATH);
			if (!dbPath.exists()) {
				dbPath.mkdir();
			}

			FileUtils.copyFile(new FileInputStream(newDb),
					new FileOutputStream(oldDb));
			// Access the copied database so SQLiteHelper will cache it and
			// mark
			// it as created.
			getWritableDatabase().close();
			return true;
		}
		return false;
	}
}
