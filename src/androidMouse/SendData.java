package com.example.AndroidMouse;

import android.app.IntentService;
import android.content.Intent;

public class SendData extends IntentService{
	
	public SendData() {
		super("SendData");
		
	}
	public static int XLoc = 0;
	public static int YLoc = 0;
	public static boolean sendLeftClick = false;
	
	private int oldXLoc = 0;
	private int oldYLoc = 0;
	private boolean oldSendLeftClick = false;
	private boolean runIntent = true;
	
	
	@SuppressWarnings("unchecked")
	protected void onHandleIntent(Intent workIntent) {
		do{
			if(Connection.isConnected){
				//if(DrawView.isOnDraw){ //Remove commend around DrawView.isOnDraw for lower resource use but worse performance.
				try{
					
					if ( XLoc != oldXLoc || YLoc != oldYLoc || oldSendLeftClick != sendLeftClick )
					{
						oldXLoc = XLoc;
						oldYLoc = YLoc;
						oldSendLeftClick = sendLeftClick;
						if ( Connection.msgQueue.remainingCapacity() > 1)
							Connection.msgQueue.add(new mouseMsgType(XLoc,YLoc,sendLeftClick));
			        	if(sendLeftClick)
			        		sendLeftClick=false;
					}
					else
					{
						try{
			                Thread.sleep(50); // Queue empty - poll!
			            }
			            catch (InterruptedException e){
			            }
					}
				}catch(Exception e){
					runIntent = false;
	        		break;
				}
			}
    	}while(runIntent);
		this.onDestroy();
	}
	
	
}
