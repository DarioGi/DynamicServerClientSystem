package com.example.AndroidMouse;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.widget.Toast;
/*--- Connection.java ---
 * The class is responsible for establishing the connection to the server.
 * Upon successful connection to the server ObjectOutputStream and ObjectIntputStream
 * are setup which first wait for a server to send a "SETUP" message. 
 * After connection is closed or interrupted, cleanUp() method is responsible for 
 * cleaning up the environment.
 */

public class Connection {
    public static ObjectOutputStream output; // Gives other classes the access
    public static ObjectInputStream input;   // Gives other classes the access
    public static BlockingQueue msgQueue, receiverMsgQueue;
    private Socket connection;
    private String message="";
    private String hostname;
    public static boolean isConnected=false;
    public boolean lostConnection=false;
    public static boolean isSetup=false;
    public String statusMessage="";
    private Context parentContext;
    
    public Connection(String hostname, Context context, DrawView drawView){
    	
    	this.hostname=hostname;
    	this.parentContext = context;
    	Connection.msgQueue = new ArrayBlockingQueue(512);
    	Connection.receiverMsgQueue = new ArrayBlockingQueue(512);
    }
    
	public void run(){
		try{
			connectToServer();
			setupStreams();
			whileConnected();
		}catch(EOFException e){
        	statusMessage="";
		}catch(IOException e){
			statusMessage=e.toString();
		}finally{
			//cleanUp(); //Not using a thread. 
		}
	}
	
	// connecting to the server
	private void connectToServer() throws IOException{
		System.out.println("Attempting to connect to "+ hostname);
		connection = new Socket(InetAddress.getByName(hostname),4321);
		
		//connection.setSendBufferSize(256);
		System.out.println("Connected to "+ hostname);
	}
	
	// setting up the streams
	private void setupStreams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());;
	}
	
	// while connected to the server, send data...
	private void whileConnected() throws IOException{
		
		do{
			try{
				message=(String)input.readObject();
				if(message.regionMatches(true, 0, "SETUP", 0, 5)){
					int count=0;
					int tempInt=0;
			        for(int i=0; i<message.length();i++){
			            if(message.charAt(i)== '-'){
			                if(count == 0){
			                    tempInt=i;
			                }
			                if(count == 1){
			                	DrawView.ScreenDimensionX=Integer.parseInt(message.substring(tempInt+1, i));
			                    DrawView.ScreenDimensionY=Integer.parseInt(message.substring(i+1, message.length()));
			                    output.writeObject("CLIENT-SETUP-ANDROID");
			                    System.out.println("ScreenSize X: " + DrawView.ScreenDimensionX +
			                    		"ScreenSize Y: " + DrawView.ScreenDimensionY);
			                    output.flush();
			                    isConnected=true;
			                    isSetup=true;
			                    break;
			                }
			                count++;
			            }
			        }	
				}
			}catch(ClassNotFoundException e){}
		}while(!isSetup);
	}
	
	public void cleanUp(){
		isConnected=false;
		try{
            if(output != null && input != null){
            	try{
            		output.writeObject("CLIENT - END");
            		output.flush();
            	}catch(IOException e){
            		System.out.println("Cannot send a disconnect message to the server!");
            	}
                output.close();
                input.close();
                connection.close();
            }
        }catch(IOException e){
        	Toast.makeText(this.parentContext, "Unable to connect",Toast.LENGTH_LONG).show();
        	System.out.println("Unable to end the connection gracefully: "+ e);
        }
	}
	
	

}
