package janken.entities;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * This is a player entity class for use with JPA,
 * and represents a player. Players are identified
 * by their usernames alone.
 * 
 * @author Rikard Nordström
 */
@Entity
public class Player implements Serializable 
{
    private static final long serialVersionUID = 1L;
    @Id
    private String playerName;
    private String password;
    private int totalScore;
    
    /**
     * Constructs a player.
     */
    public Player()
    {

    }

    /**
     * Retrieves the player's username.
     * 
     * @return the player's username.
     */
    public String getPlayerName() 
    {
        return playerName;
    }

    /**
     * Sets the player's username.
     * 
     * @param playerName the player's username.
     */
    public void setPlayerName(String playerName) 
    {
        this.playerName = playerName;
    }

    /**
     * Retrieves the player's password.
     * 
     * @return the player's password.
     */
    public String getPassword() 
    {
        return password;
    }

    /**
     * Sets the player's password.
     * 
     * @param password the player's password.
     */
    public void setPassword(String password) 
    {
        this.password = password;
    }

    /**
     * Retrieves the player's score.
     * 
     * @return the player's score.
     */
    public Integer getTotalScore() 
    {
        return totalScore;
    }

    /**
     * Sets the player's score.
     * 
     * @param totalScore the player's score.
     */
    public void setTotalScore(Integer totalScore) 
    {
        this.totalScore = totalScore;
    }

    @Override
    public int hashCode() 
    {
        int hash = 0;
        hash += (playerName != null ? playerName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) 
    {
        if (!(object instanceof Player)) 
        {
            return false;
        }
        
        Player other = (Player) object;
        
        if ((this.playerName == null && other.playerName != null) 
                || (this.playerName != null && !this.playerName.equals(other.playerName))) 
        {
            return false;
        }
        
        return true;
    }

    @Override
    public String toString() 
    {
        return "janken.entities.Player[ id=" + playerName + " ]";
    }
    
}
