package multi_device_app;


import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import javax.swing.JFrame;

/* This class creates a socket thread which listens for clients and parses the 
 * client data and settings */
public class listenSocket extends Thread 
{
    private static final int maxNumOfClients = 20;
    private static final int standbyTime = 100; //ms
    private ServerSocket server;
    private Vector userClients = new Vector(maxNumOfClients);
    private int clientID;
    private int numOfClients;
    private JFrame window;
    private BlockingQueue receiverMsgQueue, senderMsgQueue;
    private SenderThread senderThread;
    
    public listenSocket(JFrame window, BlockingQueue receiverMsgQueue, BlockingQueue senderMsgQueue)
    {
        super();
        this.window = window;
        this.receiverMsgQueue = receiverMsgQueue;
        this.senderMsgQueue = senderMsgQueue;
        clientID = 0;
        numOfClients = 0;
    }
    @Override
    public void run()
    {
        try
        {
            server=new ServerSocket(4321, maxNumOfClients); // Attemps to open up a socket at 4321
            senderThread = new SenderThread(senderMsgQueue, userClients); // Setup sender queue which sends messages back to clients
            senderThread.start();
            String msg = " Waiting for a clients to connect on port: " + server.getLocalPort();
            serverFrame.processMessage(msg);
            
            while(true)
            {
                try
                {
                    listenForClients(); 
                }
                catch(EOFException e)
                {
                    serverFrame.processMessage(" Server ended the connection");
                }
                catch(IOException e)
                {
                    serverFrame.processMessage(" Error while setting up client: \n" + e);
                }
            }
        }
        catch(IOException e)
        {
            serverFrame.processMessage(e.toString());
            System.exit(-1);
        }
    }
    
    //Waits for connection, then displays the information about the client
    private void listenForClients() throws IOException
    {
        threadStandby();
        userClients.add(new userClient(clientID,this));
        userClient client = getClient(clientID);
        if ( client == null )
            return; //Not found
        client.waitForClient();
        numOfClients++;
        updateWindowTitle();
        serverFrame.processMessage("A client joined the server (" + 
                client.getSocket().getInetAddress().getHostName()+")");
        clientID++;
    }
    
    private void threadStandby()
    {
        
        while ( numOfClients >= maxNumOfClients )
        {
            try 
            {
                Thread.sleep(standbyTime);
            } 
            catch(InterruptedException ex) 
            {
            }
            //Server is full.
        }
    }
    
    protected ServerSocket getServer()
    {
        return this.server;
    }
    
    protected BlockingQueue getReceiverMsgQueue()
    {
        return this.receiverMsgQueue;
    }
    
    private userClient getClient(int clientId)
    {
        Iterator i = userClients.iterator();
        while (i.hasNext()) 
        {
            userClient temp = (userClient) i.next();
            if ( temp.getClientId() == clientId )
                return temp;
        }
        return null;
    }
    
    public void disconnectClient(int clientId)
    {
        Iterator i = userClients.iterator();
        while (i.hasNext()) 
        {
            userClient temp = (userClient) i.next();
            if ( temp.getClientId() == clientId )
            {
                userClients.remove(temp);
                numOfClients--;
                updateWindowTitle();
                return;
            }
        }
    }
    
    private void updateWindowTitle()
    {
        String title = String.format("%s Clients: %d/%d", LaunchServer.getAppName(), numOfClients, maxNumOfClients);
        window.setTitle(title);
    }
}
        

    

