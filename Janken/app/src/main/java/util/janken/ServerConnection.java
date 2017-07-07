package util.janken;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a connection to the game server by a client.
 * It contains methods to send and receive messages from the server,
 * as well as to release internal resources. The class encapsulates
 * the input and output streams between client and server sockets.
 *
 * @see Socket
 * @see ObjectInputStream
 * @see ObjectOutputStream
 */
public class ServerConnection
{
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean isConnected = false;
    private final int TIMEOUT = 1000;

    /**
     * Constructs an instance of this class and creates a socket
     * connection to the specified host at the specified port.
     * The connection may time out after a fixed delay if
     * unsuccessful.
     *
     * @param host the host address of the server.
     * @param port the port of some game instance on the server.
     */
    public ServerConnection(String host, int port)
    {
        try
        {
            System.out.println("Attempting to establish connection...");
            Socket clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(host, port), TIMEOUT);
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            isConnected = true;
            System.out.println("Connection established!");
        }
        catch (UnknownHostException e)
        {
            System.err.println("Host unknown: " + host + ".");
        }
        catch (IOException e)
        {
            System.err.println("Couldn't establish I/O for the connection to: "
                    + host + ".");
            e.printStackTrace();
        }
    }

    /**
     * Closes the server connection, releasing all managed resources.
     */
    public void close()
    {
        try
        {
            in.close();
            out.close();
            isConnected = false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Sends a message string to the server.
     * The message is sent unchecked; it is
     * up to the sender to affirm that the
     * message corresponds with the server's
     * expectations.
     *
     * @param message the message to be sent.
     */
    public void sendMessage(String message)
    {
        try
        {
            out.writeObject(message);
            out.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Waits for a message string from the server.
     * This method blocks until input is available.
     *
     * @return the received message or an empty string
     *         if an exception was caught.
     */
    public String waitForMessage()
    {
        String message = "";

        try
        {
            message = (String) in.readObject();

            return message;
        }
        catch(IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        return message;
    }

    /**
     * Waits for a list of players from the server.
     * This method blocks until input is available.
     *
     * @return the received message or an empty list
     *         if an exception was caught. The received
     *         message may be an empty list even in
     *         the event of successful execution.
     * @see List
     */
    public List<String> waitForPlayerlist()
    {
        List<String> players = new ArrayList<>();

        try
        {
            players = (List<String>) in.readObject();
        }
        catch(IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        return players;
    }

    /**
     * Used to check the connection status of the server.
     *
     * @return <tt>true</tt> if a connection to the server
     *         has been established or <tt>false</tt> if
     *         no connection exists.
     */
    public boolean isConnected()
    {
        return isConnected;
    }
}
