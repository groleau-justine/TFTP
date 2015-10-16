/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tftp_perissier_groleau;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 *
 * @author Epulapp
 */
public class Client {
    
    private int serveurPort = 69;
    private InetAddress serveurIP;
    private DatagramSocket sc;
    
    public Client() throws SocketException, UnknownHostException {
        serveurIP = InetAddress.getLocalHost();
        sc = new DatagramSocket();
    }
    
    //Fonction de réception de datagramme
    private DatagramPacket receiveFile(String nomFichier, String nomFichierDistant, InetAddress serveurIP) throws IOException{
        this.serveurIP = serveurIP;
        byte[] data = new byte[512];
        
        DatagramPacket dpRRQ;
        
        
        int length = data.length;
        DatagramPacket dp= new DatagramPacket(data, length);
        
        //Création du fichier
        File fichier = new File(nomFichier);
        
        FileOutputStream fout;
        
        
        sc.receive(dp);
        return dp;
    }
    
    //Fonction principale du client
    public void run() throws IOException{
        boolean go = true;
        
        byte[] data = new byte[256];
        int length = data.length;
        DatagramPacket dp = new DatagramPacket(data, length);
        
        while (go){
            System.out.println("Que souhaitez-vous envoyer?");
            Scanner scanner = new Scanner(System.in);
            String chaine = scanner.nextLine();
            chaine += "£";
            System.out.println("J'envoie au port N°" + serveurPort + ".");

            //send(chaine, serveurIP, serveurPort);
            System.out.println("En attente d'une réponse...");
            
            //La chaine "close" ferme la connexion
            if (!"close£".equals(chaine)) {
                dp = receiveFile(chaine, chaine, serveurIP);
                serveurPort = dp.getPort();
                serveurIP = dp.getAddress();
                String reponseTmp = new String(dp.getData());
                String reponse = reponseTmp.substring(0, reponseTmp.indexOf("£"));
                System.out.println(reponse);
            }
            else {
                System.out.println("Fin de connexion avec le port serveur " + serveurPort + ".");
                sc.close();
                go = false;
            }
        }
    }
    
    public static void main(String[] args) throws SocketException, IOException {
        Client cli = new Client();
        cli.run();
    }
}
