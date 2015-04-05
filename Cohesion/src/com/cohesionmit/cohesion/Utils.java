package com.cohesionmit.cohesion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

public class Utils {
	
	public interface ResponseHandler {
		public void onSuccess(JSONObject json);
		public void onError(int errorCode);
	}
	
	public static final int CLIENT_SIDE_ERROR = 789;
	
	private static HttpClient sHttpClient;
	
	public static void openURL(Context ctx, String url) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		ctx.startActivity(browserIntent);
	}
	
	public static void getHTTP(String url, ResponseHandler handler) {
		checkClient();
		HttpGet request = new HttpGet(url);
		sendRequest(request, handler);
	}
	
	public static void postHTTP(String url, ResponseHandler handler) {
		checkClient();
		HttpPost request = new HttpPost(url);
		sendRequest(request, handler);
	}
	
	public static void putHTTP(String url, ResponseHandler handler) {
		checkClient();
		HttpPut request = new HttpPut(url);
		sendRequest(request, handler);
	}
	
	private static void sendRequest(HttpUriRequest request, ResponseHandler handler) {
		new RequestTask(request, handler).execute();
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