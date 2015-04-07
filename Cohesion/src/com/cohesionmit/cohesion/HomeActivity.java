package com.cohesionmit.cohesion;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class HomeActivity extends ActionBarActivity {
	
	private Context context;
	private SortedMap<String, String> mClasses;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = this;
        mClasses = new TreeMap<String, String>(Utils.getLocalClasses(this));
        
        if (mClasses.size() == 0) {
        	return;
        }
        
        TableLayout table = (TableLayout) findViewById(R.id.class_table);
        TableRow divider;
        TableRow row;
        
        divider = (TableRow) getLayoutInflater().inflate(R.layout.row_divider, null);
        table.addView(divider);
        
        for (Map.Entry<String, String> e : mClasses.entrySet()) {
        	row = (TableRow) getLayoutInflater().inflate(R.layout.class_row, null);
        	TextView className = (TextView) row.findViewById(R.id.class_name);
        	className.setText(e.getKey());
            table.addView(row);
            
            LinearLayout classSelector = (LinearLayout) className.getParent();
            classSelector.setOnTouchListener(new ClassListener(classSelector));
            
            LinearLayout todo = (LinearLayout) row.findViewById(R.id.todo);
            todo.setTag(Utils.CLASS_TODO);
            todo.setOnClickListener(mStatusListener);
            LinearLayout started = (LinearLayout) row.findViewById(R.id.started);
            started.setTag(Utils.CLASS_STARTED);
            started.setOnClickListener(mStatusListener);
            LinearLayout done = (LinearLayout) row.findViewById(R.id.done);
            done.setTag(Utils.CLASS_DONE);
            done.setOnClickListener(mStatusListener);
            
            String status = e.getValue();
            LinearLayout statusSwitch = null;
            if (Utils.CLASS_TODO.equals(status)) {
            	statusSwitch = todo;
            } else if (Utils.CLASS_STARTED.equals(status)) {
            	statusSwitch = started;
            } else if (Utils.CLASS_DONE.equals(status)) {
            	statusSwitch = done;
            }
            
            if (statusSwitch != null) {
            	statusSwitch.setBackgroundColor(getResources().getColor(R.color.table_select));
            }
        	
        	divider = (TableRow) getLayoutInflater().inflate(R.layout.row_divider, null);
            table.addView(divider);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void addClass(View v) {
    	EditText entry = (EditText) findViewById(R.id.add_class);
    	
    	String newClass = entry.getText().toString();
    	entry.setText("");
    	
    	TableLayout table = (TableLayout) findViewById(R.id.class_table);
    	
    	TableRow divider = (TableRow) getLayoutInflater().inflate(R.layout.row_divider, null);
    	if (mClasses.size() == 0) {
    		table.addView(divider);
    		divider = (TableRow) getLayoutInflater().inflate(R.layout.row_divider, null);
    	}
        
    	TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.class_row, null);
    	TextView className = (TextView) row.findViewById(R.id.class_name);
    	className.setText(newClass);
        table.addView(row);
        
        LinearLayout classSelector = (LinearLayout) className.getParent();
        classSelector.setOnTouchListener(new ClassListener(classSelector));
        
        LinearLayout todo = (LinearLayout) row.findViewById(R.id.todo);
        todo.setTag(Utils.CLASS_TODO);
        todo.setOnClickListener(mStatusListener);
        LinearLayout started = (LinearLayout) row.findViewById(R.id.started);
        started.setTag(Utils.CLASS_STARTED);
        started.setOnClickListener(mStatusListener);
        LinearLayout done = (LinearLayout) row.findViewById(R.id.done);
        done.setTag(Utils.CLASS_DONE);
        done.setOnClickListener(mStatusListener);
        
        todo.setBackgroundColor(getResources().getColor(R.color.table_select));
    	
        table.addView(divider);
        
        mClasses.put(newClass, Utils.CLASS_TODO);
    	Utils.updateLocalClasses(context, mClasses);
    	SharedPreferences prefs =
        		PreferenceManager.getDefaultSharedPreferences(context);
    	Utils.setClasses(prefs.getString(Utils.URL_KEY, null), mClasses, null);
    }
    
    public void findPeople(View v) {
    	Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }
    
    private final View.OnClickListener mStatusListener = new View.OnClickListener() {
    	@Override
    	public void onClick(View v) {
    		Object tag = v.getTag();
    		TableRow row = (TableRow) v.getParent();
    		String className = ((TextView) row.findViewById(R.id.class_name)).getText().toString();
    		
    		String oldStatus = mClasses.get(className);
    		if (oldStatus != null && !oldStatus.equals(tag)) {
    			LinearLayout todo = (LinearLayout) row.findViewById(R.id.todo);
                LinearLayout started = (LinearLayout) row.findViewById(R.id.started);
                LinearLayout done = (LinearLayout) row.findViewById(R.id.done);
    			
    			if (Utils.CLASS_TODO.equals(tag)) {
                	todo.setBackgroundColor(getResources().getColor(R.color.table_select));
                	started.setBackgroundColor(0x00000000);
                	done.setBackgroundColor(0x00000000);
                	
                	mClasses.put(className, Utils.CLASS_TODO);
                	Utils.updateLocalClasses(context, mClasses);
                } else if (Utils.CLASS_STARTED.equals(tag)) {
                	todo.setBackgroundColor(0x00000000);
                	started.setBackgroundColor(getResources().getColor(R.color.table_select));
                	done.setBackgroundColor(0x00000000);
                	
                	mClasses.put(className, Utils.CLASS_STARTED);
                	Utils.updateLocalClasses(context, mClasses);
                } else if (Utils.CLASS_DONE.equals(tag)) {
                	todo.setBackgroundColor(0x00000000);
                	started.setBackgroundColor(0x00000000);
                	done.setBackgroundColor(getResources().getColor(R.color.table_select));
                	
                	mClasses.put(className, Utils.CLASS_DONE);
                	Utils.updateLocalClasses(context, mClasses);
                }
    			
    			SharedPreferences prefs =
    	        		PreferenceManager.getDefaultSharedPreferences(context);
            	Utils.setClasses(prefs.getString(Utils.URL_KEY, null), mClasses, null);
    		}
    	}
    };
    
    private class ClassListener implements OnTouchListener {
    	private final View mView;
    	private final GestureDetector mGestureDetector;
    	
    	public ClassListener(View v) {
    		mView = v;
    		
    		mGestureDetector = new GestureDetector(context,
    				new GestureDetector.SimpleOnGestureListener() {
        		@Override
        		public boolean onDoubleTap(MotionEvent e) {
        			TableRow row = (TableRow) mView.getParent();
        			ViewGroup table = (ViewGroup) row.getParent();
        			String className =
        					((TextView) mView.findViewById(R.id.class_name)).getText().toString();
        			
        			mClasses.remove(className);
        			Utils.updateLocalClasses(context, mClasses);
        			SharedPreferences prefs =
        	        		PreferenceManager.getDefaultSharedPreferences(context);
                	Utils.setClasses(prefs.getString(Utils.URL_KEY, null), mClasses, null);
        			
        			if (mClasses.size() == 0) {
        				table.removeAllViews();
        			} else {
        				View divider = table.getChildAt(table.indexOfChild(row) + 1);
            			table.removeView(row);
            			table.removeView(divider);
        			}
        			
        			return super.onDoubleTap(e);
        		}
        	});
    	}

    	@Override
    	public boolean onTouch(View v, MotionEvent event) {
    		mGestureDetector.onTouchEvent(event);
    		return true;
    	}
    };
}