package com.paylogic.scanwarelite.tasks;

import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;

import com.paylogic.scanwarelite.APIFacade;
import com.paylogic.scanwarelite.activities.EventsActivity.EventsAdapter;
import com.paylogic.scanwarelite.dialogs.events.GetEventsDialog;
import com.paylogic.scanwarelite.dialogs.events.NoResourcesDialog;
import com.paylogic.scanwarelite.helpers.ConnectivityHelper;
import com.paylogic.scanwarelite.helpers.PreferenceHelper;
import com.paylogic.scanwarelite.helpers.ScanwareLiteOpenHelper;
import com.paylogic.scanwarelite.models.Event;
import com.paylogic.scanwarelite.models.User;

public class GetEventsTask extends AsyncTask<Void, Void, Void> {
	boolean databaseExists;
	boolean isConnected;
	boolean noResources;
	private APIFacade apiFacade;
	private Context context;
	private ProgressDialog progressDialog;
	private EventsAdapter eventsAdapter;
	private ScanwareLiteOpenHelper scanwareLiteOpenHelper;
	private ConnectivityHelper connHelper;
	private SQLiteDatabase db;
	private User user;
	private AlertDialog alertDialog;
	private boolean showAllEvents;

	public GetEventsTask(Context context, APIFacade apiFacade, User user,
			EventsAdapter eventsAdapter,
			ScanwareLiteOpenHelper scanwareLiteOpenHelper,
			ConnectivityHelper connHelper, boolean showAllEvents) {
		this.context = context;
		this.apiFacade = apiFacade;
		this.eventsAdapter = eventsAdapter;
		this.scanwareLiteOpenHelper = scanwareLiteOpenHelper;
		this.connHelper = connHelper;
		this.user = user;
		this.showAllEvents = showAllEvents;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new GetEventsDialog(context);
		progressDialog.show();

		eventsAdapter.clear();
		eventsAdapter.notifyDataSetChanged();

		databaseExists = scanwareLiteOpenHelper.databaseExists();
		isConnected = connHelper.isConnected(context);
	}

	@Override
	protected Void doInBackground(Void... params) {
		if (databaseExists || isConnected) {

			// Get event from database
			if (databaseExists) {
				try {
					getLocalEvent();
				} catch (SQLiteException e) {

				}
			}

			// Get events from API
			if (isConnected) {
				getOnlineEvents();
			}
		}
		return null;
	}

	private void getLocalEvent() {
		db = scanwareLiteOpenHelper.getReadableDatabase();
		String[] columns = new String[] { ScanwareLiteOpenHelper.USER_KEY_ID };
		Cursor cursor = db.query(ScanwareLiteOpenHelper.USER_TABLE, columns,
				null, null, null, null, null, "1");

		int dbUserId = 0;
		if (cursor != null && cursor.moveToFirst()) {
			dbUserId = cursor.getInt(cursor
					.getColumnIndex(ScanwareLiteOpenHelper.USER_KEY_ID));
		}

		if (dbUserId == user.getUserId()) {
			columns = new String[] { ScanwareLiteOpenHelper.EVENT_KEY_ID,
					ScanwareLiteOpenHelper.EVENT_KEY_TITLE };

			cursor = db.query(ScanwareLiteOpenHelper.EVENTS_TABLE, columns,
					null, null, null, null, null, "1");
			if (cursor != null && cursor.moveToFirst()) {
				int eventId = cursor.getInt(cursor
						.getColumnIndex(ScanwareLiteOpenHelper.EVENT_KEY_ID));
				String eventTitle = cursor
						.getString(cursor
								.getColumnIndex(ScanwareLiteOpenHelper.EVENT_KEY_TITLE));
				Event localEvent = new Event(eventId, eventTitle, null, null);
				eventsAdapter.setLocalEvent(localEvent);
				publishProgress();
			}
		}
	}

	private void getOnlineEvents() {
		Document response = this.apiFacade.getEvents(user);

		Element root = response.getDocumentElement();
		NodeList modules = root.getElementsByTagName("module");

		for (int i = 0; i < modules.getLength(); i++) {
			Node node = modules.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;

				int id = Integer.parseInt(element.getAttribute("id"));

				String title = element.getTextContent();
				String endDate = element.getAttribute("enddate");
				String deadline = element.getAttribute("deadline");

				Event event = new Event(id, title, endDate, deadline);

				Date now = new Date();

				if (event.getEndDate().after(now)
						|| showAllEvents) {
					System.out.println();
					eventsAdapter.addEvent(event);
					publishProgress();
				}
			}
		}
	}
	
	@Override
	protected void onProgressUpdate(Void... args){
		eventsAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onPostExecute(Void result) {
		progressDialog.dismiss();

		if (noResources) {
			alertDialog = new NoResourcesDialog(context);
			alertDialog.show();
		}
	}

}