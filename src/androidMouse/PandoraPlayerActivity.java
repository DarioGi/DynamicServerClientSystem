package com.example.AndroidMouse;
import android.app.Activity;

import android.content.BroadcastReceiver;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

public class PandoraPlayerActivity extends Activity {

	private BroadcastReceiver msgReceiver;
	private boolean isPaused = false;
	private SeekBar seekBar;
	private int playerVolume = 50;
	private boolean changeVolume = true;

	//public EditText artist;
	//public EditText song;
	//public EditText album;
	//(EditText) findViewById(R.id.pandora_editTextCurrentArtist)
	//(EditText) findViewById(R.id.pandora_editTextCurrentSong)
	//(EditText) findViewById(R.id.pandora_editTextCurrentAlbum)
	//Boolean myReceiverIsRegistered = false;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//artist = (EditText) findViewById(R.id.pandora_editTextCurrentArtist);
		//song = (EditText) findViewById(R.id.pandora_editTextCurrentSong);
		//album = (EditText) findViewById(R.id.pandora_editTextCurrentAlbum);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.example.AndroidMouse.TrackInfo");
		setContentView(R.layout.activity_pandora_player);
		seekBar = (SeekBar) findViewById(R.id.pandora_volumeSeekBar);
		msgReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				System.out.println("Got message");
				EditText artistText = (EditText) findViewById(R.id.pandora_editTextCurrentArtist);
				artistText.setText(msgReceiveListenerIntent.getArtist());
				EditText songText = (EditText) findViewById(R.id.pandora_editTextCurrentSong);
				songText.setText(msgReceiveListenerIntent.getSong());
				EditText albumText = (EditText) findViewById(R.id.pandora_editTextCurrentAlbum);
				albumText.setText(msgReceiveListenerIntent.getAlbum());
				EditText timeElapsedText = (EditText) findViewById(R.id.pandora_editTextCurrentElapsedTime);
				timeElapsedText.setText(msgReceiveListenerIntent.getTimeElapsed());
				EditText statusText = (EditText) findViewById(R.id.pandora_editTextCurrentStatus);
				String curState = msgReceiveListenerIntent.getCurrentState();
				statusText.setText(curState);
				int curVolume = msgReceiveListenerIntent.getPlayerVolume();
				if ( curState.contains(pandoraMsgType.pStatePaused) )
				{
					if (!isPaused)
					{
						Button buttonPaused = (Button) findViewById(R.id.pandora_buttonPause);
						buttonPaused.setText("Play");
						Toast.makeText(getApplicationContext(), "Paused.", Toast.LENGTH_SHORT).show();
						isPaused = true;
					}
				}
				else if (curState.contains(pandoraMsgType.pStatePlaying) )
				{
					if ( isPaused )
					{
						Button buttonPaused = (Button) findViewById(R.id.pandora_buttonPause);
						buttonPaused.setText("Pause");
						Toast.makeText(getApplicationContext(), "Playing.", Toast.LENGTH_SHORT).show();
						isPaused = false;
					}
				}		
			
				if (curVolume != playerVolume )
				{
					seekBar.setProgress(curVolume);
					curVolume = playerVolume;
				}
			}
		};
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int progress = 0;

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				if (Connection.isConnected)
				{
					if ( Connection.msgQueue.remainingCapacity() > 1)
						Connection.msgQueue.add(new pandoraMsgType(pandoraMsgType.msgPandoraVolume,progress,true));
				}
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int prog, boolean fromUser) {
				 progress = prog;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
		});
		registerReceiver(msgReceiver, filter);
	}
	
	@SuppressWarnings("unchecked")
	public void OnClickPlay(View view)
	{
		if (Connection.isConnected)
		{
			EditText editTextArtist=(EditText) findViewById(R.id.editTextArtist);
			String textArtist = editTextArtist.getText().toString();
			if (!textArtist.trim().isEmpty())
			{
				if ( Connection.msgQueue.remainingCapacity() > 1)
					Connection.msgQueue.add(new pandoraMsgType(textArtist.trim()));
			}
			else
			{
				Toast.makeText(getApplicationContext(), "Enter an Artist name.", Toast.LENGTH_SHORT).show();
			}
		}
		else
		{
			Toast.makeText(getApplicationContext(), "Not connected.", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void OnClickPause(View view)
	{
		if (Connection.isConnected)
		{
			if ( Connection.msgQueue.remainingCapacity() > 1)
			{
				Button buttonPaused = (Button) findViewById(R.id.pandora_buttonPause);
				if ( isPaused )
				{
					Connection.msgQueue.add(new pandoraMsgType(pandoraMsgType.msgPandoraPlay,true));
					buttonPaused.setText("Pause");
					Toast.makeText(getApplicationContext(), "Playing.", Toast.LENGTH_SHORT).show();
					isPaused = false;
				}
				else
				{
					Connection.msgQueue.add(new pandoraMsgType(pandoraMsgType.msgPandoraPause,true));
					buttonPaused.setText("Play");
					Toast.makeText(getApplicationContext(), "Paused.", Toast.LENGTH_SHORT).show();
					isPaused = true;
				}
			}
		}
		else
		{
			Toast.makeText(getApplicationContext(), "Not connected.", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void OnClickThumbsUp(View view)
	{
		if (Connection.isConnected)
		{
			if ( Connection.msgQueue.remainingCapacity() > 1)
			{
				Connection.msgQueue.add(new pandoraMsgType(pandoraMsgType.msgPandoraThumbsUp,true));
				Toast.makeText(getApplicationContext(), "Liked!", Toast.LENGTH_SHORT).show();
			}
		}
		else
		{
			Toast.makeText(getApplicationContext(), "Not connected.", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void OnClickThumbsDown(View view)
	{
		if (Connection.isConnected)
		{
			if ( Connection.msgQueue.remainingCapacity() > 1)
			{
				Connection.msgQueue.add(new pandoraMsgType(pandoraMsgType.msgPandoraThumbsDown,true));
				Toast.makeText(getApplicationContext(), "Disliked!", Toast.LENGTH_SHORT).show();
			}
		}
		else
		{
			Toast.makeText(getApplicationContext(), "Not connected.", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void OnClickNext(View view)
	{
		if (Connection.isConnected)
		{
			if ( Connection.msgQueue.remainingCapacity() > 1)
			{
				Connection.msgQueue.add(new pandoraMsgType(pandoraMsgType.msgPandoraNext,true));
				Toast.makeText(getApplicationContext(), "Next.", Toast.LENGTH_SHORT).show();
			}
		}
		else
		{
			Toast.makeText(getApplicationContext(), "Not connected.", Toast.LENGTH_SHORT).show();
		}
	}
}
