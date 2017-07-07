package util.janken;

import android.net.Uri;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * This class handles HTTP GET and POST request to the server.
 * No type-specific implementation of any of the class' methods
 * are available, so all responses from the server should be
 * checked for type validity before casting.
 *
 * <p> Before the class may be used, a system property representing
 * the server's host address must be set at some location.
 *
 * @see URL
 * @see HttpURLConnection
 * @see Map
 */
public class HttpHandler
{
    private String address = System.getProperty(Keys.URL);

    /**
     * Sends an HTTP POST request to the server.
     *
     * @param params a map of parameter names and values.
     * @return the response code for the request or -1
     *         if an exception was caught.
     */
    public int sendPost(Map<String, String> params)
    {
        try
        {
            URL url = new URL(address);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(false);
            conn.setDoOutput(true);

            String query = prepareQuery(params);

            System.out.println("Connecting to " + url.toString() + "...");
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            conn.connect();
            return conn.getResponseCode();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Sends an HTTP GET request to the server.
     *
     * @param params a map of parameter names and values.
     * @return the content of the server's response or
     *         <tt>null</tt> if an exception was caught.
     */
    public Object sendGet(Map<String, String> params)
    {
        try
        {
            String query = prepareQuery(params);
            URL url = new URL(address + "?" + query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();

            ObjectInputStream in = new ObjectInputStream(conn.getInputStream());

            try
            {
                return in.readObject();
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace(System.err);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
        }

        return null;
    }

    private String prepareQuery(Map<String, String> params)
    {
        Uri.Builder builder = new Uri.Builder();

        for (String paramName : params.keySet())
            builder.appendQueryParameter(paramName, params.get(paramName));

        return builder.build().getEncodedQuery();
    }
}
