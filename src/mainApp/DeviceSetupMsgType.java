/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multi_device_app;

/**
 *
 * @author Darius
 */
public class DeviceSetupMsgType {
    
    private int ScreenSizeX;
    private int ScreenSizeY;
    private DeviceSetupMsgType(){}
    public DeviceSetupMsgType(int ScreenSizeX, int ScreenSizeY )
    {
        this.ScreenSizeX = ScreenSizeX;
        this.ScreenSizeY = ScreenSizeY;
    }
    
    public int getScreenSizeX()
    {
        return ScreenSizeX;
    }
    
    public int getScreenSizeY()
    {
        return ScreenSizeY;
    }
}
