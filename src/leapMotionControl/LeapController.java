package leapcontroller;

import com.leapmotion.leap.CircleGesture;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.FingerList;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.Gesture.State;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.KeyTapGesture;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.ScreenTapGesture;
import com.leapmotion.leap.SwipeGesture;
import com.leapmotion.leap.Vector;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;


class ControlListener extends Listener implements Runnable{
    
    private long timeSwipe;
    private final long swipeDelayTimeMs = 250; // in milliseconds
    private BlockingQueue msgQueue;
    
    ApplicationGUI gui;
    
  public ControlListener(BlockingQueue queue,ApplicationGUI gui)
  {
      timeSwipe = System.currentTimeMillis();
      this.msgQueue = queue;
      this.gui = gui;
  }
    
    @Override
  public void onInit(Controller controller) {
    System.out.println("Initialized");
  }

  @Override
  public void onConnect(Controller controller) {
    System.out.println("Connected");
    controller.enableGesture(Gesture.Type.TYPE_SWIPE);
    controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
    controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
    controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
  }

    @Override
  public void onDisconnect(Controller controller) {
    //Note: not dispatched when running in a debugger.
    System.out.println("Disconnected");
  }

    @Override
  public void onExit(Controller controller) {
    System.out.println("Exited");
  }

    @Override
  public void onFrame(Controller controller) {
    // Get the most recent frame and report some basic information
    Frame frame = controller.frame();
//    System.out.println("Frame id: " + frame.id()
//                       + ", timestamp: " + frame.timestamp()
//                       + ", hands: " + frame.hands().count()
//                       + ", fingers: " + frame.fingers().count()
//                       + ", tools: " + frame.tools().count()
//                       + ", gestures " + frame.gestures().count());

    if (!frame.hands().isEmpty()) {
      // Get the first hand
      Hand hand = frame.hands().get(0);

      // Check if the hand has any fingers
      FingerList fingers = hand.fingers();
      if (!fingers.isEmpty()) {
        // Calculate the hand's average finger tip position
        Vector avgPos = Vector.zero();
        for (Finger finger : fingers) {
          avgPos = avgPos.plus(finger.tipPosition());
        }
        avgPos = avgPos.divide(fingers.count());
        //System.out.println("Hand has " + fingers.count()
        //                   + " fingers, average finger tip position: " + avgPos);
      }

      // Get the hand's sphere radius and palm position
     // System.out.println("Hand sphere radius: " + hand.sphereRadius()
      //                   + " mm, palm position: " + hand.palmPosition());

      // Get the hand's normal vector and direction
      Vector normal = hand.palmNormal();
      Vector direction = hand.direction();

      // Calculate the hand's pitch, roll, and yaw angles
     // System.out.println("Hand pitch: " + Math.toDegrees(direction.pitch()) + " degrees, "
      //                   + "roll: " + Math.toDegrees(normal.roll()) + " degrees, "
       //                  + "yaw: " + Math.toDegrees(direction.yaw()) + " degrees");
    }

    GestureList gestures = frame.gestures();
    for (int i = 0; i < gestures.count(); i++) {
      Gesture gesture = gestures.get(i);

      switch (gesture.type()) {
        case TYPE_CIRCLE:
          CircleGesture circle = new CircleGesture(gesture);

          // Calculate clock direction using the angle between circle normal and pointable
          String clockwiseness;
          if (circle.pointable().direction().angleTo(circle.normal()) <= Math.PI / 4) {
            // Clockwise if angle is less than 90 degrees
            clockwiseness = "clockwise";
          }
          else {
            clockwiseness = "counterclockwise";
          }

          // Calculate angle swept since last frame
          double sweptAngle = 0;
          if (circle.state() != State.STATE_START) {
            CircleGesture previousUpdate = new CircleGesture(controller.frame(1).gesture(circle.id()));
            sweptAngle = (circle.progress() - previousUpdate.progress()) * 2 * Math.PI;
          }

//          System.out.println("Circle id: " + circle.id()
//                             + ", " + circle.state()
//                             + ", progress: " + circle.progress()
//                             + ", radius: " + circle.radius()
//                             + ", angle: " + Math.toDegrees(sweptAngle)
//                             + ", " + clockwiseness);
          break;
        case TYPE_SWIPE:
          SwipeGesture swipe = new SwipeGesture(gesture);
//          System.out.println("Swipe id: " + swipe.id()
//                             + ", " + swipe.state()
//                             + ", position: " + swipe.position()
//                             + ", direction: " + swipe.direction()
//                             + ", speed: " + swipe.speed());
          if ( swipe.state() == Gesture.State.STATE_STOP )
          {
              long tempTime = System.currentTimeMillis();
              if ( (tempTime - swipeDelayTimeMs) > timeSwipe )
              {
                  timeSwipe = tempTime;
                  if ( swipe.direction().getX() < 0 && swipe.speed() > 150 )
                  {
                      try 
                      {
                          if ( gui.isConnected)
                            msgQueue.put(new SwipeType(false));
                          gui.processMessage("Swipe left!");
                      } 
                      catch (InterruptedException ex) 
                      {
                          Logger.getLogger(ControlListener.class.getName()).log(Level.SEVERE, null, ex);
                      }
                  }
                  else if ( swipe.direction().getX() > 0 && swipe.speed() > 150 )
                  {
                      try 
                      {
                          
                          if ( gui.isConnected)
                            msgQueue.put(new SwipeType(true));
                          gui.processMessage("Swipe right!");
                      } 
                      catch (InterruptedException ex) 
                      {
                          Logger.getLogger(ControlListener.class.getName()).log(Level.SEVERE, null, ex);
                      }
                  }
                  System.out.println("Swipe id: " + swipe.id()
                                   + ", " + swipe.state()
                                   + ", position: " + swipe.position()
                                   + ", direction: " + swipe.direction()
                                   + ", speed: " + swipe.speed());
              }
          }
          break;
        case TYPE_SCREEN_TAP:
          ScreenTapGesture screenTap = new ScreenTapGesture(gesture);
          System.out.println("Screen Tap id: " + screenTap.id()
                             + ", " + screenTap.state()
                             + ", position: " + screenTap.position()
                             + ", direction: " + screenTap.direction());
          break;
        case TYPE_KEY_TAP:
          KeyTapGesture keyTap = new KeyTapGesture(gesture);
          System.out.println("Key Tap id: " + keyTap.id()
                             + ", " + keyTap.state()
                             + ", position: " + keyTap.position()
                             + ", direction: " + keyTap.direction());
          break;
        default:
          System.out.println("Unknown gesture type.");
          break;
      }
    }

    if (!frame.hands().isEmpty() || !gestures.isEmpty()) {
      //System.out.println();
    }
  }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

class LeapController extends JFrame
{
    static final ExecutorService msgProducer= Executors.newFixedThreadPool(100);
    static final ExecutorService msgConsumer = Executors.newFixedThreadPool(100);
    
  public static void main(String[] args) throws InterruptedException {
    // Create a sample listener and controller
      
    // Create a message queue to place the messages. 
    BlockingQueue msgQueue = new ArrayBlockingQueue(1024);
    ApplicationGUI app = new ApplicationGUI("Leap Motion Control v0.01", msgQueue);
    ControlListener listener = new ControlListener(msgQueue, app);
    Controller controller = new Controller();
    controller.setPolicyFlags(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
    controller.config().setFloat("Gesture.Swipe.MinLength", 200);
    controller.config().setFloat("Gesture.Swipe.MinVelocity", 750);
    
    controller.config().save();
    // Have the sample listener receive events from the controller
    controller.addListener(listener);

   
    // Keep this process running until Enter is pressed
    System.out.println("Press Enter to quit...");
    try {
      System.in.read();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    msgProducer.shutdown();
    msgConsumer.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    msgProducer.shutdown();
    msgConsumer.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    // Remove the sample listener when done
    controller.removeListener(listener);
  }
}
