/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multi_device_app;

import java.io.IOException;
import java.io.ObjectInputStream;
import static multi_device_app.ClientThread.typeAndroid;
import static multi_device_app.ClientThread.typeKinect;
import static multi_device_app.ClientThread.typeLeap;

/**
 *
 * @author Darius
 */
public class ClientThreadReceiver extends Thread{
    
    //private BlockingQueue receiverQueue;
    private ClientThread thread;
    private ObjectInputStream input;
    private Object message;
    private ClientThreadReceiver(){}
    
    public ClientThreadReceiver(ClientThread thread)
    {
        this.input = thread.getInputStream();
        this.thread = thread;
    }
    
    @Override
    public void run()
    {
        while (thread.getKeepAlive())
        {
            try 
            {
                message = (String) input.readObject();
                int sensorType = thread.getSensorType();
                if(message.equals("CLIENT - END"))
                {
                    thread.setKeepAlive(false);
                    break;
                }
                if (!thread.getIsSetup())
                {
                    thread.parseInputSetup((String) message);
                }
                    
                if ( sensorType == typeAndroid )
                {
                    thread.parseInputAndroid((String) message);
                }
                else if ( sensorType == typeLeap )
                {
                    thread.parseInputLeap((String) message);
                }
                else if ( sensorType == typeKinect )
                {

                }
            }catch (IOException e) {
                serverFrame.processMessage(String.format("Client[%d]: I\\O Exception. \n%s",thread.getSensorType(), e));
                thread.setKeepAlive(false);
            } catch(ClassNotFoundException e)
            {
                serverFrame.processMessage("The client " + 
                        thread.clientSocket.getInetAddress().getHostName() + 
                        " sent an unparsable message");
            }
            catch (Exception e)
            {
                serverFrame.processMessage(String.format("Client[%d]: Exception. \n%s",thread.getSensorType(), e));
            }
        }
    }
}
