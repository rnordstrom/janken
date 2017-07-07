package janken.server;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class represents a generic pair consisting
 * of an input stream and an output stream.
 * As a container class, it provides methods
 * for setting and retrieving each stream.
 * 
 * @author Rikard Nordström
 */
public class StreamPair 
{
    private InputStream in;
    private OutputStream out;
    
    /**
     * Creates a stream pair.
     * 
     * @param in the input stream.
     * @param out the output stream.
     * @see InputStream
     * @see OutputStream
     */
    public StreamPair(InputStream in, OutputStream out)
    {
        this.in = in;
        this.out = out;
    }

    /**
     * Retrieves the object's input stream.
     * 
     * @return the object's input stream.
     * @see InputStream
     */
    public InputStream getInStream() 
    {
        return in;
    }

    /**
     * Sets the object's input stream.
     * 
     * @param in the object's input stream.
     * @see InputStream
     */
    public void setInStream(InputStream in) 
    {
        this.in = in;
    }

    /**
     * Retrieves the object's output stream.
     * 
     * @return the object's output stream.
     * @see OutputStream
     */
    public OutputStream getOutStream() 
    {
        return out;
    }

    /**
     * Sets the object's output stream.
     * 
     * @param out the object's output stream.
     * @see OutputStream
     */
    public void setOutStream(OutputStream out) 
    {
        this.out = out;
    }
}