package com.cohesionmit.cohesion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class Utils {
	
	public interface ResponseHandler {
		public void onSuccess(JSONObject json);
		public void onError(int errorCode);
	}
	
	public static final String APP_URL = "http://18.189.110.239:8000";
	public static final String CLASS_TODO = "TODO";
	public static final String CLASS_STARTED = "STARTED";
	public static final String CLASS_DONE = "DONE";
	public static final String URL_KEY = "url";
	public static final int CLIENT_SIDE_ERROR = 789;
	
	private static final String CLASSES_KEY = "classes";
	
	private static HttpClient sHttpClient;
	
	public static Map<String, String> getLocalClasses(Context context) {
		SharedPreferences prefs =
        		PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> classSet = prefs.getStringSet(CLASSES_KEY, null);
		
		if (classSet == null) {
			classSet = new HashSet<String>();
		}
		
		return classSetToMap(classSet);
	}
	
	public static void updateLocalClasses(Context context, Map<String, String> classes) {
		SharedPreferences prefs =
        		PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> classSet = classMapToSet(classes);
		
		Editor editor = prefs.edit();
		editor.putStringSet(CLASSES_KEY, classSet);
		editor.commit();
	}
	
	public static void register(
			String firstName, String lastName, String link, ResponseHandler handler) {
		HttpPost request = new HttpPost(APP_URL + "/api/register");
		
		JSONObject json = new JSONObject();
		try {
			json.put("firstname", firstName);
			json.put("lastname", lastName);
			json.put("fburl", link);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sendPost(request, json, handler);
	}
	
	public static void near(String link, int limit, ResponseHandler handler) {
		HttpPost request = new HttpPost(APP_URL + "/api/near");
		
		JSONObject json = new JSONObject();
		try {
			json.put("fburl", link);
			json.put("limit", limit);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sendPost(request, json, handler);
	}
	
	public static void location(String link, Location loc, ResponseHandler handler) {
		HttpPost request = new HttpPost(APP_URL + "/api/location");
		
		JSONObject json = new JSONObject();
		try {
			json.put("fburl", link);
			json.put("latitude", loc.getLatitude());
			json.put("longitude", loc.getLongitude());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sendPost(request, json, handler);
	}
	
	public static void setClasses(String link, Map<String, String> classes, ResponseHandler handler) {
		HttpPost request = new HttpPost(APP_URL + "/api/setclasses");
		
		JSONArray classList = new JSONArray();
		JSONObject classJSON;
		JSONObject json = new JSONObject();
		try {
			for (Map.Entry<String, String> e : classes.entrySet()) {
				classJSON = new JSONObject();
				classJSON.put("name", e.getKey());
				classJSON.put("status", e.getValue());
				classList.put(classJSON);
			}
			
			json.put("fburl", link);
			json.put("classes", classList);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sendPost(request, json, handler);
	}
	
	public static void getClasses(String link, ResponseHandler handler) {
		HttpPost request = new HttpPost(APP_URL + "/api/getclasses");
		
		JSONObject json = new JSONObject();
		try {
			json.put("fburl", link);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sendPost(request, json, handler);
	}
	
	private static Map<String, String> classSetToMap(Set<String> set) {
		HashMap<String, String> classMap = new HashMap<String, String>();
		
		for (String s : set) {
			String[] keyVal = s.split("=");
			classMap.put(keyVal[0], keyVal[1]);
		}
		
		return classMap;
	}
	
	private static Set<String> classMapToSet(Map<String, String> map) {
		HashSet<String> classSet = new HashSet<String>();
		
		for (Map.Entry<String, String> e : map.entrySet()) {
			classSet.add(e.getKey() + "=" + e.getValue());
		}
		
		return classSet;
	}
	
	private static void sendPost(HttpPost request, JSONObject json, ResponseHandler handler) {
		checkClient();
		Log.d("Cohesion", json.toString());
		
		try {
			StringEntity entity = new StringEntity(json.toString(), HTTP.UTF_8);
			entity.setContentType("application/json");
			request.setEntity(entity);
			new RequestTask(request, handler).execute();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void checkClient() {
		if (sHttpClient == null) {
			sHttpClient = new DefaultHttpClient();
		}
	}
	
	private static class RequestTask extends AsyncTask<String, String, String>{
		
		private HttpUriRequest mRequest;
		private ResponseHandler mHandler;
		private int mStatus;
		
		public RequestTask(HttpUriRequest request, ResponseHandler handler) {
			mRequest = request;
			mHandler = handler;
		}

	    @Override
	    protected String doInBackground(String... args) {
	    	try {
				HttpResponse response = sHttpClient.execute(mRequest);
	    		mStatus = response.getStatusLine().getStatusCode();
	    		if (mStatus == 200) {
	    			HttpEntity entity = response.getEntity();
	    			StringBuilder builder = new StringBuilder();
	    			InputStream content = entity.getContent();
	    			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
	    			
	    			String line;
	    			while((line = reader.readLine()) != null){
	    				builder.append(line);
	    			}
	    			
	    			return builder.toString();
	    		}
			} catch (ClientProtocolException e) {
				mStatus = CLIENT_SIDE_ERROR;
			} catch (IOException e) {
				mStatus = CLIENT_SIDE_ERROR;
			}
	    	
	    	return null;
	    }

	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
	        Log.d("Cohesion", Integer.toString(mStatus));
	        if (mHandler == null) {
	        	return;
	        }
	        
	        if (result != null) {
	        	JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(result);
					mHandler.onSuccess(jsonObject);
				} catch (JSONException e) {
					mHandler.onError(CLIENT_SIDE_ERROR);
				}
    		} else {
    			mHandler.onError(mStatus);
    		}
	    }
	}
}