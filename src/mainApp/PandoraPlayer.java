package multi_device_app;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Darius
 */
public class PandoraPlayer extends Thread {

    private FirefoxDriver driver;
    private final BlockingQueue msgQueue, senderQueue;
    public static final String PLAY = "/play";
    public static final String PAUSE = "/pause";
    public static final String VOLUME = "/volume";
    public static final String THUMBUP = "/thumbup";
    public static final String THUMBDOWN = "/thumbdown";
    public static final String SKIP = "/skip";
    public static final String pStatePlaying = "Playing";
    public static final String pStateSkipping = "Skipping";
    public static final String pStateNotPlaying = "Not Playing";
    public static final String pStateLoading = "Loading";
    public static final String pStatePaused = "Paused";
    public static final String pStateSearch = "Refine Search";
    public static final String pStateRegister = "Register Prompt";
    public static final String msgPandoraPlayNew = ":PLAY_NEW:";
    public static final String msgPandoraPlay = ":PLAY:";
    public static final String msgPandoraPause = ":PAUSE:";
    public static final String msgPandoraSkip = ":SKIP:";
    public static final String msgPandoraVolume = ":VOLUME:";
    public static final String msgPandoraThumbsUp = ":THUMBSUP:";
    public static final String msgPandoraThumbsDown = ":THUMBSDOWN:";
    protected boolean keepAlive;
    int standByTime = 100;// when queue is empty (ms)
    private String currentSong;
    private String currentArtist;
    private String currentAlbum;
    private String lastSongName;
    private String playerState;
    private boolean browserAlive;
    private String elapsedTime;
    private long sendMsgTimer;
    private static final long updateRate = 750; // client update rate in ms
    private int playerVolume;

    public PandoraPlayer(BlockingQueue msgQueue, BlockingQueue senderQueue) {
        this.msgQueue = msgQueue;
        this.senderQueue = senderQueue;
        keepAlive = true;
        driver = new FirefoxDriver();
        this.currentSong = "unknown";
        this.currentArtist = "unknown";
        this.currentAlbum = "unknown";
        this.lastSongName = "unknown";
        this.browserAlive = true;
        this.playerState = pStateNotPlaying;
        this.elapsedTime = "0:00";
        sendMsgTimer = System.currentTimeMillis();
        playerVolume = 50;
    }

    @Override
    public void run() {
        while (keepAlive) {
            if (!msgQueue.isEmpty()) {
                if (msgQueue.peek() instanceof PandoraPlayerMsgType) {
                    msgHandler((PandoraPlayerMsgType) msgQueue.remove());
                }
            } else {
                try {
                    if (browserAlive && driver.getCurrentUrl().contains("station/play/")) {
                        updateTrackInfo();
                        if (!lastSongName.equals(currentSong)) {
                            serverFrame.processMessage("Pandora: Currently playing " + currentSong
                                    + " by " + currentArtist
                                    + " on " + currentAlbum
                                    + ".");
                            lastSongName = currentSong;
                        }
                    } else if (browserAlive && driver.getCurrentUrl().contains("account/register")) {
                        playerState = pStateRegister;
                    } else if (browserAlive && driver.getCurrentUrl().contains("search/")) {
                        playerState = pStateSearch;
                    } else {
                        playerState = pStateNotPlaying;
                        Thread.sleep(standByTime); // nothing to do here - sleep!
                    }
                    updateAndroidClients();
                } catch (InterruptedException e) {
                    serverFrame.processMessage("Exception while sleeping in PandoraPlayer thread: " + e);
                } catch (UnreachableBrowserException | NoSuchWindowException e) {
                    serverFrame.processMessage("Pandora: Browser was closed!");
                    browserAlive = false;
                }
            }
        }
    }

    private void msgHandler(PandoraPlayerMsgType msg) {
        if (msg.getBandName().length() != 0) {
            playSong(msg.getBandName());
        }
        if (msg.getNextSong()) {
            nextSong();
        }
        if (msg.getThumbDown()) {
            thumbDown();
        }
        if (msg.getPlayerState().equals(PAUSE)) {
            pauseSong();
        }
        if (msg.getPlayerState().equals(PLAY)) {
            playSong();
        }
        if (msg.getPlayerState().equals(VOLUME)) {
            changeVolume(msg.getPlayerValue());
        }
        if (msg.getPlayerState().equals(THUMBUP)) {
            thumbUp();
        }
        if (msg.getPlayerState().equals(THUMBDOWN)) {
            thumbDown();
        }
        if (msg.getPlayerState().equals(SKIP)) {
            thumbDown();
        }
    }

    private void playSong(String bandName) {
        try {
            playerState = pStateLoading;
            updateAndroidClients();
            if (driver.getTitle().length() != 0) {
                driver.close();
                driver = new FirefoxDriver();
            }
            serverFrame.processMessage("Pandora: Attempting to play " + bandName + "...");
            driver.get("http://www.pandora.com");
            browserAlive = true;
            List<WebElement> inputs = driver.findElements(By.xpath("//input"));
            boolean second = false;
            for (WebElement input : inputs) {
                if (input.getAttribute("class").equals("searchInput")) {
                    if (second) {
                        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOf(input));
                        input.sendKeys(bandName);
                        input.sendKeys(Keys.RETURN);
                        //WebElement player = driver.findElement(By.id("playbackControl"));
                        //new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOf(player));
                        break;
                    } else {
                        second = true;
                    }
                }
            }
        } catch (ElementNotVisibleException e) {
            serverFrame.processMessage("Pandora: Invalid search.");
            this.playerState = pStateSearch;
            updateAndroidClients();
        } catch (Exception e) {
            serverFrame.processMessage(String.format("Pandora Exception (playSong): %s", e));
        }
    }

    private void playSong() {
        if (driver.getCurrentUrl().contains("station/play/")) {
            WebElement playButton = driver.findElement(By.className("playButton"));
            try {
                if (playButton.isDisplayed()) {
                    playButton.click();
                    serverFrame.processMessage("Pandora: Playing.");
                    playerState = pStatePlaying;
                } else {
                    serverFrame.processMessage("Pandora: Currently playing...");
                }
            } catch (ElementNotVisibleException e) {
                serverFrame.processMessage("Pandora: Cannot play - can't find the play button.");
            }
        } else {
            serverFrame.processMessage("Pandora: Unable to play the song. Currently not playing a track.");
        }
    }

    private void updateTrackInfo() {
        if (driver.getCurrentUrl().contains("station/play/")) {
            try {
                WebElement song = driver.findElement(By.className("playerBarSong"));
                WebElement artist = driver.findElement(By.className("playerBarArtist"));
                WebElement album = driver.findElement(By.className("playerBarAlbum"));
                WebElement timeElapsed = driver.findElement(By.className("elapsedTime"));
                new WebDriverWait(driver, 3).until(ExpectedConditions.visibilityOf(song));
                new WebDriverWait(driver, 3).until(ExpectedConditions.visibilityOf(artist));
                new WebDriverWait(driver, 3).until(ExpectedConditions.visibilityOf(album));
                new WebDriverWait(driver, 3).until(ExpectedConditions.visibilityOf(timeElapsed));
                WebElement pauseButton = driver.findElement(By.className("pauseButton"));
                if (!pauseButton.isDisplayed()) 
                    playerState = pStatePaused;
                else
                    playerState = pStatePlaying;
                Actions mouseMove = new Actions(driver);
                WebElement volumeButton = driver.findElementByClassName("volumeButton");
                WebElement volumeKnob = driver.findElementByClassName("volumeKnob");
                //volumeButton.findElement(By.name("style"));;
                mouseMove.moveToElement(volumeButton);
                mouseMove.perform();
                new WebDriverWait(driver, 2).until(ExpectedConditions.visibilityOf(volumeKnob));
                String volKnobVal = volumeKnob.getAttribute("style");
                int volumeKnobValue = (int) ((Double.parseDouble(volKnobVal.substring(5,
                                  volKnobVal.indexOf("px")).trim()) - 20)/.82);
                playerVolume = volumeKnobValue;
                currentSong = song.getText();
                currentArtist = artist.getText();
                currentAlbum = album.getText();
                elapsedTime = timeElapsed.getText();
            } catch (Exception e) {
                serverFrame.processMessage("Pandora: Unable to update track info");
            }
        } else {
            serverFrame.processMessage("Pandora: Unable to locate track info.");
        }
    }

    protected FirefoxDriver getCurrentDriver() {
        return this.driver;
    }

    private void nextSong() {
        if (driver.getCurrentUrl().contains("station/play/")) {
            WebElement skipButton = driver.findElement(By.className("skipButton"));
            new WebDriverWait(driver, 1).until(ExpectedConditions.visibilityOf(skipButton));
            skipButton.click();
            playerState = pStateSkipping;
            serverFrame.processMessage("Pandora: Skipping a song.");
        } else {
            serverFrame.processMessage("Pandora: Currently not playing a track.");
        }
    }
    
    private void skip() {
        if (driver.getCurrentUrl().contains("station/play/")) {
            WebElement skipButton = driver.findElement(By.className("skipButton"));
            new WebDriverWait(driver, 1).until(ExpectedConditions.visibilityOf(skipButton));
            skipButton.click();
            serverFrame.processMessage("Pandora: Skip song.");
        } else {
            serverFrame.processMessage("Pandora: Unable to skip the song. Currently not playing a track.");
        }
    }

    private void thumbDown() {
        if (driver.getCurrentUrl().contains("station/play/")) {
            WebElement thumbDownButton = driver.findElement(By.className("thumbDownButton"));
            new WebDriverWait(driver, 1).until(ExpectedConditions.visibilityOf(thumbDownButton));
            thumbDownButton.click();
            serverFrame.processMessage("Pandora: thumbs down.");
        } else {
            serverFrame.processMessage("Pandora: Unable to thumbs down the song. Currently not playing a track.");
        }
    }

    private void thumbUp() {
        if (driver.getCurrentUrl().contains("station/play/")) {
            WebElement thumbUpButton = driver.findElement(By.className("thumbUpButton"));
            new WebDriverWait(driver, 1).until(ExpectedConditions.visibilityOf(thumbUpButton));
            thumbUpButton.click();
            serverFrame.processMessage("Pandora: thumbs up.");
        } else {
            serverFrame.processMessage("Pandora: Unable to thumbs up the song. Currently not playing a track.");
        }
    }

    private void pauseSong() {
        if (driver.getCurrentUrl().contains("station/play/")) {
            WebElement pauseButton = driver.findElement(By.className("pauseButton"));
            //new WebDriverWait(driver, 1).until(ExpectedConditions.visibilityOf(pauseButton)).isEnabled();
            try {
                if (pauseButton.isDisplayed()) {
                    pauseButton.click();
                    serverFrame.processMessage("Pandora: Paused.");
                    playerState = pStatePaused;
                } else {
                    serverFrame.processMessage("Pandora: Currently paused - cannot pause...");
                }
            } catch (ElementNotVisibleException e) {
                serverFrame.processMessage("Pandora: Cannot pause - can't find the pause button.");
            }
        } else {
            serverFrame.processMessage("Pandora: Unable to pause the song. Currently not playing a track.");
        }
    }

    private void changeVolume(int val) {
        if (driver.getCurrentUrl().contains("station/play/")) {
            try {
                Actions mouseMove = new Actions(driver);
                WebElement volumeButton = driver.findElementByClassName("volumeButton");
                WebElement volumeKnob = driver.findElementByClassName("volumeKnob");
                //volumeButton.findElement(By.name("style"));;
                mouseMove.moveToElement(volumeButton);
                mouseMove.perform();
                new WebDriverWait(driver, 2).until(ExpectedConditions.visibilityOf(volumeKnob));
                String volKnobVal = volumeKnob.getAttribute("style");
                int volumeKnobValue = (int) ((Double.parseDouble(volKnobVal.substring(5,
                                  volKnobVal.indexOf("px")).trim()) - 20)/.82);
                mouseMove.dragAndDropBy(volumeKnob, val - volumeKnobValue, -7);
                mouseMove.perform();
                playerVolume = val;
                serverFrame.processMessage(String.format("Pandora: Volume changed to: %d.", val));
            } catch (ElementNotVisibleException e) {
                serverFrame.processMessage("Pandora: Cannot change volume - can't find the volume bar.");
            }
        } else {
            serverFrame.processMessage("Pandora: Unable to change volume. Currently not playing a track.");
        }
    }

    private void updateAndroidClients() {
        long tempTime = System.currentTimeMillis();
        if ((tempTime - sendMsgTimer) > updateRate) {
            sendMsgTimer = tempTime;
            if (senderQueue.remainingCapacity() > 1) {
                senderQueue.add(new PandoraPlayerMsgType(
                        currentArtist,
                        currentSong,
                        currentAlbum,
                        playerState,
                        playerVolume,
                        elapsedTime,
                        true));
            }
        }
    }
}
