/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package leapcontroller;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.BlockingQueue;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author Darius
 */
public class ApplicationGUI extends JFrame implements ActionListener
{
    JPanel mainPanel;
    JPanel consolePanel;
    JPanel hostnamePanel;
    JButton connectButton;
    String windowTitle;
    JTextArea consoleWindow;
    JTextField consoleInput;
    JScrollPane scroll;
    JTextField hostnameInput;
    JLabel hostnameText;
    Connection connect;
    Connection connectThread;
    BlockingQueue msgQueue;
    boolean isConnected;
    
    public ApplicationGUI(String title, BlockingQueue msgQueue)
    {
        isConnected = false;
        this.msgQueue = msgQueue;
        
        mainPanel = new JPanel();
        consolePanel = new JPanel();
        hostnamePanel = new JPanel();
        
        hostnameText = new JLabel();
        hostnameText.setText("Hostname:");
        hostnamePanel.add(hostnameText);
        
        hostnameInput = new JTextField();
        hostnameInput.setColumns(25);
        hostnamePanel.add(hostnameInput);
        
        connectButton = new JButton("Connect");
        hostnamePanel.add(connectButton, BorderLayout.WEST);
        connectButton.addActionListener(this);
        
        //Console Input
        consoleInput = new JTextField();
        consoleInput.addActionListener(
            new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent event)
                {
                    processMessage(event.getActionCommand());
                    consoleInput.setText("");
                }
            }
        );// Creates a listener which allows the text input
        consoleInput.setColumns(42);
        
        //Console Output
        consoleWindow = new JTextArea(10,40);// Adds a text areasoleWindow.setEditable(false); // Disable editing of a text area
        consoleWindow.setEditable(false);
        consoleWindow.setBounds(0,100,200,240);
        scroll = new JScrollPane(consoleWindow);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        consolePanel.add(scroll);
        DefaultCaret caret = (DefaultCaret)consoleWindow.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); // Automatic scroll bar
        
        mainPanel.add(hostnamePanel, BorderLayout.SOUTH);
        mainPanel.add(consolePanel, BorderLayout.SOUTH);
        mainPanel.add(consoleInput, BorderLayout.SOUTH);
        this.add(mainPanel);
        this.windowTitle = title;
        this.setTitle(windowTitle);
        this.pack();
        this.setSize(500,300);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if ( e.getSource() == connectButton )
          onClickConnectHandler();
    }
    
    private void onClickConnectHandler()
    {
        if (connectButton.getText().equals("Connect")) 
        {
            String hostname = hostnameInput.getText();
            connectThread = new Connection(hostname, this);
            connectThread.start();
            connectButton.setText("Disconnect");
        }
        else if (connectButton.getText().equals("Disconnect"))
        {
            connectThread.isConnected = false;
            connectButton.setText("Connect");
        }
    }
    
    /* This method appends any text into the text area*/
    public void processMessage(final String message){
        SwingUtilities.invokeLater(
            new Runnable(){
                @Override
                public void run(){
                    consoleWindow.append(message + "\n");
                }
            }
        );
    }
    
    protected void connectionStatus(boolean isConnected)
    {
        this.isConnected = isConnected;
        connectButton.setEnabled(true);
        if (isConnected)
        {
            
        }
        else
        {
            connectButton.setText("Connect");
        }
    }
}
