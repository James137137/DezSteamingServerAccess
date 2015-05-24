/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137.dezsteamingserveraccess;

import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.pojo.Droplet;
import com.myjeeva.digitalocean.pojo.Droplets;
import com.myjeeva.digitalocean.pojo.Image;
import com.myjeeva.digitalocean.pojo.Images;
import com.myjeeva.digitalocean.pojo.Region;
import com.myjeeva.digitalocean.pojo.Snapshot;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JButton;

/**
 *
 * @author James
 */
public class MyAPIMethods {

    public static List<String> getServerInfo() throws MalformedURLException, IOException {
        URL oracle = new URL("http://" + Main.domainName + "/computerInfo.txt");
        List<String> serverInfo = new ArrayList<>();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(oracle.openStream()));

        String inputLine;
        String freeMemory = null;
        String totalMemory = null;
        boolean memoryDone = false;
        boolean internetDone  = false;
        String data = null;
        int line = 1;
        while ((inputLine = in.readLine()) != null) {
            if (line == 1) {
                serverInfo.add("CPU:" + "\t" + inputLine + "%");
            } else if (line == 3) {
                int count = 1;
                for (String arg : inputLine.split(" ")) {
                    if (!arg.equals("")) {
                        if (count == 2) {
                            totalMemory = arg;
                        }
                        count++;
                    }

                }
            } else if (line == 4) {
                int count = 1;
                for (String arg : inputLine.split(" ")) {
                    if (!arg.equals("")) {
                        if (count == 4) {

                            freeMemory = arg;
                        }
                        count++;
                    }

                }

            } else if (line == 9) {
                int count = 1;
                for (String arg : inputLine.split(" ")) {
                    if (!arg.equals("")) {
                        if (count == 9) {
                            data = arg;
                        } else if (count == 10) {
                            data += " " + arg;
                        }
                        count++;
                    }

                }
            }
            if (data != null && !internetDone) {
                serverInfo.add("internet usage: " + data);
                internetDone = true;
            }

            if (totalMemory != null && freeMemory != null && !memoryDone) {
                int memoryFreeP = 100 - (int) (Double.parseDouble(freeMemory) * 100 / Double.parseDouble(totalMemory));
                serverInfo.add("Memory:" + "\t" + memoryFreeP + "%");
                memoryDone = true;
            }

            line++;
        }
        in.close();

        return serverInfo;
    }

    public static void serverInfo() throws DigitalOceanException, RequestUnsuccessfulException {
        System.out.println("Your current server info:");
        Droplets availableDroplets = Main.apiClient.getAvailableDroplets(1);
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

    public static boolean SimplePing() {
        try {
            return java.net.InetAddress.getByName(Main.domainName).isReachable(5000);
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean serverExist(String ServerName) throws DigitalOceanException, RequestUnsuccessfulException {
        List<Droplet> droplets = Main.apiClient.getAvailableDroplets(0).getDroplets();
        for (Droplet droplet : droplets) {
            if (droplet.getName().equals(Main.serverName)) {
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
        List<Droplet> droplets = Main.apiClient.getAvailableDroplets(0).getDroplets();
        for (Droplet droplet : droplets) {
            if (droplet.getName().equals(serverName)) {
                Main.apiClient.deleteDroplet(droplet.getId());
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

    public static boolean checkClient() {
        try {
            Main.apiClient.getAccountInfo();
        } catch (DigitalOceanException | RequestUnsuccessfulException ex) {
            return false;
        }
        System.out.println("Connected, API key is correct.");
        return true;
    }

    public static void wellcomeMessage() {
        System.out.println("Welcome Dez!");
    }

    public static void CreateServer(String serverName, ActionEvent event) throws DigitalOceanException, RequestUnsuccessfulException {
        int i = 0;
        System.out.println("Creating Server: " + serverName);
        System.out.println("Please wait");
        ((JButton) event.getSource()).setText("Creating Server.");
        Droplet newDroplet = new Droplet();
        newDroplet.setName(serverName);
        newDroplet.setSize("512mb");
        newDroplet.setRegion(new Region("sfo1"));
        newDroplet.setImage(new Image(Main.imageId));
        newDroplet.setEnableBackup(Boolean.FALSE);
        newDroplet.setEnableIpv6(Boolean.FALSE);
        newDroplet.setEnablePrivateNetworking(Boolean.FALSE);
        Droplet droplet = Main.apiClient.createDroplet(newDroplet);
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
        while (Main.apiClient.getDropletInfo(droplet.getId()).isNew()) {
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
                Main.apiClient.rebootDroplet(droplet.getId());
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
        while (!SimplePing()) {
            System.out.print(".");
            if (i == 0) {
                ((JButton) event.getSource()).setText("Pinging server.");
            } else if (i == 1) {
                ((JButton) event.getSource()).setText("Pinging server..");
            } else if (i == 2) {
                ((JButton) event.getSource()).setText("Pinging server...");
                ;
            } else {
                ((JButton) event.getSource()).setText("Pinging server");
                i = 0;
            }
            i++;
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

    public static void help() {
        System.out.println("Your commands are:");
        System.out.println("Create");
        System.out.println("Destory");
        System.out.println("serverInfo");
        System.out.println("changeapikey");
        System.out.println("changesnapshot");
        System.out.println("help");
        System.out.println("exit");
    }

    static String[] getSnapshotList() {
        List<String> list = new ArrayList<>();
        list.add(Main.imageId + " - Current");

        try {
            for (Image image : Main.apiClient.getUserImages(0).getImages()) {
                list.add(image.getId() + " - " + image.getName() + " - " + image.getCreatedDate());
            }
        } catch (DigitalOceanException ex) {
            Logger.getLogger(MyAPIMethods.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RequestUnsuccessfulException ex) {
            Logger.getLogger(MyAPIMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    static void changeSnapshotID(int newID) {
        Preferences userNodeForPackage = java.util.prefs.Preferences.userRoot();
        Main.imageId = newID;
        userNodeForPackage.put("streamdigitaloceanimageid", "" + Main.imageId);
    }

}
