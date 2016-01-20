/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multi_device_app;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import static multi_device_app.serverFrame.processMessage;

/**
 *
 * @author Darius
 */
public class ClientThread extends Thread {

    private ObjectOutputStream output;
    private ObjectInputStream input;
    protected Socket clientSocket;
    private boolean LeftClick = false;
    private listenSocket parentSocket;
    private userClient userClient;
    private int sensorType;
    public static final int typeAndroid = 100;
    public static final int typeLeap = 101;
    public static final int typeKinect = 102;
    private boolean isSetup;
    private final String msgSwipe = ":SWIPE:";
    private final String msgMousePos = ":MOUSE_POS:";
    private final String msgTrackInfo = ":TRACK_INFO:";
    private final BlockingQueue senderQueue;
    private boolean keepAlive = true;
    private ClientThreadReceiver receiverThread;

    public ClientThread(listenSocket parentSocket, Socket clientSocket, userClient userClient) {
        super();
        this.parentSocket = parentSocket;
        this.clientSocket = clientSocket;
        //this.server = parentSocket.getServer();
        this.userClient = userClient;
        this.senderQueue = new ArrayBlockingQueue(512);
        //this.clientReceiverQueue = new ArrayBlockingQueue(512);
        isSetup = false;
        userClient.setClientThreadStatus(); // Notify that initialized
    }

    @Override
    public void run() {
        try {
            setupStreams();
            whileConnected();
        } catch (EOFException e) {
            serverFrame.processMessage(String.format(
                    "Client[%d]:Server ended the connection:\n%s", userClient.getClientId(), e));
        } catch (IOException e) {
            serverFrame.processMessage(String.format(
                    "Client[%d]:Error while setting up client:\n%s", userClient.getClientId(), e));
        } finally {
            cleanUp();
        }
    }

    private void setupStreams() throws IOException {
        output = new ObjectOutputStream(clientSocket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(clientSocket.getInputStream());
        serverFrame.processMessage(String.format(
                "Client[%d]: The streams were succesfully set up.", userClient.getClientId()));
        receiverThread = new ClientThreadReceiver(this);
        receiverThread.start();
        OutputSetupParse(new DeviceSetupMsgType((int) MouseController.screenSize.getWidth(), (int) MouseController.screenSize.getHeight()));
    }

    private void whileConnected() {
        do {
            if (!isSetup) {
                try {
                    Thread.sleep(25);//Sleep
                } catch (InterruptedException e) {
                    serverFrame.processMessage(String.format(
                            "Client[%d]: Interrupted exception. ", userClient.getClientId()));
                    keepAlive = false;
                }
            } else {
                if (!senderQueue.isEmpty()) {
                    if (senderQueue.peek() instanceof PandoraPlayerMsgType) {
                        OutputAndroid((PandoraPlayerMsgType) senderQueue.remove());
                    }
                } else {
                    try {
                        Thread.sleep(25);//Sleep
                    } catch (InterruptedException e) {
                        serverFrame.processMessage(String.format(
                                "Client[%d]: Interrupted exception. ", userClient.getClientId()));
                        keepAlive = false;
                    }
                }
            }
        } while (keepAlive);
    }
    // Parses the messages received from client which are in the form of:
    // "XCoordinate-YCoordinate-isLeftCLick"

    public void parseInputLeap(String str) {
        if (str != null && !str.equalsIgnoreCase("")) {
            try {
                if (str.contains(msgSwipe)) {
                    int index = str.indexOf(msgSwipe);
                    if (str.charAt(index + msgSwipe.length()) == 'R') {
                        serverFrame.processMessage("Client " + userClient.getClientId() + ": Swipe Right.");
                        parentSocket.getReceiverMsgQueue().add(new PandoraPlayerMsgType(true));
                    } else if (str.charAt(index + msgSwipe.length()) == 'L') {
                        serverFrame.processMessage("Client " + userClient.getClientId() + ": Swipe Left. (thumbs down)");
                        parentSocket.getReceiverMsgQueue().add(new PandoraPlayerMsgType(false, true));
                    } else {
                        serverFrame.processMessage("Received unparsable Swipe "
                                + "message from client " + userClient.getClientId() + ".");
                    }
                }
            } catch (Exception e) {
                /*serverFrame.processMessage("The client " + 
                 clientSocket.getInetAddress().getHostName() + 
                 " sent an unparsable message");*/
                //Detects unparsable message from the client
            }
        }
    }

    protected void parseInputAndroid(String str) {
        int tempInt = 0, count = 0, x = 0, y = 0;
        if (str == null || str.equalsIgnoreCase("")) {
            return;
        }
        if (str.contains(msgMousePos)) {
            try {
                for (int i = msgMousePos.length(); i < str.length(); i++) {
                    if (str.charAt(i) == '-') {
                        if (count == 0) {
                            tempInt = i;
                            x = Integer.parseInt(str.substring(msgMousePos.length(), i));
                        }
                        if (count == 1) {
                            y = Integer.parseInt(str.substring(tempInt + 1, i));
                            LeftClick = Boolean.parseBoolean(str.substring(i + 1, str.length()));
                            System.out.println(x + " " + y);
                            break;
                        }
                        count++;
                    }
                }
                parentSocket.getReceiverMsgQueue().add(new MouseControlMsgType(x, y, LeftClick));
            } catch (Exception e) {
                serverFrame.processMessage(String.format(
                        "Client[%d]:Problem parsing Android"
                        + " Mouse Controller message.", userClient.getClientId()));
            }
        } else if (str.contains(PandoraPlayer.msgPandoraPlayNew)) {
            try {
                if (parentSocket.getReceiverMsgQueue().remainingCapacity() > 1) {
                    parentSocket.getReceiverMsgQueue().put(new PandoraPlayerMsgType(str.substring(PandoraPlayer.msgPandoraPlayNew.length(), str.length())));
                }
            } catch (Exception e) {
                serverFrame.processMessage(String.format(
                        "Client[%d]:Problem parsing Android"
                        + " Pandora Player message.", userClient.getClientId()));
            }
        } else if (str.contains(PandoraPlayer.msgPandoraPlay)) {
            try {
                if (parentSocket.getReceiverMsgQueue().remainingCapacity() > 1) {
                    parentSocket.getReceiverMsgQueue().put(new PandoraPlayerMsgType(PandoraPlayer.PLAY, false));
                }
            } catch (Exception e) {
                serverFrame.processMessage(String.format(
                        "Client[%d]:Problem parsing Android"
                        + " Pandora Player message.", userClient.getClientId()));
            }
        } else if (str.contains(PandoraPlayer.msgPandoraPause)) {
            try {
                if (parentSocket.getReceiverMsgQueue().remainingCapacity() > 1) {
                    parentSocket.getReceiverMsgQueue().put(new PandoraPlayerMsgType(PandoraPlayer.PAUSE, false));
                }
            } catch (Exception e) {
                serverFrame.processMessage(String.format(
                        "Client[%d]:Problem parsing Android"
                        + " Pandora Player message.", userClient.getClientId()));
            }
        } else if (str.contains(PandoraPlayer.msgPandoraThumbsDown)) {
            try {
                if (parentSocket.getReceiverMsgQueue().remainingCapacity() > 1) {
                    parentSocket.getReceiverMsgQueue().put(new PandoraPlayerMsgType(PandoraPlayer.THUMBDOWN, false));
                }
            } catch (Exception e) {
                serverFrame.processMessage(String.format(
                        "Client[%d]:Problem parsing Android"
                        + " Pandora Player message.", userClient.getClientId()));
            }
        } else if (str.contains(PandoraPlayer.msgPandoraThumbsUp)) {
            try {
                if (parentSocket.getReceiverMsgQueue().remainingCapacity() > 1) {
                    parentSocket.getReceiverMsgQueue().put(new PandoraPlayerMsgType(PandoraPlayer.THUMBUP, false));
                }
            } catch (Exception e) {
                serverFrame.processMessage(String.format(
                        "Client[%d]:Problem parsing Android"
                        + " Pandora Player message.", userClient.getClientId()));
            }
        }else if (str.contains(PandoraPlayer.msgPandoraSkip)) {
            try {
                if (parentSocket.getReceiverMsgQueue().remainingCapacity() > 1) {
                    parentSocket.getReceiverMsgQueue().put(new PandoraPlayerMsgType(PandoraPlayer.SKIP, false));
                }
            } catch (Exception e) {
                serverFrame.processMessage(String.format(
                        "Client[%d]:Problem parsing Android"
                        + " Pandora Player message.", userClient.getClientId()));
            }
        }else if (str.contains(PandoraPlayer.msgPandoraVolume)) {
            try {
                tempInt = Integer.parseInt(str.substring(PandoraPlayer.VOLUME.length() + 1, str.length()));
                if (tempInt >= 0 && tempInt <= 100) {
                    try {
                        if (parentSocket.getReceiverMsgQueue().remainingCapacity() > 1) {
                            parentSocket.getReceiverMsgQueue().put(new PandoraPlayerMsgType(PandoraPlayer.VOLUME, tempInt, false));
                        } else {
                            serverFrame.processMessage("Request cannot be completed: Too many requests made!");
                        }
                    } catch (InterruptedException e) {
                        serverFrame.processMessage(e.toString());
                    }
                }
//                if (parentSocket.getReceiverMsgQueue().remainingCapacity() > 1) {
//                    parentSocket.getReceiverMsgQueue().put(new PandoraPlayerMsgType(PandoraPlayer.VOLUME, false));
//                }
            } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
                serverFrame.processMessage(String.format(
                        "Client[%d]:Problem parsing Android"
                        + " Pandora Player message (Volume msg)", userClient.getClientId()));
            } catch (Exception e) {
                serverFrame.processMessage(String.format(
                        "Client[%d]:Problem parsing Android"
                        + " Pandora Player message.", userClient.getClientId()));
            }
        }
    }

    private void OutputAndroid(PandoraPlayerMsgType msg) {
        if (msg.getSender()) {
            try {
                output.writeObject(String.format("%s%s::%s::%s::%s::%s::%s",
                        msgTrackInfo,
                        msg.getArtist(),
                        msg.getSong(),
                        msg.getAlbum(),
                        msg.getPlayerState(),
                        msg.getPlayerVolume(),
                        msg.getElapsedTime()));
                output.flush();
            } catch (IOException e) {
                serverFrame.processMessage(String.format(
                        "Client[%d]: Client has disconnected, terminating... ", userClient.getClientId()));
                keepAlive = false;
            }
        }
    }

    // Executes when connection to the client has been terminated,
    // closes streams and sockets.
    private void cleanUp() {
        try {
            if (output != null && input != null) {
                output.close();
                input.close();
                serverFrame.processMessage(String.format(
                        "Client[%d]: Disconnected.", userClient.getClientId()));
                clientSocket.close();
            } else {
                serverFrame.processMessage(String.format(
                        "Client[%d]: Disconnected due to corrupted streams "
                        + "(bad connection)", userClient.getClientId()));
                clientSocket.close();
            }

        } catch (IOException e) {
            serverFrame.processMessage(String.format(
                    "Client[%d]: Unable to disconnect gracefully:"
                    + "\ne", userClient.getClientId(), e));
        } finally {
            userClient.disconnectClient();
        }
    }

    protected void parseInputSetup(String msg) {
        if (msg.equalsIgnoreCase("CLIENT-SETUP-ANDROID")
                || msg.equalsIgnoreCase("CLIENT-SETUP-LEAP")
                || msg.equalsIgnoreCase("CLIENT-SETUP-KINECT")) {
            if (msg.equalsIgnoreCase("CLIENT-SETUP-ANDROID")) {
                sensorType = typeAndroid;
                serverFrame.processMessage(String.format("Client[%d]: Android sensor detected.", userClient.getClientId()));
            } else if (msg.equalsIgnoreCase("CLIENT-SETUP-LEAP")) {
                sensorType = typeLeap;
                serverFrame.processMessage(String.format("Client[%d]: Leap sensor detected.", userClient.getClientId()));
            } else if (msg.equalsIgnoreCase("CLIENT-SETUP-KINECT")) {
                sensorType = typeKinect;
                serverFrame.processMessage(String.format("Client[%d]: Kinect sensor detected.", userClient.getClientId()));
            }
            isSetup = true;
        } else {
            serverFrame.processMessage(String.format("Client[%d]: Unknown message sent from client. Terminating...", userClient.getClientId()));
            keepAlive = false;
        }
    }

    private void OutputSetupParse(DeviceSetupMsgType msg) {
        if (msg.getScreenSizeX() != 0 && msg.getScreenSizeY() != 0) {
            try {
                output.writeObject("SETUP-"
                        + msg.getScreenSizeX()
                        + "-" + msg.getScreenSizeY());
                output.flush();
            } catch (IOException e) {
                serverFrame.processMessage(String.format(
                        "Client[%d]: I\\O interruption."
                        + "\ne", userClient.getClientId(), e));
                keepAlive = false;
            }
        }
    }

    protected BlockingQueue getClientSenderQueue() {
        return this.senderQueue;
    }

    protected ObjectInputStream getInputStream() {
        return this.input;
    }

    public int getSensorType() {
        return this.sensorType;
    }

    protected void setKeepAlive(boolean alive) {
        this.keepAlive = alive;
    }

    protected boolean getKeepAlive() {
        return this.keepAlive;
    }

    protected boolean getIsSetup() {
        return this.isSetup;
    }
}