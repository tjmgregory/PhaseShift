package client.ClientLogic;

import networking.Connection;
import objects.Sendable;
import server.game.*;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;

import objects.InitGame;


/**
 * Created by Patrick on 2/11/2017.
 * Deals with the objects received from the Server
 */
public class ClientReceiver extends Entity {


    private Connection connection;
    private PlayerConnection pconn;
    private ArrayList<Zombie> zombies;
    private ArrayList<Player> players;
    private int mapID;

    /**
     *
     *
     *
     * @param conn the connection to the server
     * 
     */
    public ClientReceiver(Connection conn) {

        this.connection = conn;
       // this.pconn = pconn;

        connection.addFunctionEvent("String", this::getData);
        connection.addFunctionEvent("InitGame",this::setupGame);
        //



    }

    //InitGame object -
    //Create other object GameData


    public void setupGame(Sendable s)
    {
        InitGame i = (InitGame) s;
        HashMap<Integer,Player> players = i.getPlayers();
        HashMap<Integer,Zombie> zombies = i.getZombies();
        int mapID = i.getMapID();

        GameData gd = new GameData(players,zombies,mapID);


    }

    /**
     *
     *
     * @param o method to get some data.
     */
     public void getData(Object o) {
    }

    /**
     *
     * @return the position at a certain moment.
     */
    public Vector2 getPosition()
    {

        return getPos();

    }





}
