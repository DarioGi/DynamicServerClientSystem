/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package leapcontroller;

/**
 *
 * @author Darius
 */
public class SwipeType 
{
    private boolean isSwipeRight;
    public SwipeType (boolean isSwipeRight)
    {
        this.isSwipeRight = isSwipeRight;
    }
    
    public boolean getIsSwipeRight()
    {
        return this.isSwipeRight;
    }
}
