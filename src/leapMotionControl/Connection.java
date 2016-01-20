package leapcontroller;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/*--- Connection.java ---
 * The class is responsible for establishing the connection to the server.
 * Upon successful connection to the server ObjectOutputStream and ObjectIntputStream
 * are setup which first wait for a server to send a "SETUP" message. 
 * After connection is closed or interrupted, cleanUp() method is responsible for 
 * cleaning up the environment.
 */

public class Connection extends Thread {

    protected ObjectOutputStream output; // Gives other classes the access
    protected ObjectInputStream input;   // Gives other classes the access
    private Socket connection;
    private String message = "";
    private String hostname;
    public boolean isConnected = false;
    public boolean lostConnection = false;
    public boolean isSetup = false;
    public String statusMessage = "";
    private int ScreenDimensionX;
    private int ScreenDimensionY;
    protected ApplicationGUI parent;
    private SendData sender;

    private Connection(){}
    public Connection(String hostname, ApplicationGUI parent) {
        this.hostname = hostname;
        this.parent = parent;
    }

    @Override
    public void run() {
        try {
            connectToServer();
            setupStreams();
            whileConnected();
        } catch (EOFException e) {
            statusMessage = "";
            parent.connectionStatus(false);
        } catch (IOException e) {
            statusMessage = e.toString();
            parent.connectionStatus(false);
        } finally {
            //cleanUp(); //Not using a thread. 
        }
    }

    // connecting to the server
    private void connectToServer() throws IOException {
        parent.processMessage("Attempting to connect to " + hostname + "...");
        connection = new Socket(InetAddress.getByName(hostname), 4321);
        connection.setSendBufferSize(256);
        parent.processMessage("Connected to " + hostname);
    }

    // setting up the streams
    private void setupStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());;
    }

    // while connected to the server, send data...
    private void whileConnected() throws IOException {
        //isConnected = true;
        do {
            try {
                message = (String) input.readObject();
                if (message.regionMatches(true, 0, "SETUP", 0, 5)) {
                    int count = 0;
                    int tempInt = 0;
                    for (int i = 0; i < message.length(); i++) {
                        if (message.charAt(i) == '-') {
                            if (count == 0) {
                                tempInt = i;
                            }
                            if (count == 1) {
                                ScreenDimensionX = Integer.parseInt(message.substring(tempInt + 1, i));
                                ScreenDimensionY = Integer.parseInt(message.substring(i + 1, message.length()));
                                output.writeObject("CLIENT-SETUP-LEAP");
                                parent.processMessage("Host ScreenSize X: " + ScreenDimensionX
                                        + " Host ScreenSize Y: " + ScreenDimensionY);
                                output.flush();
                                isSetup = true;
                                sender = new SendData(this, parent.msgQueue);
                                sender.start();
                                isConnected = true;
                                parent.connectionStatus(true);
                                break;
                            }
                            count++;
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                cleanUp();
            }
        } while (!isSetup);
        do
        {
            try
            {
                Thread.sleep(250);
            }
            catch (InterruptedException e)
            {
                parent.processMessage("Exception while sleeping: " + e);
                cleanUp();
            }
        }while (isConnected);
        cleanUp();
    }

    public void cleanUp() {
        try {
            if (output != null && input != null) {
                try {
                    output.writeObject("CLIENT - END");
                    output.flush();
                } catch (IOException e) {
                    parent.processMessage("Cannot send a disconnect message to the server!");
                }
                output.close();
                input.close();
                connection.close();
                parent.processMessage("Disconnected.");
            }
        } catch (IOException e) {
            parent.processMessage("Unable to end the connection gracefully: " + e);
        }
        finally
        {
            parent.connectionStatus(false);
        }
    }
}