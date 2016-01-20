/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multi_device_app;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.swing.JFrame;

/* This class is responsible for creating a frame and starting a thread 
 * which listens for clients and processes the data sent to the server.
 */
public class LaunchServer {

    private static JFrame mainFrame;
    private static String applicationName = "App Control";
    private static PandoraPlayer player;
    private static MouseController mouseController;
    private static BlockingQueue pandoraMsgQueue;
    public static BlockingQueue mouseMsgQueue;
    private static BlockingQueue senderQueue;
    private static BlockingQueue receiverQueue;
    private static ReceiverThread receiverThread;

    public static void main(String[] noargs) {
        try {
            //Setting up the Pandora Player
            pandoraMsgQueue = new ArrayBlockingQueue(1024);
            mouseMsgQueue = new ArrayBlockingQueue(1024);
            senderQueue = new ArrayBlockingQueue(1024);
            receiverQueue = new ArrayBlockingQueue(1024);
            
            // Start both applications
            mouseController = new MouseController(mouseMsgQueue, senderQueue);
            mouseController.start();
            player = new PandoraPlayer(pandoraMsgQueue, senderQueue);
            player.start();
            
            receiverThread = new ReceiverThread(receiverQueue, pandoraMsgQueue, mouseMsgQueue);
            receiverThread.start();
            
            //Initializes console window
            mainFrame = new serverFrame(pandoraMsgQueue);
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // Start the thread that listens for new clients.
            Thread t = new listenSocket(mainFrame, receiverQueue, senderQueue);
            t.start();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static String getAppName() {
        return applicationName;
    }
}
