package janken.server;

import janken.integration.JankenDAO;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class represents a lobby server for the game Janken.
 * It contains methods to handle HTTP GET and POST requests,
 * in addition to internally managing the creation and monitoring
 * of all game instances on the server. Furthermore, this class
 * delegates the creation and validation of user accounts to
 * appropriate handlers.
 * 
 * <p> The class' HTTP interface allows a client to create
 * a game instance on the server, join any game instance,
 * retrieve all available instances, retrieve scores for
 * all players that are currently in a game and create
 * or log in to a user account.
 * 
 * @author Rikard Nordström
 */
@WebServlet(name = "LobbyServlet", urlPatterns = {"/LobbyServlet"})
public class LobbyServlet extends HttpServlet
{
    private static final String ACTION = "action";
    private static final String INSTANCE_NAME = "name";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CREATE_INSTANCE = "create";
    private static final String ACCOUNT = "account";
    private static final String JOIN_INSTANCE = "join";
    private static final String GET_INSTANCES = "instances";
    private static final String GET_STATS = "stats";
    @Resource
    private ManagedExecutorService instancePool;
    @Resource
    private ManagedScheduledExecutorService monitorPool;
    @Resource
    private ManagedScheduledExecutorService playerStateScheduler;
    private HashMap<String, GameInstanceHandler> instances;
    private ArrayList<Future<String>> runningInstances;
    @EJB
    private JankenDAO jankenDAO;

    @Override
    public void init() throws ServletException 
    {
        super.init();
        
        System.out.println("Initializing LobbyServlet.");
        instances = new HashMap<>();
        runningInstances = new ArrayList<>();
        monitorPool.scheduleAtFixedRate(new InstanceMonitor(), 1, 1, TimeUnit.SECONDS);
    }
    
    private boolean createInstance(String name)
    {
        if(instances.containsKey(name))
        {
            System.out.println("Failed to create game instance.");
            
            return false;
        }
        
        GameInstanceHandler handler = 
                new GameInstanceHandler(name, jankenDAO, playerStateScheduler);
        System.out.println("Game instance created!");
        
        runningInstances.add(instancePool.submit(handler));
        System.out.println("The number of running instances is now " 
                + runningInstances.size() + ".");
        
        instances.put(name, handler);
        
        return true;
    }
    
    private ArrayList<Integer> getInstancePorts(String name)
    {
        GameInstanceHandler instance = instances.get(name);
        
        return instance.getPorts();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        System.out.println("GET request received!");
        
        String action = request.getParameter(ACTION);
        response.setContentType("text/plain;charset=UTF-8");
            
        if(action != null)
        {
            switch(action)
            {
                case JOIN_INSTANCE : 
                    System.out.println("Attempting to join game instance...");
                    try (OutputStream out = response.getOutputStream()) 
                    {
                        ObjectOutputStream outStream = new ObjectOutputStream(out);
                        
                        outStream.writeObject(getInstancePorts(request.getParameter(INSTANCE_NAME)));
                        outStream.flush();
                    }
                    catch(Exception e)
                    {
                        System.err.println("Could not open output stream.");
                    }
                    break;
                case GET_INSTANCES :
                    System.out.println("Fetching game instances...");
                    try (OutputStream out = response.getOutputStream()) 
                    {
                        Set<String> instanceNamesSet = instances.keySet();
                        ArrayList<String> instanceNamesAL = new ArrayList<>();

                        for (String name : instanceNamesSet)
                        {
                            if(instances.get(name).getNumConnections() > 0)
                                instanceNamesAL.add(name);
                        }

                        ObjectOutputStream outStream = new ObjectOutputStream(out);

                        outStream.writeObject(instanceNamesAL);
                        outStream.flush();
                    }
                    catch(Exception e)
                    {
                        System.err.println("Could not open output stream.");
                    }
                    break;
                case GET_STATS :
                    System.out.println("Fetching stats...");
                    try (OutputStream out = response.getOutputStream()) 
                    {
                        ObjectOutputStream outStream = new ObjectOutputStream(out);
                        Set<String> instanceNames = instances.keySet();
                        ArrayList<String> playerNames = new ArrayList<>();

                        for (String iname : instanceNames)
                        {
                            playerNames.addAll(instances.get(iname).getCurrentPlayers());
                        }

                        outStream.writeObject(jankenDAO.getPlayerScores(playerNames));
                        outStream.flush();
                    }
                    catch(Exception e)
                    {
                        System.err.println("Could not open output stream.");
                    }
                    break;
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        System.out.println("POST request received!");
        
        String action = request.getParameter(ACTION);
        
        if(action != null)
        {
            switch(action)
            {
                case CREATE_INSTANCE : 
                    try (PrintWriter out = response.getWriter())
                    {
                        if(createInstance(request.getParameter(INSTANCE_NAME)))
                        {
                            response.setStatus(201);
                            out.write("Instance was successfully created.");
                            out.flush();
                        }
                        else
                        {
                            response.setStatus(400);
                            out.write("Instance was not created.");
                            out.flush();
                        }
                    }
                    catch(Exception e)
                    {
                        System.err.println("Could not open output stream.");
                    }
                    break;
                case ACCOUNT : 
                    try (PrintWriter out = response.getWriter())
                    {
                        System.out.println("Setting up/validating account...");
                        String playerName = request.getParameter(USERNAME);
                        String password = request.getParameter(PASSWORD);
                        
                        if(!jankenDAO.playerExists(playerName))
                        {
                            System.out.println("Registering account " + playerName
                                    + " using password " + password + ".");
                            jankenDAO.addPlayer(playerName, password);
                            
                            response.setStatus(201);
                            out.write("Registration successful.");
                            out.flush();
                        }
                        else if(jankenDAO.validate(playerName, password))
                        {
                            System.out.println("Access granted for " + playerName
                                    + " using password " + password + ".");
                            response.setStatus(200);
                            out.write("Login successful.");
                            out.flush();
                        }
                        else
                        {
                            System.out.println("Access denied for " + playerName
                                    + " using password " + password + ".");
                            response.setStatus(401);
                            out.write("Access denied.");
                            out.flush();
                        }
                    }
                    catch(Exception e)
                    {
                        System.err.println("Could not open output stream.");
                    }
                    break;
            }
        }
    }

    @Override
    public String getServletInfo() 
    {
        return "A janken game lobby server.";
    }
    
    private class InstanceMonitor implements Runnable
    {
        @Override
        public void run()
        {
            try 
            {
                Iterator iterator = runningInstances.iterator();
                
                while (iterator.hasNext())
                {
                    Future<String> future = (Future<String>) iterator.next();
                    
                    if(future.isDone())
                    {
                        String instanceName = future.get();

                        System.out.println("Removing " + instanceName + ".");
                        instances.remove(instanceName);
                        iterator.remove(); // Remove running instance.
                        System.out.println("Number of instances is now: " 
                                + instances.size());
                    }

                }
            } 
            catch (Exception e) 
            {
                e.printStackTrace(System.err);
            }    
        }
    }
}
