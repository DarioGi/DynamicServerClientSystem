package com.example.AndroidMouse;

public class pandoraMsgType {
	
	private String bandName;
	public static final String msgPandoraPlay = ":PLAY:";
    public static final String msgPandoraPause = ":PAUSE:";
    public static final String msgPandoraVolume = ":VOLUME:";
    public static final String msgPandoraThumbsUp = ":THUMBSUP:";
    public static final String msgPandoraThumbsDown = ":THUMBSDOWN:";
    public static final String msgPandoraNext = ":SKIP:";
    public static final String pStatePlaying = "Playing";
    public static final String pStateSkipping = "Skipping";
    public static final String pStateNotPlaying = "Not Playing";
    public static final String pStateLoading = "Loading";
    public static final String pStatePaused = "Paused";
    public static final String pStateSearch = "Refine Search";
    public static final String pStateRegister = "Register Prompt";
    @SuppressWarnings("unused")
	private pandoraMsgType(){};
    private boolean nextSong;
    private boolean thumbDown;
    private String playerState;
    private boolean send;
    private int playerVolume;
    
    public pandoraMsgType(String bandName)
    {
    	this.playerVolume = -1;
        this.bandName = bandName;
        this.playerState = "";
        this.nextSong = false;
        this.thumbDown = false;
    }
    
    public pandoraMsgType(boolean nextSong)
    {
    	this.playerVolume = -1;
        this.bandName = "";
        this.playerState = "";
        this.nextSong = nextSong;
    }
    
    public pandoraMsgType(boolean nextSong, boolean thumbDown)
    {
    	this.playerVolume = -1;
        this.bandName = "";
        this.nextSong = nextSong;
        this.thumbDown = thumbDown;
    }
    public pandoraMsgType(String playerState, boolean sender)
    {
    	this.playerVolume = -1;
    	this.bandName = "";
    	this.playerState = "";
    	this.send = sender;
    	this.playerState = playerState;
    }
    
    public pandoraMsgType(String playerState,int volumeValue, boolean sender)
    {
    	this.playerVolume = volumeValue;
    	this.bandName = "";
    	this.playerState = "";
    	this.send = sender;
    	this.playerState = playerState;
    }
    
    public String getBandName()
    {
        return this.bandName;
    }
    
    public boolean getNextSong()
    {
        return nextSong;
    }
    
    public boolean getThumbDown()
    {
        return thumbDown;
    }
    
    public String getPlayerState()
    {
    	return this.playerState;
    }
    
    public boolean getSend()
    {
    	return this.send;
    }
    
    public int getPlayerVolume()
    {
    	return this.playerVolume;
    }
}
