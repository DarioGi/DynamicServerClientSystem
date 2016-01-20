package com.example.AndroidMouse;

public class mouseMsgType {
	
	private int LocX;
	private int LocY;
	private boolean isLeftClick;
	
	@SuppressWarnings("unused")//Used to prevent accidental no param constructor
	private mouseMsgType(){}
	
	public mouseMsgType(int locX, int locY, boolean isLeftClick)
	{
		this.LocX = locX;
		this.LocY = locY;
		this.isLeftClick = isLeftClick;
	}
	
	public int getLocX()
	{
		return this.LocX;
	}
	
	public int getLocY()
	{
		return this.LocY;
	}
	
	public boolean getIsLeftClick()
	{
		return this.isLeftClick;
	}
}
