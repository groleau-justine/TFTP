package tftp_perissier_groleau;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
        short exBlock = 1;
        int dataLength, Cr_rv;
        FileOutputStream fileWriter;
        this.serveurIP = serveurIP;
        
        //Créer et envoyer le datagramPacket RRQ
        DatagramPacket dpRRQ = newRQdp((short)1, nomFichierDistant, "octet", serveurIP);
        sc.send(dpRRQ);
        
        //Ecriture dans le fichier de destination
        fileWriter = new FileOutputStream(nomFichier);
        
        while (!isClose) {
            
            //Créer le datagramPacket de réception des données
            DatagramPacket dpr = receive();
            
            if (dpr.getData()[1] == 5) {
                String erreurText = new String (dpr.getData());
                System.out.println("Erreur n°" + dpr.getData()[2] + dpr.getData()[3] + " : " + erreurText.substring(4, dpr.getLength() - 1));
            }
            else if (dpr.getData()[1] == 3) {
                short block = (short) (((dpr.getData()[2] & 0xFF) << 8) | (dpr.getData()[3] & 0xFF));
                dataLength = dpr.getLength() - 4;
                
                //Création du fichier de destination
                File fichier = new File(nomFichier);
                fichier.createNewFile();

                if (fichier.exists())
                {
                    try {
                        //Si c'est le 1er block de données
                        if (block == 1 && exBlock == block) {
                            fileWriter.write(dpr.getData(), 4, dataLength);
                            
                            serveurPortAfter = dpr.getPort();
                            
                            DatagramPacket dpACK = newACKdp((short)4, block, serveurIP);
                            sc.send(dpACK);
                        }
                        else if (block == exBlock + 1) {
                            fileWriter.write(dpr.getData(), 4, dataLength);
                            
                            exBlock++;
                            
                            DatagramPacket dpACK = newACKdp((short)4, block, serveurIP);
                            sc.send(dpACK);
                        }
                        else {
                            System.out.println("Block : " + block + " - Un problème de numéro de block a été détecté !");
                            
                            DatagramPacket dpERROR = newERRORdp((short)5, (short)1, "Le numéro de block ne correspond pas à la demande !", serveurIP);
                            sc.send(dpERROR);
                        }
                        
                        if (dpr.getLength() < 512) {
                            isClose = true;
                        }
                    }
                    catch(IOException ex) { }
                }
            }
        }
        
        //Fermeture du fichier de destination
        fileWriter.close();
    }
    
    //Fonction de réception de datagramme
    private DatagramPacket receive() throws IOException{
        byte[] data = new byte[516];
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
    
    //Fonction de création du datagramPacket ACK
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
    
    //Fonction de création du datagramPacket Error
    private DatagramPacket newERRORdp(short opcode, short error, String messageError, InetAddress addr) {
        
        ByteArrayOutputStream BAout;
        DataOutputStream Dout;
        
        //Create a byte array output stream and write to it through the data output stream methods
        Dout = new DataOutputStream(BAout = new ByteArrayOutputStream(5 + messageError.length()));
        try {
            Dout.writeShort(opcode);
            Dout.writeShort(error);
            Dout.writeBytes(messageError);
            Dout.writeByte(0);
       } catch (IOException ex) { } //Should not happen with fixed byte array

       return(new DatagramPacket(BAout.toByteArray(), BAout.size(), addr, serveurPortAfter));
    }
    
    //Fonction principale du client
    public void run() throws IOException {
        String nomFichier = "C:\\Users\\Epulapp\\Documents\\fichierDestination.txt";
        String nomFichierDistant = "fichierSource.txt";
        receiveFile(nomFichier, nomFichierDistant, serveurIP);
        
        //String nomFichierImage = "C:\\Users\\Epulapp\\Documents\\fichierDestinationImage.png";
        //String nomFichierDistantImage = "fichierSourceImage.png";
        //receiveFile(nomFichierImage, nomFichierDistantImage, serveurIP);
    }
    
}
