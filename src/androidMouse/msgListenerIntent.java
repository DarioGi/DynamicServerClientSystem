package com.example.AndroidMouse;

import java.io.IOException;

import android.app.IntentService;
import android.content.Intent;

public class msgListenerIntent extends IntentService {
	
	public msgListenerIntent() {
		super("msgListenerIntent");
	}
	
	protected void onHandleIntent(Intent workIntent) {
		while ( Connection.isConnected || !Connection.isSetup )
		{
			if ( !Connection.msgQueue.isEmpty() )
	        {
	            if ( Connection.msgQueue.peek() instanceof pandoraMsgType )
	            {
	                sendPandoraMsg((pandoraMsgType) Connection.msgQueue.remove());
	            }
	            else if ( Connection.msgQueue.peek() instanceof mouseMsgType ) 
	            {
	            	sendMouseMsg((mouseMsgType) Connection.msgQueue.remove());
	            }
	        }
	        else
	        {
	            try
	            {
	                Thread.sleep(50); // Queue empty - poll!
	            }
	            catch (InterruptedException e)
	            {
	                //connection.parent.processMessage("Exception while sleeping in SendData thread: " + e);
	            }
	        }
		}
		this.onDestroy();
	}
	
	 private void sendPandoraMsg(pandoraMsgType msg)
	 {
		 if (!msg.getBandName().isEmpty())
		 {
			 try {
					Connection.output.writeObject(":PLAY_NEW:" + msg.getBandName().trim());
					Connection.output.flush();
				} catch (IOException e) {}
		 }
		 else if (msg.getThumbDown())
		 {
			 try {
					Connection.output.writeObject(":THUMBSDOWN:");
					Connection.output.flush();
				} catch (IOException e) {}
		 } 
		 else if (msg.getSend())
		 {
			 if (msg.getPlayerState().contains(pandoraMsgType.msgPandoraPause))
			 {
				 try {
						Connection.output.writeObject(pandoraMsgType.msgPandoraPause);
						Connection.output.flush();
					} catch (IOException e) {}
			 }
			 else if (msg.getPlayerState().contains(pandoraMsgType.msgPandoraPlay))
			 {
				 try {
						Connection.output.writeObject(pandoraMsgType.msgPandoraPlay);
						Connection.output.flush();
					} catch (IOException e) {}
			 }
			 else if (msg.getPlayerState().contains(pandoraMsgType.msgPandoraNext))
			 {
				 try {
						Connection.output.writeObject(pandoraMsgType.msgPandoraNext);
						Connection.output.flush();
					} catch (IOException e) {}
			 }
			 else if (msg.getPlayerState().contains(pandoraMsgType.msgPandoraThumbsDown))
			 {
				 try {
						Connection.output.writeObject(pandoraMsgType.msgPandoraThumbsDown);
						Connection.output.flush();
					} catch (IOException e) {}
			 }
			 else if (msg.getPlayerState().contains(pandoraMsgType.msgPandoraThumbsUp))
			 {
				 try {
						Connection.output.writeObject(pandoraMsgType.msgPandoraThumbsUp);
						Connection.output.flush();
					} catch (IOException e) {}
			 }
			 else if (msg.getPlayerState().contains(pandoraMsgType.msgPandoraVolume))
			 {
				 try {
						Connection.output.writeObject(pandoraMsgType.msgPandoraVolume+msg.getPlayerVolume());
						Connection.output.flush();
					} catch (IOException e) {}
			 }
		 }
	}
	
	 private void sendMouseMsg(mouseMsgType msg)
	 {
		 try
		 {
			Connection.output.writeObject(":MOUSE_POS:" + msg.getLocX() + "-" + msg.getLocY() + "-" + msg.getIsLeftClick());
	     	Connection.output.flush();
		 }
		 catch(IOException e){
  		
		}
	 }
}
