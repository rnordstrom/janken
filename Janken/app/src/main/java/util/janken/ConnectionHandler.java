package util.janken;

/**
 * This class holds two instances of a server connection:
 * a primary connection and a secondary connection.
 * Either connection may be set or retrieved at any time.
 *
 * @see ServerConnection
 */
public class ConnectionHandler
{
    private static ServerConnection connection;
    private static ServerConnection utilityConnection;

    /**
     * Retrieves the primary connection.
     * This connection is intended for game state
     * communication.
     *
     * @return the primary server connection object or <tt>null</tt>
     *         if no such object has been previously set.
     */
    public static synchronized ServerConnection getConnection()
    {
        return connection;
    }

    /**
     * Sets the primary connection to the server.
     * This connection is intended for game state
     * communication.
     *
     * @param con the primary server connection.
     */
    public static synchronized void setConnection(ServerConnection con)
    {
        ConnectionHandler.connection = con;
    }

    /**
     * Retrieves the secondary connection.
     * This connection is intended for any
     * communication that does not affect the
     * state of the game.
     *
     * @return the secondary server connection object or <tt>null</tt>
     *         if no such object has been previously set.
     */
    public static ServerConnection getUtilityConnection()
    {
        return utilityConnection;
    }

    /**
     * Sets the secondary connection to the server.
     * This connection is intended for any
     * communication that does not affect the
     * state of the game.
     *
     * @param utilityConnection the secondary server connection.
     */
    public static void setUtilityConnection(ServerConnection utilityConnection)
    {
        ConnectionHandler.utilityConnection = utilityConnection;
    }
}