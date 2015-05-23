/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137.dezsteamingserveraccess;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author James
 */
public class CheckServerInfo implements Runnable {

    private void start() {
        Thread t = new Thread(this);
        t.start();
    }

    public CheckServerInfo() {
        start();
    }

    @Override
    public void run() {
        while (true) {
            String output = "<html>";
            try {
                List<String> serverInfo = MyAPIMethods.getServerInfo();
                for (String serverInfo1 : serverInfo) {
                    output += serverInfo1 + "<br>";
                    
                }
                output += "</html>";
                MainGUI.serverInfoJLabel.setText(output);
            } catch (IOException ex) {
                Logger.getLogger(CheckServerInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                Thread.sleep(6000);
            } catch (InterruptedException ex) {
                Logger.getLogger(CheckServerInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
