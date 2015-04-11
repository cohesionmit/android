package com.cohesionmit.cohesion;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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

public class HomeActivity extends Activity {
	
	private Context context;
	private Resources resources;
	private Map<String, String> mClasses;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = this;
        resources = context.getResources();
        mClasses = Utils.getLocalClasses(this);
        
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
            	statusSwitch.setBackgroundColor(resources.getColor(R.color.table_select));
            }
        	
        	divider = (TableRow) getLayoutInflater().inflate(R.layout.row_divider, null);
            table.addView(divider);
        }
    }
    
    public void addClass(View v) {
    	final EditText entry = (EditText) findViewById(R.id.add_class);
    	
    	String newClass = entry.getText().toString().trim();
    	
    	if (newClass == null || newClass.length() == 0) {
    		new AlertDialog.Builder(context)
			.setTitle(resources.getString(R.string.add_class_no_name_title))
			.setMessage(resources.getString(R.string.add_class_no_name_message))
			.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) { 
    				entry.setText("");
    			}
    		})
    		.show();
    		
    		return;
    	}
    	
    	if (!Utils.checkClassName(newClass)) {
    		new AlertDialog.Builder(context)
			.setTitle(resources.getString(R.string.add_class_invalid_title))
			.setMessage(resources.getString(R.string.add_class_invalid_message, newClass))
			.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) { 
    				// do nothing
    			}
    		})
    		.show();
    		
    		return;
    	}
    	
    	if (mClasses.keySet().contains(newClass)) {
    		new AlertDialog.Builder(context)
			.setTitle(resources.getString(R.string.add_class_duplicate_title))
			.setMessage(resources.getString(R.string.add_class_duplicate_message, newClass))
			.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) { 
    				entry.setText("");
    			}
    		})
    		.show();
    		
    		return;
    	}
    	
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
        
        todo.setBackgroundColor(resources.getColor(R.color.table_select));
    	
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
                	todo.setBackgroundColor(resources.getColor(R.color.table_select));
                	started.setBackgroundColor(0x00000000);
                	done.setBackgroundColor(0x00000000);
                	
                	mClasses.put(className, Utils.CLASS_TODO);
                	Utils.updateLocalClasses(context, mClasses);
                } else if (Utils.CLASS_STARTED.equals(tag)) {
                	todo.setBackgroundColor(0x00000000);
                	started.setBackgroundColor(resources.getColor(R.color.table_select));
                	done.setBackgroundColor(0x00000000);
                	
                	mClasses.put(className, Utils.CLASS_STARTED);
                	Utils.updateLocalClasses(context, mClasses);
                } else if (Utils.CLASS_DONE.equals(tag)) {
                	todo.setBackgroundColor(0x00000000);
                	started.setBackgroundColor(0x00000000);
                	done.setBackgroundColor(resources.getColor(R.color.table_select));
                	
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
        			final TableRow row = (TableRow) mView.getParent();
        			final ViewGroup table = (ViewGroup) row.getParent();
        			final String className =
        					((TextView) mView.findViewById(R.id.class_name)).getText().toString();
        			
        			new AlertDialog.Builder(context)
            		.setTitle(resources.getString(R.string.delete_class_confirm_title))
            		.setMessage(resources.getString(R.string.delete_class_confirm_message, className))
            		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            			public void onClick(DialogInterface dialog, int which) { 
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
            			}
            		})
            		.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            			public void onClick(DialogInterface dialog, int which) { 
            				// do nothing
            			}
            		})
            		.show();
        			
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