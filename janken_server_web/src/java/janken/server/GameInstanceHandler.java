package janken.server;

import janken.integration.JankenDAO;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.ejb.EJBException;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;

/**
 * This class represents a game instance in progress.
 * In addition to managing the lifecycle of the game,
 * including connections, player ready statuses and
 * gameplay decisions, it contains methods to retrieve
 * the names and the amount of connected players,
 * as well as to retrieve its own name and socket ports.
 * 
 * <p> Two separate connections are managed within
 * this class: a primary and a secondary connection.
 * The primary connection is used for all communication
 * that concerns the state of the game, while the
 * secondary connection is used solely for other
 * forms of communication.
 * 
 * @author Rikard Nordström
 */
public class GameInstanceHandler implements Callable<String>
{
    private String name;
    private int port;
    private int utilityPort;
    private ServerSocket serverSocket;
    private ServerSocket serverUtilitySocket;
    private PlayerConnection[] connections = new PlayerConnection[4];
    private int numConnections;
    private boolean inProgress = true;
    private boolean playing = false;
    private boolean roundDone = false;
    private static final int MAX_PLAYERS = 4;
    private static final String READY_MESSAGE = "ready";
    private static final String WAIT_MESSAGE = "wait";
    private static final String CONNECTED_MESSAGE = "connected";
    private static final String PLAYING_MESSAGE = "playing";
    private static final String ROCK = "rock";
    private static final String PAPER = "paper";
    private static final String SCISSORS = "scissors";
    private JankenDAO jankenDAO;
    private ManagedScheduledExecutorService playerStateScheduler;
    
    /**
     * Allocates all resources necessary to manage the
     * game session, as well as scheduling a player connection
     * monitor.
     * 
     * @param name the name of the instance.
     * @param jankenDAO an EJB managing database connections.
     * @param playerStateScheduler a scheduled thread pool that manages
     *                             the player connection monitor.
     * @see JankenDAO
     * @see ManagedScheduledExecutorService
     */
    public GameInstanceHandler(String name, JankenDAO jankenDAO, 
            ManagedScheduledExecutorService playerStateScheduler)
    {
        this.name = name;
        this.jankenDAO = jankenDAO;
        this.playerStateScheduler = playerStateScheduler;
        
        for (int i = 0; i < connections.length; i++)
            connections[i] = new PlayerConnection();
        
        try 
        {
            serverSocket = new ServerSocket(0);
            serverSocket.setSoTimeout(1000);
            serverUtilitySocket = new ServerSocket(0);
            port = serverSocket.getLocalPort();
            utilityPort = serverUtilitySocket.getLocalPort();
        } 
        catch (IOException ioe) 
        {
            ioe.printStackTrace(System.err);
        }
        
        this.playerStateScheduler
                .scheduleAtFixedRate(new PlayerMonitor(), 1, 1, TimeUnit.SECONDS);
    }
    
    @Override
    public String call()
    {
        try 
        {
            System.out.println("Game instance " + name + " is in progress.");
            while (inProgress)
            {
                try 
                {
                    if(numConnections < MAX_PLAYERS && roundDone == false)
                    {   
                        System.out.println("Waiting for a player to connect...");
                        boolean tryToConnect = true;

                        while(tryToConnect)
                        {
                            try 
                            {
                                connections[numConnections]
                                        .setSocket(serverSocket.accept());

                                System.out.println("A player has connected to game instance " 
                                + name + "!");
                                tryToConnect = false;
                            } 
                            catch (SocketTimeoutException ste) 
                            {
                                // System.err.println(ste.toString());

                                for (int i = 0; i < numConnections; i++)
                                {
                                    PlayerConnection player = connections[i];

                                    try 
                                    {
                                        ObjectInputStream in = 
                                            (ObjectInputStream) player.getUtilityStreams()
                                                    .getInStream();

                                        in.skipBytes(in.available());

                                        String doki = (String) in.readObject();
                                    } 
                                    catch(ClassNotFoundException cnfe)
                                    {
                                        cnfe.printStackTrace(System.err);
                                    }
                                    catch(IOException ioe) 
                                    {
                                        // System.err.println(ioe.toString());
                                        // handleDisconnect(i);
                                        
                                        Thread.sleep(500);
                                        
                                        i--;

                                        if(numConnections == 0)
                                            tryToConnect = false;
                                    }
                                }
                            }
                        }

                        ObjectOutputStream out = 
                        new ObjectOutputStream(connections[numConnections]
                                .getSocket().getOutputStream());
                        ObjectInputStream in = 
                                new ObjectInputStream(connections[numConnections]
                                        .getSocket().getInputStream());

                        connections[numConnections]
                                .setUtilitySocket(serverUtilitySocket.accept());

                        ObjectOutputStream utilityOut = 
                                new ObjectOutputStream(connections[numConnections]
                                        .getUtilitySocket().getOutputStream());
                        ObjectInputStream utilityIn = 
                                new ObjectInputStream(connections[numConnections]
                                        .getUtilitySocket().getInputStream());

                        String playerName = "";

                        try 
                        {
                            System.out.println("Waiting for player to send their name...");
                            Object input = in.readObject();
                            playerName = (String) input;
                            System.out.println("Player " + playerName + " has joined!");

                            connections[numConnections].setPlayerName(playerName);
                        } 
                        catch (ClassNotFoundException cnfe) 
                        {
                            cnfe.printStackTrace(System.err);
                        }

                        StreamPair playerStreamPair = new StreamPair(in, out);
                        StreamPair utilityStreamPair = new StreamPair(utilityIn, utilityOut);
                        connections[numConnections].setStreams(playerStreamPair);
                        connections[numConnections].setUtilityStreams(utilityStreamPair);

                        numConnections++;
                    }
                } 
                catch(IOException ioe) 
                {
                    ioe.printStackTrace(System.err);
                }
                catch(NullPointerException npe)
                {
                    System.err.println(npe.toString());
                    break;
                }

                if(numConnections < 2)
                {
                    roundDone = false;
                    continue;
                }
                else if(numConnections >= 2 && numConnections < MAX_PLAYERS)
                {
                    if(roundDone == false)
                        broadcast(CONNECTED_MESSAGE);

                    for (int i = 0; i < numConnections; i++)
                    {
                        PlayerConnection player = connections[i];

                        try 
                        {
                            ObjectInputStream in = 
                                    (ObjectInputStream) player.getStreams().getInStream();

                            System.out.println("Waiting for ready status from " 
                                    + player.getPlayerName() + "...");
                            Object input = in.readObject();
                            System.out.println("Ready status received from " 
                                    + player.getPlayerName() + "!");
                            String decision = (String) input;
                            System.out.println("Status is " + decision + ".");

                            if(decision.equals(READY_MESSAGE))
                                player.setReady(true);
                            else if(decision.equals(WAIT_MESSAGE))
                                player.setReady(false);
                        } 
                        catch(ClassNotFoundException cnfe) 
                        {
                            cnfe.printStackTrace(System.err);
                        }
                        catch(NullPointerException npe)
                        {
                            System.err.println(npe.toString());
                        }
                        catch(IOException ioe)
                        {
                            if(ioe instanceof EOFException)
                            {
                                // handleDisconnect(i);
                                
                                Thread.sleep(500);
                                
                                i--;
                            }
                            else
                                ioe.printStackTrace(System.err);
                        }
                    }

                    int readyCount = 0;
                    int waitCount = 0;

                    for (int i = 0; i < numConnections; i++)
                    {
                        PlayerConnection player = connections[i];

                        try 
                        {
                            if (player.isReady())
                                readyCount++;
                            else
                                waitCount++;
                        } 
                        catch (NullPointerException npe) 
                        {
                            System.err.println(npe.toString());
                        }
                    }

                    System.out.println(readyCount + " players are ready; "
                        + waitCount + " players want to wait.");

                    if(readyCount <= waitCount || numConnections < 2)
                    {
                        System.out.println("Waiting for more players...");
                        roundDone = false;
                        broadcast(WAIT_MESSAGE);
                        continue;
                    }
                    else
                    {
                        System.out.println("Round starting!");
                        playing = true;
                        broadcast(PLAYING_MESSAGE);
                    }
                }
                else
                    playing = true;

                if(playing)
                {
                    for (int i = 0; i < numConnections; i++)
                    {
                        PlayerConnection player = connections[i];

                        try 
                        {
                            ObjectInputStream in
                                    = (ObjectInputStream) player.getStreams().getInStream();

                            System.out.println("Waiting for choice from " 
                                    + player.getPlayerName() + "...");
                            Object input = in.readObject();
                            System.out.println("Choice received from " 
                                    + player.getPlayerName() + "!");
                            String choice = (String) input;
                            System.out.println("The choice was " + choice + ".");

                            player.setChoice(choice);
                        } 
                        catch(ClassNotFoundException cfne) 
                        {
                            cfne.printStackTrace(System.err);
                        }
                        catch(NullPointerException npe)
                        {
                            System.err.println(npe.toString());
                        }
                        catch(IOException ioe)
                        {
                            if(ioe instanceof EOFException)
                            {
                                // handleDisconnect(i);
                                
                                Thread.sleep(500);
                                
                                i--;
                            }
                            else
                                ioe.printStackTrace(System.err);
                        }
                    }
                    
                    if(numConnections < 2)
                    {
                        broadcast(WAIT_MESSAGE);
                        continue;
                    }

                    for (int i = 0; i < numConnections; i++)
                    {
                        PlayerConnection player = connections[i];
                        int score = 0;

                        try 
                        {
                            for (int j = 0; j < numConnections; j++)
                            {
                                PlayerConnection otherPlayer = connections[j];

                                if(!player.getPlayerName().equals(otherPlayer.getPlayerName()))
                                {
                                    if(player.getChoice().equals(ROCK)
                                        && otherPlayer.getChoice().equals(SCISSORS))
                                            score++;
                                    else if(player.getChoice().equals(PAPER)
                                        && otherPlayer.getChoice().equals(ROCK))
                                            score++;
                                    else if(player.getChoice().equals(SCISSORS)
                                        && otherPlayer.getChoice().equals(PAPER))
                                            score++;
                                }
                            }

                            player.setScore(score);
                        }
                        catch(NullPointerException npe)
                        {
                            System.err.println(npe.toString());
                        }
                    }

                    PlayerConnection winner = connections[0];

                    for (int i = 1; i < numConnections; i++)
                    {
                        PlayerConnection player = connections[i];

                        try 
                        {
                            if (player.getScore() > winner.getScore()) 
                            {
                                winner = player;
                            }
                        } 
                        catch (NullPointerException npe) 
                        {
                            System.err.println(npe.toString());
                        }
                    } 

                    try 
                    {
                        int winnerScore = jankenDAO.getPlayerScore(winner.getPlayerName());
                        jankenDAO.updatePlayerScore(winner.getPlayerName(), winnerScore + 1);
                    } 
                    catch(NullPointerException npe) 
                    {
                        npe.printStackTrace(System.err);
                    }
                    catch(EJBException ejbe)
                    {
                        System.err.println(ejbe.toString());
                    }

                    broadcast(winner.getPlayerName());
                    System.out.println("The winner is " + winner.getPlayerName()
                            + "!");
                    roundDone = true;
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("Game instance " + name + " has ended"
                    + " unexpectedly.");
            
        
            return name;
        }
        
        System.out.println("Game instance " + name + " has ended.");
        
        return name;
    }
    
    /**
     * Retrieves all players currently in the session.
     * 
     * @return a list of all connected players' names.
     * @see ArrayList
     */
    public ArrayList<String> getCurrentPlayers()
    {
        ArrayList<String> playerNames = new ArrayList<>();
        
        for(PlayerConnection player : connections)
        {
            if(player.getPlayerName() != null)
                playerNames.add(player.getPlayerName());
        }
        
        return playerNames;
    }
    
    /**
     * Retrieves the number of connected players.
     * 
     * @return the number of connected players.
     */
    public int getNumConnections()
    {
        return numConnections;
    }

    /**
     * Retrieves the name of the game instance.
     * 
     * @return the name of the game instance.
     */
    public String getName() 
    {
        return name;
    }

    /**
     * Retrieves the ports for the primary and secondary
     * game instance connections.
     * 
     * @return a list of all ports that the game instance's
     *         sockets possess.
     * @see ArrayList
     */
    public ArrayList<Integer> getPorts() 
    {
        ArrayList<Integer> ports = new ArrayList<>();
        ports.add(port);
        ports.add(utilityPort);
        
        return ports;
    }
    
    private void broadcast(Object message)
    {
        try 
        {
            for (int i = 0; i < numConnections; i++) 
            {
                PlayerConnection player = connections[i];
                
                ObjectOutputStream out
                        = (ObjectOutputStream) player.getStreams().getOutStream();
                
                out.writeObject(message);
                out.flush();
            }
        }
        catch(NullPointerException | IOException e)
        {
            System.err.println(e.toString());
        }
    }
    
    private void broadcastUtility(Object message)
    {
        for (int i = 0; i < numConnections; i++) 
        {
            try 
            {
                PlayerConnection player = connections[i];

                ObjectOutputStream out
                        = (ObjectOutputStream) player.getUtilityStreams()
                                .getOutStream();

                out.writeObject(message);
                out.flush();
            } 
            catch(IOException | NullPointerException e) 
            {
                System.err.println(e.toString());
                handleDisconnect(i);
            }
        }
    }
    
    private void handleDisconnect(int index)
    {
        String playerName = connections[index].getPlayerName();
        System.out.println("Player " + playerName + " has disconnected");
        
        connections[index] = new PlayerConnection();
        
        for (int i = index + 1; i < numConnections; i++)
        {
            connections[index] = connections[i];
            connections[i] = new PlayerConnection();
        }
        
        if(--numConnections == 0)
            inProgress = false;
    }
    
    private class PlayerMonitor implements Runnable
    {
        @Override
        public void run() 
        {
            broadcastUtility(getCurrentPlayers());
        }
    }
}