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
import com.myjeeva.digitalocean.pojo.Droplets;
import com.myjeeva.digitalocean.pojo.Image;
import com.myjeeva.digitalocean.pojo.Region;
import com.myjeeva.digitalocean.pojo.Snapshot;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JButton;

/**
 *
 * @author James
 */
public class Main {

    public static final String serverName = "Stream";
    private static Integer imageId;
    private static final String domainName = "dezil.ddns.net";
    private static final int portToPing = 80;

    private static DigitalOcean apiClient;

    public static boolean serverExist(String ServerName) throws DigitalOceanException, RequestUnsuccessfulException {
        List<Droplet> droplets = apiClient.getAvailableDroplets(0).getDroplets();
        for (Droplet droplet : droplets) {
            if (droplet.getName().equals(serverName)) {
                return true;
            }

        }
        return false;
    }

    public static void destoryServer(String serverName, ActionEvent event) throws DigitalOceanException, RequestUnsuccessfulException {
        System.out.println("Removing Server: " + serverName);
        if (event != null) {
            ((JButton) event.getSource()).setText("Stoping Server");
        }

        List<Droplet> droplets = apiClient.getAvailableDroplets(0).getDroplets();
        for (Droplet droplet : droplets) {
            if (droplet.getName().equals(serverName)) {
                apiClient.deleteDroplet(droplet.getId());
                System.out.println("Removed");
            }

        }
        int i = 0;
        while (serverExist(serverName)) {
            if (i == 0 && event != null) {
                ((JButton) event.getSource()).setText("Stoping Server.");
            } else if (i == 1 && event != null) {
                ((JButton) event.getSource()).setText("Stoping Server..");
            } else if (i == 2 && event != null) {
                ((JButton) event.getSource()).setText("Stoping Server...");
            } else if (event != null) {
                ((JButton) event.getSource()).setText("Stoping Server");
                i = 0;
            }
            i++;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        help();
        MainGUI.busy = false;

    }

    public static void CreateServer(String serverName, ActionEvent event) throws DigitalOceanException, RequestUnsuccessfulException {
        int i = 0;
        System.out.println("Creating Server: " + serverName);
        System.out.println("Please wait");
        ((JButton) event.getSource()).setText("Creating Server.");
        Droplet newDroplet = new Droplet();
        newDroplet.setName(serverName);
        newDroplet.setSize("512mb"); // setting size by slug value
        newDroplet.setRegion(new Region("sfo1")); // setting region by slug value; 
        newDroplet.setImage(new Image(imageId)); // setting by Image Id 1601 => centos-5-8-x64 also available in image slug value
        newDroplet.setEnableBackup(Boolean.FALSE);
        newDroplet.setEnableIpv6(Boolean.FALSE);
        newDroplet.setEnablePrivateNetworking(Boolean.FALSE);
        Droplet droplet = apiClient.createDroplet(newDroplet);
        boolean stop = false;
        ((JButton) event.getSource()).setText("Server Created.");
        ((JButton) event.getSource()).setText("Preparing server");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Created Server.");
        System.out.println("Waiting for server to be ready to reboot.");
        System.out.print("Please wait");
        while (apiClient.getDropletInfo(droplet.getId()).isNew()) {
            System.out.print(".");
            if (i == 0) {
                ((JButton) event.getSource()).setText("Preparing server.");
            } else if (i == 1) {
                ((JButton) event.getSource()).setText("Preparing server..");
            } else if (i == 2) {
                ((JButton) event.getSource()).setText("Preparing server...");
            } else {
                ((JButton) event.getSource()).setText("Preparing server");
                i = 0;
            }
            i++;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("");
        while (!stop) {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                apiClient.rebootDroplet(droplet.getId());
                stop = true;
                System.out.println("rebooted.");
                ((JButton) event.getSource()).setText("Server Booting up.");
            } catch (Exception e) {
                stop = false;
                System.out.println("failed to reboot");
                ((JButton) event.getSource()).setText("Failed to Boot server up.");
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        ((JButton) event.getSource()).setText("Pinging server");
        System.out.println("Waiting for domain to be reachable...");
        System.out.print("Please wait");
        while (!SimplePing(domainName, portToPing)) {
            System.out.print(".");
            if (i == 0) {
                ((JButton) event.getSource()).setText("Pinging server.");
            } else if (i == 1) {
                ((JButton) event.getSource()).setText("Pinging server..");
            } else if (i == 2) {
                ((JButton) event.getSource()).setText("Pinging server...");;
            } else {
                ((JButton) event.getSource()).setText("Pinging server");
                i = 0;
            }
            i++;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("");
        ((JButton) event.getSource()).setText("Server Ready");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        ((JButton) event.getSource()).setText("Stop Server");
        MainGUI.busy = false;
        System.out.println("Server is ready for streaming.");
        serverInfo();
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    if (serverExist(serverName)) {
                        destoryServer(serverName, null);
                    }
                } catch (DigitalOceanException | RequestUnsuccessfulException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, "Shutdown-thread"));
        Scanner myScanner = new Scanner(System.in);
        Preferences userNodeForPackage = java.util.prefs.Preferences.userRoot();
        String APIKey = userNodeForPackage.get("streamdigitaloceanapikey", "");
        if (APIKey == null || APIKey.equalsIgnoreCase("")) {
            System.out.println("Please enter your API key");
            APIKey = myScanner.nextLine();
            userNodeForPackage.put("streamdigitaloceanapikey", APIKey);

        }
        apiClient = new DigitalOceanClient(APIKey);
        new MainGUI().setVisible(true);
        if (!checkClient()) {
            System.out.println("Something went wrong. Check your key");
        }
        String imageID = userNodeForPackage.get("streamdigitaloceanimageid", "");
        if (imageID == null || imageID.equalsIgnoreCase("")) {
            imageId = 11937487;
        } else {
            imageId = Integer.parseInt(imageID);
        }

        wellcomeMessage();
        try {
            serverInfo();
        } catch (DigitalOceanException | RequestUnsuccessfulException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        help();
        while (true) {
            String userInput = myScanner.nextLine().toLowerCase();
            switch (userInput) {
                case "exit":
                    try {
                        if (serverExist(serverName)) {
                            destoryServer(serverName, null);
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
                        if (!serverExist(serverName)) {
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
                        if (serverExist(serverName)) {
                            destoryServer(serverName, null);
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
                        serverInfo();
                    } catch (DigitalOceanException | RequestUnsuccessfulException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
                case "help":
                    help();
                    break;
                case "changeapikey":
                    System.out.println("Please enter your API key");
                    APIKey = myScanner.nextLine();
                    userNodeForPackage.put("streamdigitaloceanapikey", APIKey);
                    apiClient = new DigitalOceanClient(APIKey);
                    if (!checkClient()) {
                        System.out.println("Something went wrong. Check your key");
                    }
                    break;

                case "changesnapshot": {
                    try {
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
                    help();
                    break;
            }
        }
    }

    public static boolean SimplePing(String IP, int Port) {
        Socket socket = null;
        boolean reachable = false;
        try {
            socket = new Socket(IP, Port);
            reachable = true;
        } catch (IOException ex) {

        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        return reachable;
    }

    private static void help() {
        System.out.println("Your commands are:");
        System.out.println("Create");
        System.out.println("Destory");
        System.out.println("serverInfo");
        System.out.println("changeapikey");
        System.out.println("changesnapshot");
        System.out.println("help");
        System.out.println("exit");
        System.out.println("");
    }

    private static void wellcomeMessage() {
        System.out.println("Welcome Dez!");
        System.out.println("");
    }

    private static void serverInfo() throws DigitalOceanException, RequestUnsuccessfulException {
        System.out.println("Your current server info:");
        Droplets availableDroplets = apiClient.getAvailableDroplets(1);
        System.out.println("Your have " + availableDroplets.getDroplets().size() + " number of droplets");
        List<Droplet> droplets = availableDroplets.getDroplets();
        for (Droplet droplet : droplets) {
            String stat1;
            if (!droplet.isOff()) {
                stat1 = "online";
            } else {
                stat1 = "offline";
            }
            System.out.println(droplet.getId() + "-" + droplet.getName() + " is currently " + stat1 + " & " + droplet.getStatus());
        }
        System.out.println("");
    }

    private static boolean checkClient() {
        try {
            apiClient.getAccountInfo();
        } catch (DigitalOceanException | RequestUnsuccessfulException ex) {
            return false;
        }
        System.out.println("Connected, API key is correct.");
        return true;
    }

    static boolean SimplePing() {
        return SimplePing(domainName, portToPing);
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
                Main.CreateServer(Main.serverName, event);
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
                Main.destoryServer(Main.serverName, event);
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
