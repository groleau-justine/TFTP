package tftp_perissier_groleau;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author Epulapp
 */
public class Client {
    
    private static final int serveurPort = 69;
    private int serveurPortAfter;
    private InetAddress serveurIP;
    private final DatagramSocket sc;
    
    public Client() throws SocketException, UnknownHostException {
        serveurIP = InetAddress.getLocalHost();
        sc = new DatagramSocket();
    }
    
    //Fonction de réception de datagramme
    private void receiveFile(String nomFichier, String nomFichierDistant, InetAddress serveurIP) throws IOException {
        
        boolean isClose = false;
        int dataLength;
        this.serveurIP = serveurIP;
        
        //Créer et envoyer le datagramPacket RRQ
        DatagramPacket dpRRQ = newRQdp((short)1, nomFichierDistant, "octet", serveurIP);
        sc.send(dpRRQ);
        
        while (!isClose) {
            
            //Créer le datagramPacket de réception des données
            DatagramPacket dpr = receive();
            
            if (dpr.getData()[1] == 5) {
                String erreurText = new String (dpr.getData());
                System.out.println("Erreur n°" + dpr.getData()[2] + dpr.getData()[3] + " : " + erreurText.substring(4, dpr.getLength() - 1));
            }
            else if (dpr.getData()[1] == 3) {
                //Suppression des quatre premiers octets
                String data = new String(dpr.getData());
                short block = (short) (((dpr.getData()[2] & 0xFF) << 8) | (dpr.getData()[3] & 0xFF));
                //data = data.substring(0, dpr.getLength());
                data = data.substring(4, dpr.getLength());
                dataLength = dpr.getLength();
                
                //Création du fichier de destination
                File fichier = new File(nomFichier);
                fichier.createNewFile();

                if (fichier.exists())
                {
                    try {
                        //Ecriture dans le fichier de destination
                        FileWriter fichierWrite;
                        
                        //Si c'est le 1er block de données
                        if (block == 1)
                        {
                            fichierWrite = new FileWriter(nomFichier);
                            fichierWrite.write(data);
                            
                            //Fermeture du fichier de destination
                            fichierWrite.close();
                        }
                        else {
                            fichierWrite = new FileWriter(nomFichier, Boolean.TRUE);
                            fichierWrite.write(data);
                            
                            //Fermeture du fichier de destination
                            fichierWrite.close();
                        }
                        
                        if (dataLength < 512) {
                            isClose = true;
                        }
                        
                        serveurPortAfter = dpr.getPort();
                        
                        DatagramPacket dpACK = newACKdp((short)4, block, serveurIP);
                        sc.send(dpACK);
                    }
                    catch(IOException ex) { }
                }
            }
        }
    }
    
    //Fonction de réception de datagramme
    private DatagramPacket receive() throws IOException{
        byte[] data = new byte[512];
        int length = data.length;
        DatagramPacket dc = new DatagramPacket(data, length);
        sc.receive(dc);
        return dc;
    }
    
    //Fonction de création du datagramPacket RRQ
    private static DatagramPacket newRQdp(short opcode, String file, String mode, InetAddress addr) {
        
        ByteArrayOutputStream BAout;
        DataOutputStream Dout;
        
        //Create a byte array output stream and write to it through the data output stream methods
        Dout = new DataOutputStream(BAout = new ByteArrayOutputStream(4 + file.length() + mode.length()));
        try {
            Dout.writeShort(opcode);
            Dout.writeBytes(file);
            Dout.writeByte(0);
            Dout.writeBytes(mode);
            Dout.writeByte(0);
       } catch (IOException ex) { } //Should not happen with fixed byte array

       return(new DatagramPacket(BAout.toByteArray(), BAout.size(), addr, serveurPort));
    }
    
    //Fonction de création du datagramPacket RRQ
    private DatagramPacket newACKdp(short opcode, short block, InetAddress addr) {
        
        ByteArrayOutputStream BAout;
        DataOutputStream Dout;
        
        //Create a byte array output stream and write to it through the data output stream methods
        Dout = new DataOutputStream(BAout = new ByteArrayOutputStream(4));
        try {
            Dout.writeShort(opcode);
            Dout.writeShort(block);
       } catch (IOException ex) { } //Should not happen with fixed byte array

       return(new DatagramPacket(BAout.toByteArray(), BAout.size(), addr, serveurPortAfter));
    }
    
    //Fonction principale du client
    public void run() throws IOException {
        String nomFichier = "C:\\Users\\Epulapp\\Documents\\fichierDestination.txt";
        String nomFichierDistant = "fichierSource.txt";
        
        receiveFile(nomFichier, nomFichierDistant, serveurIP);
    }
    
}
