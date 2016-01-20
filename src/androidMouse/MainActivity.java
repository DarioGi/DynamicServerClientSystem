package com.example.AndroidMouse;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.*;
import android.widget.EditText;


public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.example.AndroidMouse.MESSAGE";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public void connectPrompt(View view) {
    	Intent intent = new Intent(this,TabbedActivity.class);
    	EditText ipAdress=(EditText) findViewById(R.id.textfield_enterIP);
    	String message = ipAdress.getText().toString();
    	intent.putExtra(EXTRA_MESSAGE,message);
    	this.startActivity(intent);
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      EditText ipAdress=(EditText) findViewById(R.id.textfield_enterIP);
      String message = ipAdress.getText().toString();
      savedInstanceState.putString("IPAdress", message);
    }
    
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	  super.onRestoreInstanceState(savedInstanceState);
    	  EditText ipAdress = (EditText) findViewById(R.id.textfield_enterIP);
    	  ipAdress.setText(savedInstanceState.getString("IPAdress"));
    }
    
}
