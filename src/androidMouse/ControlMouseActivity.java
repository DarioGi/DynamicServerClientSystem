package com.example.AndroidMouse;

import android.annotation.TargetApi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class ControlMouseActivity extends Activity {
	public Intent intent;
	public Intent connectIntent;
	public Intent msgHandlerIntent;
	public Intent msgReceiveHandlerIntent;
	private Connection connection;
	private DrawView drawView;
	private ProgressDialog dialog;
	public String ipAdress;
	private SeekBar sensitivityBar;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control_mouse);
		
		
		// Show the Up button in the action bar.
		//setupActionBar();
        /** Called when the activity is first created. */
		try {
			Intent intent = getIntent();
        	ipAdress= intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        	sensitivityBar = (SeekBar) findViewById(R.id.sensitivityBar);
        	sensitivityBar.setOnSeekBarChangeListener(
        			new OnSeekBarChangeListener() {
        				int sensitivityBarValue = 0;
        				@Override
        				public void onProgressChanged(SeekBar seekBar,int progresValue, boolean fromUser) {
        					sensitivityBarValue = progresValue;
        				}
	        			@Override
	        			public void onStartTrackingTouch(SeekBar seekBar) {
	            
	        			}
	        			@Override
				        public void onStopTrackingTouch(SeekBar seekBar) {
				            drawView.sensitivity=sensitivityBarValue+50;
				       }
	        		});
        	drawView = new DrawView(this.getApplicationContext());
    		connectIntent = new Intent();
    		connection = new Connection(ipAdress, getApplicationContext(), drawView);
        	dialog = new ProgressDialog(this);
        	new LoadingScreen().execute("");
        	Intent msgIntent = new Intent(this, SendData.class);
        	startService(msgIntent);
        	
        	msgHandlerIntent = new Intent(this, msgListenerIntent.class);
        	startService(msgHandlerIntent);
        	
        	msgReceiveHandlerIntent = new Intent(this, msgReceiveListenerIntent.class);
        	startService(msgReceiveHandlerIntent);
        } 
		catch (Exception e) {
			System.out.println("TCP Error: " + e.toString());
        }
    }
	@Override
	protected void onDestroy(){
		super.onDestroy();
		connection.cleanUp();
	}
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_message, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure.
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class LoadingScreen extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params){
    		connection.run();
        	return "Executed";
        }

        @Override
        protected void onPostExecute(String result){
        	hide();
        	if(Connection.isConnected){
            	startService(msgReceiveHandlerIntent);
        	}else{
        		Toast.makeText(getApplicationContext(), "Unable to connect",Toast.LENGTH_LONG).show();
        		finish();
        	}
        }
        
        @Override
        protected void onPreExecute(){
        	show("Attempting to connect to server...");
        }
        
        @Override
        protected void onProgressUpdate(Void... values){}
    }
	
	void show(String str) {
	    dialog.setMessage(str);
	    dialog.show();
	}

	void hide() {
	    dialog.dismiss();
	}
}
