package com.cohesionmit.cohesion;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.login.widget.ProfilePictureView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


@SuppressLint("InflateParams")
public class SearchActivity extends Activity {
    
    private final static int SEARCH_LIMIT = 10;
    
    private Context context;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_loading);
        context = this;
        
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        Utils.near(prefs.getString(Utils.URL_KEY, null), SEARCH_LIMIT, mResponseHandler);
    }
    
    private void goToProfile(String link) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }
    
    private void noSearchResults() {
        TextView message = (TextView) findViewById(R.id.status_message);
        message.setText(getString(R.string.no_people_near));
    }
    
    private void showSearchResults(List<JSONObject> list) {
        setContentView(R.layout.activity_search);
        
        TableLayout table = (TableLayout) findViewById(R.id.people_table);
        TableRow divider = (TableRow) getLayoutInflater().inflate(R.layout.row_divider, null);
        table.addView(divider);
        
        TableRow row;
        for (JSONObject o : list) {
            try {
                row = (TableRow) getLayoutInflater().inflate(R.layout.people_row, null);
                table.addView(row);
                
                String firstName = o.getString("firstname");
                String lastName = o.getString("lastname");
                final String link = o.getString("fburl");
                String distance = String.format(Locale.US, "%.2f", o.getDouble("distance"));
                JSONArray classesJSON = o.getJSONArray("classes");
                
                SortedMap<String, String> classes =
                        new TreeMap<String, String>(new ClassNameComparator());
                for (int i = 0; i < classesJSON.length(); i++) {
                    JSONObject classJSON = classesJSON.getJSONObject(i);
                    classes.put(classJSON.getString("name"), classJSON.getString("status"));
                }
                
                TextView name = (TextView) row.findViewById(R.id.name);
                name.setText(firstName + " " + lastName + "\n(" + distance + " km)");
                
                TextView classText = (TextView) row.findViewById(R.id.classes);
                String classList = "";
                
                for (Map.Entry<String, String> e : classes.entrySet()) {
                    String classString = e.getKey();
                    classString += " (";
                    
                    String value = e.getValue();
                    if (Utils.CLASS_TODO.equals(value)) {
                        classString += getString(R.string.class_todo);
                    } else if (Utils.CLASS_STARTED.equals(value)) {
                        classString += getString(R.string.class_started);
                    } else if (Utils.CLASS_DONE.equals(value)) {
                        classString += getString(R.string.class_done);
                    }
                    
                    classString += ")";
                    classList += classString + "\n";
                }
                
                classText.setText(classList.trim());
                
                ProfilePictureView picture = (ProfilePictureView) row.findViewById(R.id.picture);
                picture.setProfileId(getUserID(link));
                
                row.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToProfile(link);
                    }
                });
                
                divider = (TableRow) getLayoutInflater().inflate(R.layout.row_divider, null);
                table.addView(divider);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    private String getUserID(String link) {
        String[] parts = link.split("/");
        return parts[parts.length - 1];
    }
    
    private final Utils.ResponseHandler mResponseHandler = new Utils.ResponseHandler() {
        @Override
        public void onSuccess(JSONObject json) {
            try {
                JSONArray list = (JSONArray) json.get("near");
                int numPeople = list.length();
                
                if (numPeople == 0) {
                    noSearchResults();
                } else {
                    List<JSONObject> personList = new ArrayList<JSONObject>();
                    for (int i = 0; i < numPeople; i++) {
                        personList.add(list.getJSONObject(i));
                    }
                    
                    showSearchResults(personList);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void onError(int errorCode) {
            // TODO
        }
    };
}
