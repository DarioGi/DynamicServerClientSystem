package com.example.AndroidMouse;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

@SuppressWarnings("deprecation")
public class TabbedActivity extends TabActivity {
	public final static String EXTRA_MESSAGE = "com.example.AndroidMouse.MESSAGE";

	private TabHost mTabHost;
	private String ipAdress;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tabbed);
		
		Intent thisIntent = getIntent();
    	ipAdress= thisIntent.getStringExtra(MainActivity.EXTRA_MESSAGE);
		TabHost.TabSpec spec;
		Intent intent;
		
		mTabHost = getTabHost();
		intent = new Intent(this, PandoraPlayerActivity.class);
		spec = mTabHost.newTabSpec("Pandora Player")
				.setIndicator("Pandora")
				.setContent(intent);
		mTabHost.addTab(spec);
		
		intent = new Intent(this, ControlMouseActivity.class);
		spec = mTabHost.newTabSpec("Mouse Control")
				.setIndicator("Mouse")
				.setContent(intent);
		intent.putExtra(EXTRA_MESSAGE,ipAdress); 
		mTabHost.addTab(spec);
		
		mTabHost.setCurrentTab(1);
		
	}
}
