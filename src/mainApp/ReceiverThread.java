/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multi_device_app;

import java.util.concurrent.BlockingQueue;

/**
 *
 * @author darius
 */
public class ReceiverThread extends Thread{
    
    private BlockingQueue receiverQueue;
    private BlockingQueue pandoraMsgQueue;
    private BlockingQueue mouseMsgQueue;
    private boolean keepAlive;
    
    public ReceiverThread (BlockingQueue receiverQueue, BlockingQueue pandoraMsgQueue, BlockingQueue mouseMsgQueue)
    {
        this.receiverQueue= receiverQueue;
        this.pandoraMsgQueue = pandoraMsgQueue;
        this.mouseMsgQueue= mouseMsgQueue;
        keepAlive = true;
    }
    
    @Override
    public void run()
    {
        while (keepAlive)
        {
            if (!receiverQueue.isEmpty())
            {
                if (receiverQueue.peek() instanceof PandoraPlayerMsgType )
                {
                    if (pandoraMsgQueue.remainingCapacity() > 1 )
                        pandoraMsgQueue.add(receiverQueue.remove());
                }
                else if (receiverQueue.peek() instanceof MouseControlMsgType )
                {
                    if (mouseMsgQueue.remainingCapacity() > 1 )
                    {
                        //MouseControlMsgType msg = (MouseControlMsgType) receiverQueue.remove();
                        mouseMsgQueue.add((MouseControlMsgType) receiverQueue.remove());
                    }
                }
            }
            else
            {
                try {
                    Thread.sleep(5); // Empty - sleep
                } catch (InterruptedException ex) {
                    serverFrame.processMessage("InterruptedException in receiver thread.");
                }
            }
            
        }
    }
    
    public boolean getKeepAlive()
    {
        return this.keepAlive;
    }
}
