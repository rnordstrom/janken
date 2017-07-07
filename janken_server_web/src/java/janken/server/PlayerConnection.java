package janken.server;

import java.net.Socket;

/**
 * This class represents a player connection to a
 * game instance on the server. It is a container class,
 * and as such offers only methods to change and to retrieve
 * the state of the object.
 * 
 * @author Rikard Nordström
 */
public class PlayerConnection 
{
    private String playerName;
    private Socket socket;
    private Socket utilitySocket;
    private StreamPair streams;
    private StreamPair utilityStreams;
    private boolean isReady;
    private String choice;
    private int score;

    /**
     * Retrieves the player's name.
     * 
     * @return the player's name.
     */
    public String getPlayerName() 
    {
        return playerName;
    }

    /**
     * Sets the player's name.
     * 
     * @param playerName the player's name.
     */
    public void setPlayerName(String playerName) 
    {
        this.playerName = playerName;
    }

    /**
     * Retrieves the player's primary
     * socket connection.
     * 
     * @return the player's primary socket.
     * @see Socket
     */
    public Socket getSocket() 
    {
        return socket;
    }

    /**
     * Sets the player's primary socket connection.
     * 
     * @param socket the player's socket.
     * @see Socket
     */
    public void setSocket(Socket socket) 
    {
        this.socket = socket;
    }
    
    /**
     * Retrieves the player's secondary
     * socket connection.
     * 
     * @return the player's secondary socket.
     * @see Socket
     */
    public Socket getUtilitySocket() 
    {
        return utilitySocket;
    }

    /**
     * Sets the player's secondary socket connection.
     * 
     * @param utilitySocket the player's secondary socket.
     * @see Socket
     */
    public void setUtilitySocket(Socket utilitySocket) 
    {
        this.utilitySocket = utilitySocket;
    }

    /**
     * Retrieves the player's primary communication
     * streams.
     * 
     * @return the player's primary streams.
     * @see StreamPair
     */
    public StreamPair getStreams() 
    {
        return streams;
    }

    /**
     * Sets the player's primary communication 
     * streams.
     * 
     * @param streams the player's primary streams.
     * @see StreamPair
     */
    public void setStreams(StreamPair streams) 
    {
        this.streams = streams;
    }

    /**
     * Retrieves the player's secondary communication
     * streams.
     * 
     * @return the player's secondary streams.
     * @see StreamPair
     */
    public StreamPair getUtilityStreams() 
    {
        return utilityStreams;
    }

    /**
     * Sets the player's secondary communication 
     * streams.
     * 
     * @param utilityStreams the player's secondary streams.
     * @see StreamPair
     */
    public void setUtilityStreams(StreamPair utilityStreams) 
    {
        this.utilityStreams = utilityStreams;
    }
    
    /**
     * Retrieves the player's gameplay choice.
     * 
     * @return the player's choice.
     */
    public String getChoice() 
    {
        return choice;
    }

    /**
     * Sets the player's gameplay choice.
     * 
     * @param choice the player's choice.
     */
    public void setChoice(String choice) 
    {
        this.choice = choice;
    }

    /**
     * Retrieves the player's score.
     * The figure represents the number
     * of rounds won.
     * 
     * @return the player's score.
     */
    public int getScore() 
    {
        return score;
    }

    /**
     * Sets the player's score.
     * 
     * @param score the player's score.
     */
    public void setScore(int score) 
    {
        this.score = score;
    }
    
    /**
     * Sets the player's ready status.
     * 
     * @param status the player's status.
     */
    public void setReady(boolean status)
    {
        isReady = status;
    }
    
    /**
     * Used to check if a player is ready
     * to start a round.
     * 
     * @return the player's status.
     */
    public boolean isReady()
    {
        return isReady;
    }
}
