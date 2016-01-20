/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multi_device_app;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.util.concurrent.BlockingQueue;
/**
 *
 * @author darius
 */
public class MouseController extends Thread {
    
    private BlockingQueue mouseControllerQueue;
    private BlockingQueue senderQueue;
    private Robot mouseControl;
    private Point coordinates;
    
    protected static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    boolean keepAlive;
    private MouseController(){}
    
    public MouseController(BlockingQueue mouseControllerQueue, BlockingQueue senderQueue)
    {
        this.mouseControllerQueue = mouseControllerQueue;
        this.senderQueue = senderQueue;
        coordinates = new Point(0,0);
        keepAlive = true;
        try
        {
            mouseControl = new Robot();
        } // Creates a macro used for mouse control
        catch(AWTException e)
        {
            serverFrame.processMessage(e.toString());
            keepAlive = false;
        }
    }
    
    @Override
    public void run()
    {
        while (keepAlive)
        {
            if (!mouseControllerQueue.isEmpty())
            {
                if ( mouseControllerQueue.peek() instanceof MouseControlMsgType )
                {
                    parseMsg((MouseControlMsgType)mouseControllerQueue.remove());
                }
            }
            else
            {
                try {
                    Thread.sleep(15); //Empty - sleep
                } catch (InterruptedException ex) {
                    serverFrame.processMessage("Interrupted Exception in Mouse Controller");
                }
            }
        }
    }
    
    public boolean getKeepAlive()
    {
        return this.keepAlive;
    }  
    
    private void parseMsg(MouseControlMsgType msg)
    {
        //No checks for sub-type ***IMPLEMENT LATER***
        mouseControl.mouseMove(msg.getMousePosX(), msg.getMousePosY());
        
        if(msg.getIsLeftClick())
        {
            mouseControl.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            mouseControl.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }
    }
}
