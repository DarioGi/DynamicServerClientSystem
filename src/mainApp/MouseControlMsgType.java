/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multi_device_app;

/**
 *
 * @author darius
 */
public class MouseControlMsgType {
    
    private int mousePosX;
    private int mousePosY;
    private boolean isLeftClick;
    
    private MouseControlMsgType(){}
    
    public MouseControlMsgType(int mousePosX, int mousePosY, boolean isLeftClick)
    {
        this.mousePosX = mousePosX;
        this.mousePosY = mousePosY;
        this.isLeftClick = isLeftClick;
    }
    
    public int getMousePosX()
    {
        return this.mousePosX;
    }
    
    public int getMousePosY()
    {
        return this.mousePosY;
    }
    
    public boolean getIsLeftClick()
    {
        return this.isLeftClick;
    }        
}
