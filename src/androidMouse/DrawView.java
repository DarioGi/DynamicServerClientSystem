package com.example.AndroidMouse;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class DrawView extends View implements OnTouchListener {
	
    private long tempLong=1;
    private double tempDouble=1;
    public boolean flip=false;
    public boolean isDown=false;
    public boolean isOnClick = false;
    public boolean isOnDraw=true;
    public boolean sendLeftClick = false;
    public boolean sendRightClick = false;
    public float XLoc=60;
    public float YLoc=60;
    public float XTemp=60;
    public float YTemp=60;
    public float YCircleLoc;
    public float XCircleLoc;
    public float mDownY;
    public float mDownX;
    public float velocity;
    public float sensitivity=150;
    public int SCROLL_THRESHOLD=1;
    public static int ScreenDimensionX;
    public static int ScreenDimensionY;
    private Paint paint = new Paint();

	public DrawView(Context context){
   	 	super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);

    }

    public DrawView(Context context, AttributeSet attrs) 
    { 
        super(context, attrs); 
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
    } 

    public DrawView(Context context, AttributeSet attrs, int defStyle) 
    { 
        super(context, attrs, defStyle);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
    }
    
    @Override
    public void onDraw(Canvas canvas) {
    	isOnDraw=true;
	    if(isDown)
	    {
    		paint.setColor(Color.rgb(9, 184, 215));
    		canvas.drawCircle(XTemp, YTemp, 50, paint);
	    }else{
    		paint.setColor(Color.WHITE);
    		canvas.drawCircle(XTemp, YTemp, 30, paint);	
    	}
    }
    
    public boolean onTouch(View view, MotionEvent event){
        isDown=false;
    	switch (event.getAction() & MotionEvent.ACTION_MASK){
        case MotionEvent.ACTION_DOWN:
        	mDownX = event.getX();
            mDownY = event.getY();
            isDown=true;
            isOnClick=true;
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            if(isOnClick){
            	sendLeftClick=true;
            }
            SendData.sendLeftClick = sendLeftClick;
            break;
        case MotionEvent.ACTION_MOVE:
            sendLeftClick=false;
            sendRightClick=false;
        	isDown=true;
        	tempDouble=(double)System.currentTimeMillis()-tempLong;
        	if(tempDouble > 40 || tempDouble == 0){
        		tempDouble=1/sensitivity;
        	}else{
        		tempDouble=1/tempDouble;
        		}
        	if(XTemp > event.getX())
        		XLoc = XLoc-(float)(sensitivity*tempDouble);
        	if(XTemp < event.getX())
        		XLoc = XLoc+(float)(sensitivity*tempDouble);
        	if(YTemp > event.getY())
        		YLoc = YLoc-(float)(sensitivity*tempDouble);
        	if(YTemp < event.getY())
        		YLoc = YLoc+(float)(sensitivity*tempDouble);
        	if(XLoc<0)
        		XLoc=0;
        	if(YLoc<0)
        		YLoc=0;
        	if(XLoc>ScreenDimensionX)
        		XLoc=ScreenDimensionX;
        	if(YLoc>ScreenDimensionY)
        		YLoc=ScreenDimensionY;
            if (isOnClick && (Math.abs(mDownX - event.getX()) > SCROLL_THRESHOLD || Math.abs(mDownY - event.getY()) > SCROLL_THRESHOLD)){
                isOnClick=false;
                sendLeftClick=false;
            }
            SendData.XLoc = Math.round(XLoc);
    		SendData.YLoc = Math.round(YLoc);
    		SendData.sendLeftClick = sendLeftClick;
            break;
        default:
            break;
    	}
    	XTemp = event.getX();
        YTemp = event.getY();
        tempLong=System.currentTimeMillis();
    	invalidate();
        return true;
    }
}
