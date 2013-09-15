package com.synaptian.smoketracker.habits;

import org.dhappy.android.widget.Timer;

import com.synaptian.smoketracker.habits.contentprovider.MyHabitContentProvider;
import com.synaptian.smoketracker.habits.database.HabitTable;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class HabitTimeListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private SimpleCursorAdapter adapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list);

        registerForContextMenu(getListView());

        String[] from = new String[] { HabitTable.COLUMN_NAME, HabitTable.COLUMN_TIME };
        // Fields on the UI to which we map
        int[] to = new int[] { R.id.label, R.id.timer };

        getLoaderManager().initLoader(0, null, this);

        adapter = new SimpleCursorAdapter(this, R.layout.habit_row, null, from, to, 0);

        adapter.setViewBinder(new ViewBinder() {
    		@Override
    		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
    			if(columnIndex == 2) { // Time
                    long time = cursor.getInt(columnIndex);
                    Timer timer = (Timer) view;
                    timer.setStartingTime(time * 1000);
                    return true;
    			}

    			return false;
    		}
        });
        
        setListAdapter(adapter);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.listmenu, menu);
      return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.insert:
        createHabit();
        return true;
      }
      return super.onOptionsItemSelected(item);
    }

    private void createHabit() {
    	Intent i = new Intent(this, HabitDetailActivity.class);
        startActivity(i);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = { HabitTable.COLUMN_ID, HabitTable.COLUMN_NAME, HabitTable.COLUMN_TIME };
        CursorLoader cursorLoader = new CursorLoader(this, MyHabitContentProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }
    
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
      adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
      // data is not available anymore, delete reference
      adapter.swapCursor(null);
    }
}