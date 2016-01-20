/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multi_device_app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author Darius
 */
public class userClient {
    
    private int clientID;
    private Socket socket;
    private ClientThread clientThread;
    private ServerSocket server;
    private listenSocket listenSocket;
    private boolean isClientThreadSetup = false;
    
    public userClient(int id, listenSocket listenSocket)
    {
        this.clientID = id;
        socket = new Socket();
        this.listenSocket = listenSocket;
        this.server = listenSocket.getServer();
    }
    
    public int waitForClient() throws IOException
    {
        try
        {
            socket = server.accept();
            clientThread = new ClientThread(listenSocket, socket, this);
            clientThread.start();
            return 0;
        }
        catch ( IOException e)
        {
            return -1;
        }
    }
    
    public int getClientId()
    {
        return this.clientID;
    }
    
    public Socket getSocket()
    {
        return this.socket;
    }
    protected void disconnectClient()
    {
        listenSocket.disconnectClient(clientID);
    }
    protected int getClientThreadType()
    {
        return clientThread.getSensorType();
    }
    
    protected BlockingQueue getClientThreadSenderQueue()
    {
        return clientThread.getClientSenderQueue();
    }
    
    protected void setClientThreadStatus()
    {
            this.isClientThreadSetup = true;
    }
    
    public boolean getClientThreadStatus()
    {
        return this.isClientThreadSetup;
    }
}
