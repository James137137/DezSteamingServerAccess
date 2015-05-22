/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137.dezsteamingserveraccess;

import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 *
 * @author James
 */
public class MainGUI extends JFrame {

    private static boolean currentState = false; //false = offline, true = online
    static boolean busy = false;
    private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true).fullyPadded();
    private final LinedBoxPanel buttonsPane2 = new LinedBoxPanel(true).fullyPadded();
    public static final JButton startStopButton = new JButton("Start Server");

    public MainGUI() throws HeadlessException {
        super("Server is Online");
        this.setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(300, 100);
        setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - getSize().width / 2 - 50, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - getSize().height / 2);
        initComponents();
        try {
            if (MyAPIMethods.serverExist(Main.serverName)) {
                this.setTitle("Server is Online");
                if (!MyAPIMethods.SimplePing()) {
                    this.setTitle("Server is Online : needs rebooting");
                }

                currentState = true;
                startStopButton.setText("Stop Server");
            } else {
                this.setTitle("Server is Offline");
                currentState = false;
            }
        } catch (DigitalOceanException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RequestUnsuccessfulException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void initComponents() {
        startStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (busy != true) {
                    busy = true;
                    if (MainGUI.currentState == false) {
                        try {
                            if (MyAPIMethods.serverExist(Main.serverName)) {
                                ((JButton) event.getSource()).setText("Try again later.");
                                busy = false;
                            } else {
                                MainGUI.currentState = true;
                                new Main.CreateServer(event);
                                setTitle("Server is Online");
                            }
                        } catch (DigitalOceanException ex) {
                            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (RequestUnsuccessfulException ex) {
                            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } else {
                        try {
                            if (!MyAPIMethods.serverExist(Main.serverName)) {
                                ((JButton) event.getSource()).setText("Can't find server");
                                busy = false;
                            } else {
                                MainGUI.currentState = false;
                                new Main.DestoryServer(event);
                                setTitle("Server is offline");
                            }
                        } catch (DigitalOceanException ex) {
                            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (RequestUnsuccessfulException ex) {
                            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }

                }

            }
        });

        buttonsPanel.addGlue();
        buttonsPanel.add(startStopButton);
        buttonsPanel.addGlue();
        buttonsPane2.addGlue();
        add(buttonsPanel, BorderLayout.SOUTH);

    }

}
