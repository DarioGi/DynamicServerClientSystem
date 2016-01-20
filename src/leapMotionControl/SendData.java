/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package leapcontroller;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author Darius
 */
public class SendData extends Thread {
    
    private BlockingQueue msgQueue;
    private Connection connection;
    public SendData(Connection connection, BlockingQueue msgQueue)
    {
        this.msgQueue = msgQueue;
        this.connection = connection;
    }
    
    @Override
    public void run()
    {
        do
        {
            if ( !msgQueue.isEmpty() )
            {
                if ( msgQueue.peek() instanceof SwipeType )
                {
                    sendSwipe((SwipeType) msgQueue.remove());
                }
            }
            else
            {
                try
                {
                    Thread.sleep(100); // Queue empty - poll!
                }
                catch (InterruptedException e)
                {
                    connection.parent.processMessage("Exception while sleeping in SendData thread: " + e);
                }
            }
        }
        while (connection.isConnected);
    }
    
    private void sendSwipe(SwipeType swipe)
    {
        try
        {
           String msg;
           if (swipe.getIsSwipeRight())
               msg = "R";
           else
               msg = "L";
           connection.output.writeObject(":SWIPE:" + msg);
           connection.output.flush();
        }
        catch(IOException e)
        {
            connection.parent.processMessage("Exception while sending Swipe Data: " + e); 
            connection.cleanUp();
        }   
    }
}
