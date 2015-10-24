/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137.dezsteamingserveraccess;

import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.Droplet;
import com.myjeeva.digitalocean.pojo.Snapshot;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JOptionPane;

/**
 *
 * @author James
 */
public class Main {

    //cf861046b04df91c60e336433ac1c960b28537a27a8fccc248f5e55d1a58676d
    //fa40b7fe9f73b2d65f7a5800e6b871aabb27d8a39219a401e21c103db0c611fd
    
    
    public static final String serverName = "Stream";
    public static Integer imageId; //11937487 20-05-15   11966377 22-05-15
    public static final String domainName = "dezil.ddns.net";
    //public static final String domainName = "192.241.212.135";
    static boolean debug = true;
    boolean createSnapshotOnStop = false;

    public static DigitalOcean apiClient;

    public static void main(String[] args) {
        Preferences userNodeForPackage = java.util.prefs.Preferences.userRoot();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                if (MainGUI.closeServerOnExit.getState()) {
                    try {
                        if (MyAPIMethods.serverExist(Main.serverName)) {
                            MyAPIMethods.destoryServer(Main.serverName, null);
                        }
                    } catch (DigitalOceanException | RequestUnsuccessfulException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                java.util.prefs.Preferences.userRoot().put("dezsteamingserveraccessdestoryserveronexit", "" + MainGUI.closeServerOnExit.getState());
                java.util.prefs.Preferences.userRoot().put("dezsteamingserveraccesssaveonstop", "" + MainGUI.saveOnStop.getState());

            }
        }, "Shutdown-thread"));
        Scanner myScanner = new Scanner(System.in);
        String APIKey = userNodeForPackage.get("streamdigitaloceanapikey", "");
        if (APIKey == null || APIKey.equalsIgnoreCase("")) {
            boolean result = MyAPIMethods.changeAPIKey();
            if (!result) {
                JOptionPane.showMessageDialog(null, "You must enter an API key");
                System.exit(0);
            }
        }
        apiClient = new DigitalOceanClient(APIKey);
        System.out.println(APIKey);
        new MainGUI().setVisible(true);
        if (!MyAPIMethods.checkClient()) {
            System.out.println("Something went wrong. Check your key");
            boolean result = MyAPIMethods.changeAPIKey();
        }
        String imageID = userNodeForPackage.get("streamdigitaloceanimageid", "");
        if (imageID == null || imageID.equalsIgnoreCase("")) {
            imageId = 11966377;
        } else {
            imageId = Integer.parseInt(imageID);
        }

        MyAPIMethods.wellcomeMessage();
        try {
            MyAPIMethods.serverInfo();
        } catch (DigitalOceanException | RequestUnsuccessfulException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        MyAPIMethods.help();
        while (true) {
            String userInput = myScanner.nextLine().toLowerCase();
            switch (userInput) {
                case "exit":
                    try {
                        if (MyAPIMethods.serverExist(Main.serverName)) {
                            MyAPIMethods.destoryServer(Main.serverName, null);
                        }
                    } catch (DigitalOceanException | RequestUnsuccessfulException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("GoodBye.");
                    System.exit(0);
                    break;
                case "exit2":
                    System.exit(0);
                    break;
                case "create": {
                    try {
                        if (!MyAPIMethods.serverExist(Main.serverName)) {
                            //CreateServer(serverName);
                        } else {
                            System.out.println("server already exist");
                        }

                    } catch (DigitalOceanException | RequestUnsuccessfulException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
                case "destory": {
                    try {
                        if (MyAPIMethods.serverExist(Main.serverName)) {
                            MyAPIMethods.destoryServer(Main.serverName, null);
                        } else {
                            System.out.println("Can't find server");
                        }
                    } catch (DigitalOceanException | RequestUnsuccessfulException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
                case "serverinfo": {
                    try {
                        MyAPIMethods.serverInfo();
                    } catch (DigitalOceanException | RequestUnsuccessfulException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
                case "help":
                    MyAPIMethods.help();
                    break;
                case "changeapikey":
                    System.out.println("Please enter your API key");
                    APIKey = myScanner.nextLine();
                    userNodeForPackage.put("streamdigitaloceanapikey", APIKey);
                    apiClient = new DigitalOceanClient(APIKey);
                    if (!MyAPIMethods.checkClient()) {
                        System.out.println("Something went wrong. Check your key");
                    }
                    break;

                case "changesnapshot": {
                    try {
                        System.out.println("This program is currently using ID: " + imageId);
                        System.out.println("Current snapshots are:");
                        for (Droplet droplet : apiClient.getAvailableDroplets(1).getDroplets()) {
                            List<Snapshot> snapshots = apiClient.getAvailableSnapshots(droplet.getId(), 1).getSnapshots();
                            for (Snapshot snapshot : snapshots) {
                                System.out.println(snapshot.getId() + " - " + snapshot.getName());
                            }
                        }
                    } catch (DigitalOceanException | RequestUnsuccessfulException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                System.out.println("Please enter the ID of snapshot");
                imageId = myScanner.nextInt();
                userNodeForPackage.put("streamdigitaloceanimageid", "" + imageId);
                break;
                default:
                    MyAPIMethods.help();
                    break;
            }
        }
    }

    public static class CreateServer implements Runnable {

        ActionEvent event;

        private void start() {
            Thread t = new Thread(this);
            t.start();
        }

        public CreateServer(ActionEvent event) {
            this.event = event;
            start();
        }

        @Override
        public void run() {
            try {
                MyAPIMethods.CreateServer(Main.serverName, event);
            } catch (DigitalOceanException ex) {
                ((JButton) event.getSource()).setText("Error");
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RequestUnsuccessfulException ex) {
                ((JButton) event.getSource()).setText("Error");
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    static class DestoryServer implements Runnable {

        ActionEvent event;

        private void start() {
            Thread t = new Thread(this);
            t.start();
        }

        public DestoryServer(ActionEvent event) {
            this.event = event;
            start();
        }

        @Override
        public void run() {
            try {
                MyAPIMethods.destoryServer(Main.serverName, event);
                ((JButton) event.getSource()).setText("Start Server");
            } catch (DigitalOceanException ex) {
                ((JButton) event.getSource()).setText("Error");
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                MainGUI.busy = false;
            } catch (RequestUnsuccessfulException ex) {
                ((JButton) event.getSource()).setText("Error");
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                MainGUI.busy = false;
            }

        }
    }
}
