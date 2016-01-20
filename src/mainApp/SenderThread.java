/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multi_device_app;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author Darius
 */
public class SenderThread extends Thread {

    private BlockingQueue senderQueue;
    private Vector userClients;

    private SenderThread() {
    }
    private int senderSleepTime = 25;
    boolean keepAlive = true;

    public SenderThread(BlockingQueue senderQueue, Vector userClients) {
        this.senderQueue = senderQueue;
        this.userClients = userClients;
    }

    @Override
    public void run() {
        while (keepAlive) {
            try {
                if (!senderQueue.isEmpty()) {
                    if (senderQueue.peek() instanceof PandoraPlayerMsgType) {
                        notifyPandoraApps((PandoraPlayerMsgType) senderQueue.remove());
                    }
                    //Notify other types...
                } else {
                    Thread.sleep(senderSleepTime); //Empty - sleep
                }
            } catch (Exception e) {
                serverFrame.processMessage(String.format("Exception in Sender Thread: %s", e));
                keepAlive = false;
            }
        }
    }

    private void notifyPandoraApps(PandoraPlayerMsgType msg) {
        Iterator i = userClients.iterator();
        while (i.hasNext()) {
            userClient temp = (userClient) i.next();
            if (!temp.getClientThreadStatus())
                continue;
            if (temp.getClientThreadType() == ClientThread.typeAndroid) {
                temp.getClientThreadSenderQueue().
                        add(msg);
            }
        }
    }

    public boolean getKeepAlive() {
        return keepAlive;
    }
}
