package networking;

import client.Client;
import objects.Sendable;
import server.game.Player;

import java.io.IOException;
import java.io.ObjectInputStream;

import static networking.Connection.out;


/**
 * Listens for communications on the network. Then deals with them appropriately?
 */
class NetworkListener implements Runnable {

    private Client client;
    private NetworkEventHandler handler;
    private ObjectInputStream fromConnection;
    private boolean isRunning;

    NetworkListener(ObjectInputStream fromConnection, NetworkEventHandler handler, Client client){
        this.client = client;
        this.fromConnection = fromConnection;
        this.handler = handler;
    }

    /**
     * Blocks connection when waiting for a new object. Hence this is threaded.
     */
    public void run(){
        isRunning = true;
        while(isRunning){
            try {
                Sendable received = (Sendable) fromConnection.readObject();
//                try{
//                    System.err.println("[RECEIVED] "+((Player)received).getPos());
//                } catch (Exception e){}
                handler.queueForExecution(received);
            } catch (IOException e) {
                out("Connection broke. Client returning to main menu.");
                if(client != null) client.returnToMainMenu();
            } catch (ClassNotFoundException e) {
                out("NetworkListener failed to interpret object type.");
                out(e.getMessage());
            }
        }
    }

    /**
     * Closes the input stream.
     * @throws IOException
     */
    void close() throws IOException {
        isRunning = false;
        fromConnection.close();
    }
}
