package com.synaptian.smoketracker.habits;

import java.util.Calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.util.Log;
import com.synaptian.smoketracker.habits.contentprovider.MyHabitContentProvider;
import com.synaptian.smoketracker.habits.database.HabitTable;
import com.synaptian.smoketracker.habits.database.GoalTable;
import com.synaptian.smoketracker.habits.database.EventTable;

/*
 * HabitDetailActivity allows to enter a new habit item 
 * or to change an existing
 */
public class EventDetailActivity extends Activity
	implements AdapterView.OnItemSelectedListener {
  private Spinner mHabitSelect;
  private EditText mDescriptionText;
  private TimePicker mEventTime;
  private DatePicker mEventDate;

  private Uri eventUri;
  private long habitId;

  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.goal_edit);

    mHabitSelect = (Spinner) findViewById(R.id.habit);
    mEventTime = (TimePicker) findViewById(R.id.event_time);
    mEventDate = (DatePicker) findViewById(R.id.event_date);
    mDescriptionText = (EditText) findViewById(R.id.habit_edit_description);
    Button confirmButton = (Button) findViewById(R.id.habit_edit_button);
    Button cancelButton = (Button) findViewById(R.id.habit_cancel_button);

    Bundle extras = getIntent().getExtras();

    // Check from the saved Instance
    eventUri = (bundle == null) ? null : (Uri) bundle.getParcelable(MyHabitContentProvider.EVENT_CONTENT_ITEM_TYPE);

    // Or passed from the other activity
    if (extras != null) {
      eventUri = extras.getParcelable(MyHabitContentProvider.EVENT_CONTENT_ITEM_TYPE);

      fillData(eventUri);
    }

    String[] queryCols = new String[] { HabitTable.TABLE_HABIT + "." + HabitTable.COLUMN_ID, HabitTable.COLUMN_NAME };
    String[] from = new String[] { HabitTable.COLUMN_NAME };
    int[] to = new int[] { R.id.label };

    Cursor cursor = getContentResolver().query(MyHabitContentProvider.HABITS_URI, queryCols, null, null, null);
    SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this, R.layout.habit_select_row, cursor, from, to, 0);
    mHabitSelect.setAdapter(mAdapter);
    
    mHabitSelect.setOnItemSelectedListener(this);
    
    confirmButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          setResult(RESULT_OK);
          saveState();
          finish();
        }
      });

  	cancelButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
      	setResult(RESULT_CANCELED);
      	finish();
      }
    });
  }

  private void fillData(Uri uri) {
    String[] projection = { GoalTable.COLUMN_HABIT_ID, EventTable.COLUMN_TIME, EventTable.COLUMN_DESCRIPTION };
    Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
    if (cursor != null) {
      cursor.moveToFirst();

      mDescriptionText.setText(cursor.getString(cursor.getColumnIndexOrThrow(EventTable.COLUMN_DESCRIPTION)));

      mDescriptionText.setText(uri.toString());
      
      Calendar eventTime = Calendar.getInstance();
      long seconds = cursor.getInt(cursor.getColumnIndexOrThrow(EventTable.COLUMN_TIME));
      eventTime.setTimeInMillis(seconds * 1000);
      
      mEventDate.updateDate(eventTime.get(Calendar.YEAR),
    		  eventTime.get(Calendar.MONTH),
    		  eventTime.get(Calendar.DAY_OF_MONTH));
      mEventTime.setCurrentHour(eventTime.get(Calendar.HOUR_OF_DAY));
      mEventTime.setCurrentMinute(eventTime.get(Calendar.MINUTE));
      
      cursor.close();
    }
  }

  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    saveState();
    outState.putParcelable(MyHabitContentProvider.GOAL_CONTENT_ITEM_TYPE, eventUri);
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  private void saveState() {
    String description = mDescriptionText.getText().toString();

    Calendar eventTime = Calendar.getInstance();
    eventTime.set(mEventDate.getYear(),
    			  mEventDate.getMonth(),
    			  mEventDate.getDayOfMonth(),
    			  mEventTime.getCurrentHour(),
    			  mEventTime.getCurrentMinute());
    
    ContentValues values = new ContentValues();	
    values.put(EventTable.COLUMN_HABIT_ID, habitId);
    values.put(EventTable.COLUMN_TIME, Math.floor(eventTime.getTimeInMillis() / 1000));
    values.put(EventTable.COLUMN_DESCRIPTION, description);

    if (eventUri == null) {
      // New habit
      eventUri = getContentResolver().insert(MyHabitContentProvider.EVENTS_URI, values);
    } else {
      // Update habit
      getContentResolver().update(eventUri, values, null, null);
    }

    Log.w(EventDetailActivity.class.getName(), "Event Time: " + eventTime);
  }
  
  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
	  habitId = id;
  }

  @Override
  public void onNothingSelected(AdapterView<?> arg0) {
  }
}