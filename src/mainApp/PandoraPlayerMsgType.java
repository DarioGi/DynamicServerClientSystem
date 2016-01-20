/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multi_device_app;

/**
 *
 * @author Darius
 */
public class PandoraPlayerMsgType {
    
    private String bandName;
    private String artist, song, album;
    private PandoraPlayerMsgType(){};
    private boolean nextSong;
    private boolean thumbDown;
    private boolean sender;
    private String playerState;
    private int playerValue;
    private String elapsedTime;
    private int playerVolume;
    
    public PandoraPlayerMsgType(String bandName)
    {
        this.bandName = "";
        this.bandName = bandName;
        this.nextSong = false;
        this.thumbDown = false;
        this.playerState = "";
    }
    
    public PandoraPlayerMsgType(boolean nextSong)
    {
        this.bandName = "";
        this.nextSong = nextSong;
        this.playerState = "";
    }
    
    public PandoraPlayerMsgType(boolean nextSong, boolean thumbDown)
    {
        this.bandName = "";
        this.nextSong = nextSong;
        this.thumbDown = thumbDown;
        this.playerState = "";
    }
    
    public PandoraPlayerMsgType(String artist, String song, String album)
    {
        this.bandName = "";
        this.artist = artist;
        this.song = song;
        this.album = album;
        this.playerState = "";
    }
    
    public PandoraPlayerMsgType(String artist, String song, String album, 
            String playerState,int playerVolume, String elapsedTime, boolean sender)
    {
        this.bandName = "";
        this.artist = artist;
        this.song = song;
        this.album = album;
        this.playerState = playerState;
        this.elapsedTime = elapsedTime;
        this.playerVolume = playerVolume;
        this.sender = sender;
    }
    
    public PandoraPlayerMsgType(String setState, boolean sender)
    {
        this.playerState = setState;
        this.bandName = "";
    }
    
    public PandoraPlayerMsgType(String setState, int val, boolean sender)
    {
        this.playerState = setState;
        this.bandName = "";
        this.playerValue = val;
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
    
    public String getArtist()
    {
        return this.artist;
    }
    
    public String getSong()
    {
        return this.song;
    }
    
    public String getAlbum()
    {
        return this.album;
    }
    
    public boolean getSender()
    {
        return this.sender;
    }
    
    public String getPlayerState()
    {
        return this.playerState;
    }
    
    public int getPlayerValue()
    {
        return this.playerValue;
    }
    
    public String getElapsedTime()
    {
        return this.elapsedTime;
    }
    
    public int getPlayerVolume()
    {
        return this.playerVolume;
    }
}
