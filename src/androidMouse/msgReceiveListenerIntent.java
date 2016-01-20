package com.example.AndroidMouse;

import java.io.IOException;
import java.io.OptionalDataException;
import java.util.StringTokenizer;

import android.app.IntentService;
import android.content.Intent;

public class msgReceiveListenerIntent extends IntentService {

	private final String msgTrackInfo = ":TRACK_INFO:";
	private static String Artist;// = "unknown";
	private static String Song = "unknown";
	private static String Album = "unknown";
	private static String CurrentState = "unknown";
	private static String TimeElapsed = "0:00";
	private static int PlayerVolume = 50;
	private final static Object lockArtist = new Object();
	private final static Object lockAlbum = new Object();
	private final static Object lockSong= new Object();
	private final static Object lockCurrentState = new Object();
	private final static Object lockTimeElapsed = new Object();
	private final static Object lockPlayerVolume = new Object();
	private Object msg;
	
	public msgReceiveListenerIntent()
	{
		super("msgReceiveListenerIntent");
		this.msg = "";
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		do{
			try {
				if (Connection.isSetup)
					msg = (String) Connection.input.readObject();
			} catch (OptionalDataException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if ( !((String)msg).isEmpty() )
				parseInput((String)msg);
			else
			{
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		while(Connection.isConnected || !Connection.isSetup);
		
	}
	
	private void parseInput(String msg)
	{
		if ( msg.contains(msgTrackInfo) )
		{
			String tempMsg = msg.substring(msgTrackInfo.length());
			
			StringTokenizer st = new StringTokenizer(tempMsg, "::"); 
			setArtist(st.nextToken()); 
			setSong(st.nextToken()); 
			setAlbum(st.nextToken());
			setCurrentState(st.nextToken());
			setPlayerVolume(Integer.parseInt(st.nextToken()));
			setTimeElapsed(st.nextToken()+":"+st.nextToken());
			Intent intent = new Intent();
			intent.setAction("com.example.AndroidMouse.TrackInfo");
			intent.putExtra("TrackInfo", 1);
			sendBroadcast(intent);
		}
	}
	
	public static void setArtist(String s){
		synchronized(lockArtist){
			Artist = s;
		}
	}
	
	public static void setSong(String s){
		synchronized(lockSong){
			Song = s;
		}
	}
	
	public static void setAlbum(String s){
		synchronized(lockAlbum){
			Album = s;
		}
	}
	
	public static void setTimeElapsed(String s){
		synchronized(lockTimeElapsed){
			TimeElapsed = s;
		}
	}
	
	public static void setCurrentState(String s){
		synchronized(lockCurrentState){
			CurrentState = s;
		}
	}
	
	public static void setPlayerVolume(int vol)
	{
		synchronized(lockPlayerVolume){
			PlayerVolume = vol;
		}
	}
	
	public static String getArtist(){
		synchronized(lockArtist){
			return Artist;
		}
	}
	
	public static String getSong(){
		synchronized(lockSong){
			return Song;
		}
	}
	
	public static String getAlbum(){
		synchronized(lockAlbum){
			return Album;
		}
	}
	
	public static String getTimeElapsed(){
		synchronized(lockTimeElapsed){
			return TimeElapsed;
		}
	}
	
	public static String getCurrentState(){
		synchronized(lockCurrentState){
			return CurrentState;
		}
	}
	public static int getPlayerVolume()
	{
		synchronized(lockPlayerVolume){
			return PlayerVolume;
		}
	}
	
}
