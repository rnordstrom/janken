package janken.integration;

import janken.entities.Player;
import java.util.ArrayList;
import java.util.HashMap;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * This class manages all communication with the game's
 * database. It contains methods to retrieve scores for
 * all players or for a specific player and to update a
 * player's score, as well as adding a player to the
 * database or checking for their existence/validity.
 * 
 * @author Rikard Nordström
 */
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Stateless
public class JankenDAO 
{
    @PersistenceContext(unitName = "janken_server_webPU")
    private EntityManager em;
    
    /**
     * Retrieves the scores of all connected players.
     * 
     * @param playerNames the names of all players currently
     *                    connected to game instances on the server.
     * @return a map of player names and their corresponding score.
     * @see HashMap
     * @see ArrayList
     */
    public HashMap<String, Integer> getPlayerScores(ArrayList<String> playerNames)
    {
        HashMap<String, Integer> playerScores = new HashMap<>();
        
        for (String name : playerNames)
        {
            if(name != null)
            {
                Player player = em.find(Player.class, name);
            
                if(player != null)
                    playerScores.put(name, player.getTotalScore());
                else
                    playerScores.put(name, 0);
            }
        }
        
        return playerScores;
    }
    
    /**
     * Adds a player to the database.
     * 
     * @param name the player's username.
     * @param password the player's password.
     */
    public void addPlayer(String name, String password)
    {
        Player player = new Player();
        player.setPlayerName(name);
        player.setPassword(password);
        player.setTotalScore(0);
        
        em.persist(player);
    }
    
    /**
     * Verifies the existence of a player.
     * 
     * @param name the player's username.
     * @return <tt>true</tt> if the player exists in the
     *         database and <tt>false</tt> otherwise.
     */
    public boolean playerExists(String name)
    {
        Player player = em.find(Player.class, name);
        
        if(player != null)
            return true;
        
        return false;
    }
    
    /**
     * Validates a player's login details on the server.
     * 
     * @param name the player's username.
     * @param password the player's password.
     * @return <tt>true</tt> if validation is successful
     *         and <tt>false</tt> otherwise.
     */
    public boolean validate(String name, String password)
    {
        Player player = em.find(Player.class, name);
        
        if(player != null && player.getPassword().equals(password))
            return true;
        
        return false;
    }
    
    /**
     * Updates a player's score on the server.
     * 
     * @param name the player's username.
     * @param score the player's new score.
     */
    public void updatePlayerScore(String name, Integer score)
    {
        Player player = em.find(Player.class, name);
        
        if(player != null)
            player.setTotalScore(score);
    }
    
    /**
     * Retrieves the score for a given player.
     * 
     * @param name the player's username.
     * @return the player's score. The default
     *         score is 0.
     */
    public int getPlayerScore(String name)
    {
        Player player = em.find(Player.class, name);
        int score = 0;
        
        if(player != null)
            score = player.getTotalScore();
        
        return score;
    }
}
