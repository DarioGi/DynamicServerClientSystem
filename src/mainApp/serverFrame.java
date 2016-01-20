/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multi_device_app;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import java.util.Calendar;
import javax.swing.ScrollPaneConstants;
/* Creates a server frame*/
public class serverFrame extends JFrame {

    private static JTextArea consoleWindow;
    private JTextField consoleInput;
    private final BlockingQueue pandoraPlayerMsgQueue;
    private static Calendar cal;
    private final JScrollPane scroll;

    public serverFrame(BlockingQueue pandoraPlayerMsgQueue) {
        super("App Control - v0.01");
        this.pandoraPlayerMsgQueue = pandoraPlayerMsgQueue;
        consoleInput = new JTextField();
        consoleInput.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                processMessage(event.getActionCommand());
                processInput(event.getActionCommand());
                consoleInput.setText("");
            }
        });// Creates a listener which allows the text input
        add(consoleInput, BorderLayout.SOUTH);
        consoleWindow = new JTextArea();// Adds a text area
        scroll = new JScrollPane(consoleWindow);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scroll);
        DefaultCaret caret = (DefaultCaret) consoleWindow.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); // Automatic scroll bar
        consoleWindow.setEditable(false); // Disable editing of a text area
        setSize(500, 250);
        setVisible(true);
    }

    /* This method appends any text into the text area*/
    public static void processMessage(final String message) {
        SwingUtilities.invokeLater(
                new Runnable() {
            @Override
            public void run() {
                cal = Calendar.getInstance();
                
                consoleWindow.append( String.format("[%02d:%02d:%02d] ",
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE),
                        cal.get(Calendar.SECOND))
                        + message + "\n");
            }
        });
    }

    private void processInput(final String msg) {
        SwingUtilities.invokeLater(
                new Runnable() {
            @Override
            public void run() {
                if (msg.toLowerCase(Locale.ENGLISH).contains(PandoraPlayer.PLAY)) {
                    try {
                        if (pandoraPlayerMsgQueue.remainingCapacity() > 1)
                        {
                            if (msg.trim().length() == PandoraPlayer.PLAY.length() )
                                pandoraPlayerMsgQueue.put(new PandoraPlayerMsgType(PandoraPlayer.PLAY, false));
                            else
                                pandoraPlayerMsgQueue.put(new PandoraPlayerMsgType(msg.substring(PandoraPlayer.PLAY.length(), msg.length())));
                        }
                        else
                            processMessage("Request cannot be completed: Too many requests made!");
                    } catch (InterruptedException e) {
                        processMessage(e.toString());
                    }
                }
                else if( (msg.toLowerCase(Locale.ENGLISH).contains(PandoraPlayer.PAUSE)) )
                {
                    try {
                        if (pandoraPlayerMsgQueue.remainingCapacity() > 1)
                             pandoraPlayerMsgQueue.put(new PandoraPlayerMsgType(PandoraPlayer.PAUSE, false));
                         else
                             processMessage("Request cannot be completed: Too many requests made!");
                     } catch (InterruptedException e) {
                         processMessage(e.toString());
                     }
                }
                else if((msg.toLowerCase(Locale.ENGLISH).contains(PandoraPlayer.VOLUME)))
                {
                    try
                    {
                        int tempInt = Integer.parseInt(msg.substring(PandoraPlayer.VOLUME.length()+1, msg.length()));
                        if ( tempInt < 0 || tempInt > 100 )
                        {
                            processMessage("Enter a value: [0,100]");
                        }
                        else
                        {
                            try {
                                if (pandoraPlayerMsgQueue.remainingCapacity() > 1)
                                     pandoraPlayerMsgQueue.put(new PandoraPlayerMsgType(PandoraPlayer.VOLUME, tempInt, false));
                                 else
                                     processMessage("Request cannot be completed: Too many requests made!");
                             } catch (InterruptedException e) {
                                 processMessage(e.toString());
                             }
                        }
                    }
                    catch(StringIndexOutOfBoundsException | NumberFormatException e)
                    {
                        processMessage("Failed to parse. Usage: \"\\volume [0,100]\"");
                    }
                }
            }
        });
    }
}
