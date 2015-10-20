package tftp_perissier_groleau;

import java.io.IOException;
import java.net.SocketException;

/**
 * @author Epulapp
 */
public class Main {

    /**
     * @param args the command line arguments
     * @throws java.net.SocketException
     */
    public static void main(String[] args) throws SocketException, IOException {
        IHM ihm = new IHM();
        ihm.setVisible(true);
        ihm.setLocationRelativeTo(null);
    }
    
}
