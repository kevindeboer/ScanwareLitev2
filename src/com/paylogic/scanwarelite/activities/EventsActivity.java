package com.paylogic.scanwarelite.activities;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.paylogic.scanwarelite.APIFacade;
import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.TaskListener;
import com.paylogic.scanwarelite.dialogs.events.DownloadDialog;
import com.paylogic.scanwarelite.dialogs.events.DownloadSpqDialog;
import com.paylogic.scanwarelite.dialogs.events.Error500Dialog;
import com.paylogic.scanwarelite.dialogs.events.GetEventsDialog;
import com.paylogic.scanwarelite.dialogs.events.InsufficientStorageDialog;
import com.paylogic.scanwarelite.dialogs.events.NoResourcesDialog;
import com.paylogic.scanwarelite.dialogs.events.OnlyReuseDialog;
import com.paylogic.scanwarelite.dialogs.events.OverwriteDialog;
import com.paylogic.scanwarelite.dialogs.events.ReuseOrOverwriteDialog;
import com.paylogic.scanwarelite.helpers.DatabaseHelper;
import com.paylogic.scanwarelite.models.Event;
import com.paylogic.scanwarelite.models.User;
import com.paylogic.scanwarelite.utils.FileUtils;

public class EventsActivity extends CommonActivity {

	private ListView eventsView;
	private Button refreshButton;
	private Button continueButton;

	private EventsAdapter eventsAdapter;
	private Event selectedEvent;

	private String username;
	private String password;
	private int userId;

	private User user;
	private ArrayList<Event> events;
	private GetEventsTask getEventsTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events);

		events = new ArrayList<Event>();

		Intent intent = getIntent();

		username = intent.getStringExtra("username");
		password = intent.getStringExtra("password");
		userId = intent.getIntExtra("userId", -1);

		user = User.createInstance(userId, username, password);

		eventsView = (ListView) findViewById(R.id.listView_events);
		refreshButton = (Button) findViewById(R.id.button_refresh);
		continueButton = (Button) findViewById(R.id.button_event_continue);

		eventsAdapter = new EventsAdapter(EventsActivity.this,
				android.R.layout.simple_list_item_1, events);
		eventsView.setAdapter(eventsAdapter);

		getEventsTask = new GetEventsTask();

		updateEvents();
	}

	protected void onResume() {
		super.onResume();
		eventsView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				selectedEvent = eventsAdapter.getItem(position);
				eventsAdapter.setSelectedIndex(position);

				// Disable continue button for all events but the local event if
				// internet connectivity is lost
				if (!connHelper.isConnected()
						&& !(selectedEvent.getId() == eventsAdapter
								.getLocalEvent().getId())) {
					continueButton.setClickable(false);
				} else {
					continueButton.setClickable(true);
				}
			}
		});
       
		refreshButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				updateEvents();
			}
		});

		continueButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (selectedEvent != null) {
					if ((eventsAdapter.getLocalEvent() != null)
							&& (selectedEvent.getId() == eventsAdapter
									.getLocalEvent().getId())) {
						if (!connHelper.isConnected()) {
							alertDialog = new OnlyReuseDialog(
									EventsActivity.this);
							alertDialog.show();
						} else {
							alertDialog = new ReuseOrOverwriteDialog(
									EventsActivity.this, selectedEvent);

							alertDialog.show();

						}
					} else if (databaseHelper.databaseExists()) {
						alertDialog = new OverwriteDialog(EventsActivity.this,
								selectedEvent);
						alertDialog.show();
					} else {
						alertDialog = new DownloadDialog(EventsActivity.this,
								selectedEvent);
						alertDialog.show();
					}
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.toast_select_event, Toast.LENGTH_SHORT)
							.show();
				}
			}
		});
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_events, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem toggleEventsMenuItem = menu.findItem(R.id.menu_toggle_events);
		if (preferenceHelper.showAllEvents()) {
			toggleEventsMenuItem
					.setTitle(getString(R.string.menu_item_show_active_events));
		} else {
			toggleEventsMenuItem
					.setTitle(getString(R.string.menu_item_show_all_events));
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent intent = new Intent(EventsActivity.this,
					SettingsActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_toggle_events:
			if (preferenceHelper.showAllEvents()) {
				preferenceHelper.setShowAllEvents(false);
			} else {
				preferenceHelper.setShowAllEvents(true);
			}
			updateEvents();
			break;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		// Do not return to login activity
	}

	private void updateEvents() {
		getEventsTask.execute();
		getEventsTask = new GetEventsTask();
		eventsAdapter.setSelectedIndex(-1);
		selectedEvent = null;

	}

	public class EventsAdapter extends ArrayAdapter<Event> {
		private int selectedIndex = -1;
		private Event localEvent;

		public EventsAdapter(Context context, int resource,
				ArrayList<Event> events) {
			super(context, resource, events);
		}

		public void setSelectedIndex(int index) {
			selectedIndex = index;
			notifyDataSetChanged();
		}

		public ArrayList<Event> getData() {
			return events;
		}

		public void setLocalEvent(Event event) {
			event.setName(event.getName() + "*");
			events.add(event);
			localEvent = event;
		}
		
		public Event getLocalEvent() {
			return localEvent;
		}

		public void addEvent(Event event) {
			if (event.isLocalEvent()) {
				setLocalEvent(event);
			} else if ((localEvent != null) && (event.getId() == localEvent.getId())) {
				localEvent.setDeadline(event.getDeadline());
				localEvent.setEndDate(event.getEndDate());
			} else {
				events.add(event);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			LayoutParams params;
			if (v == null) {
				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.view_events_item, null);
			}
			Event event = events.get(position);

			String eventName = event.getName();
			Date eventEndDate = event.getEndDate();
			String dateString = null;
			if (eventEndDate != null) {
				DateFormat df = new SimpleDateFormat("dd-MM-yyyy");

				dateString = df.format(eventEndDate);
			}
			TextView eventNameView = (TextView) v
					.findViewById(R.id.textView_eventName);
			TextView eventDateView = (TextView) v
					.findViewById(R.id.textView_eventDate);

			if (selectedIndex != -1 && position == selectedIndex) {
				v.setBackgroundColor(resources.getColor(R.color.selectedEvent));
			} else {
				v.setBackgroundColor(Color.TRANSPARENT);
			}
			
			if (event != null) {

				eventNameView.setText(eventName);
				if (dateString != null) {
					eventDateView.setText("(" + dateString + ")");
				}
				params = new LayoutParams(
						(int) (parent.getMeasuredWidth() * 0.7),
						LayoutParams.WRAP_CONTENT);
				eventNameView.setLayoutParams(params);

				params = new LayoutParams(
						(int) (parent.getMeasuredWidth() * 0.3),
						LayoutParams.MATCH_PARENT);
				eventDateView.setLayoutParams(params);

			}
			return v;
		}

	}

	public void setGetEventsTask(GetEventsTask getEventsTask) {
		this.getEventsTask = getEventsTask;
	}
	
	public void setUser(int id, String username, String password){
		user = User.createInstance(id, username, password);
	}

	public class DownloadSpqTask extends AsyncTask<Void, Integer, Void> {
		private String url = "https://api.paylogic.nl/API/?command=";

		private String username;
		private String password;
		private Event event;

		private boolean error = false;
		private int errorCode = -1;
		private final int notEnoughDiskSpace = -1;

		public DownloadSpqTask(String username, String password, Event event) {
			this.username = username;
			this.password = password;
			this.event = event;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new DownloadSpqDialog(EventsActivity.this);
			progressDialog.show();

			deleteDatabase(DatabaseHelper.DATABASE_NAME);
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			if (!error) {
				Intent intent = new Intent(EventsActivity.this,
						ProductsActivity.class);
				startActivity(intent);
			} else {
				if (errorCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
					alertDialog = new Error500Dialog(EventsActivity.this);

					alertDialog.show();
				} else if (errorCode == notEnoughDiskSpace) {
					alertDialog = new InsufficientStorageDialog(
							EventsActivity.this);
					alertDialog.show();
				}
			}
		}

		@Override
		protected Void doInBackground(Void... args) {

			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(
					PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
			wl.acquire();

			String command = "sparqCodeList";
			// TODO: remove nocrypt, implement decrypt
			String urlParams = "&username=" + username + "&password="
					+ password + "&mmid=" + event.getId() + "&nocrypt";

			String spqUrl = url + command + urlParams;
			try {

				URL url = new URL(spqUrl);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.connect();
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					int fileLength = conn.getContentLength();
					
					if(FileUtils.availableDiskSpace() > fileLength){
						databaseHelper.importDatabase(conn.getInputStream());
					} else {
						publishProgress(notEnoughDiskSpace);
					}
					
				} else if (conn.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
					publishProgress(HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// TODO: WHYYYYYYYYYY?!?!
				// http://code.google.com/p/android/issues/detail?id=43212
				wl.release();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (values[0] == HttpURLConnection.HTTP_OK) {
				progressDialog
						.setTitle(getString(R.string.dialog_title_downloading_event_data)
								+ event.getId());
				progressDialog.setMessage(String.format(
						getString(R.string.dialog_msg_downloading_event_data),
						values[1] / 1000));

				progressDialog.setIndeterminate(false);
			} else if (values[0] == HttpURLConnection.HTTP_INTERNAL_ERROR) {
				error = true;
				errorCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
			} else if (values[0] == notEnoughDiskSpace) {
				error = true;
				errorCode = notEnoughDiskSpace;
			} else {
				progressDialog.setProgress(values[0]);
			}
		}
	}

	public class GetEventsTask extends AsyncTask<Void, Event, Void> {
		boolean databaseExists;
		boolean isConnected;
		boolean noResources;

		private boolean showAllEvents;
		private APIFacade apiFacade;
		
		// Used for tests
		private TaskListener listener;

		public GetEventsTask() {
			this.apiFacade = new APIFacade();
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new GetEventsDialog(EventsActivity.this);
			progressDialog.show();

			eventsAdapter.clear();
			eventsAdapter.notifyDataSetChanged();
			
			showAllEvents = preferenceHelper.showAllEvents();
			databaseExists = databaseHelper.databaseExists();
			isConnected = connHelper.isConnected();
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
			} else {
				noResources = true;
			}
			return null;
		}

		private void getLocalEvent() {
			db = databaseHelper.getReadableDatabase();
			String[] columns = new String[] { DatabaseHelper.USER_KEY_ID };
			Cursor cursor = db.query(DatabaseHelper.USER_TABLE, columns, null,
					null, null, null, null, "1");

			int dbUserId = 0;
			if (cursor != null && cursor.moveToFirst()) {
				dbUserId = cursor.getInt(cursor
						.getColumnIndex(DatabaseHelper.USER_KEY_ID));
			}

			if (dbUserId == user.getUserId()) {
				columns = new String[] { DatabaseHelper.EVENT_KEY_ID,
						DatabaseHelper.EVENT_KEY_TITLE };

				cursor = db.query(DatabaseHelper.EVENTS_TABLE, columns, null,
						null, null, null, null, "1");
				if (cursor != null && cursor.moveToFirst()) {
					int eventId = cursor.getInt(cursor
							.getColumnIndex(DatabaseHelper.EVENT_KEY_ID));
					String eventTitle = cursor.getString(cursor
							.getColumnIndex(DatabaseHelper.EVENT_KEY_TITLE));
					Event event = new Event(eventId, eventTitle, null, null);
					event.setLocalEvent(true);

					publishProgress(event);
				}
			}
		}

		private void getOnlineEvents() {
			Document response = apiFacade.getEvents(user.getUsername(),
					user.getPassword());

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
					
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					
					Event event = null;
					try {
						event = new Event(id, title, df.parse(endDate), df.parse(deadline));
					} catch (ParseException e) {
						e.printStackTrace();
					}

					Date now = new Date();

					if (event.getEndDate().after(now) || showAllEvents) {
						publishProgress(event);
					}
				}
			}
		}

		@Override
		protected void onProgressUpdate(Event... events) {
			eventsAdapter.addEvent(events[0]);
			eventsAdapter.notifyDataSetChanged();
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();

			if (noResources) {
				alertDialog = new NoResourcesDialog(EventsActivity.this);
				alertDialog.show();
			}

			if (listener != null) {
				listener.executionDone();
			}
		}

		public void setListener(TaskListener listener) {
			this.listener = listener;
		}

		public void setAPIFacade(APIFacade apiFacade) {
			this.apiFacade = apiFacade;
		}
	}
}
