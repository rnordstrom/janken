package util.janken;

/**
 * This class contains a number of keys for communication and utility.
 */
public class Keys
{
    /** {@value #URL} specifies the server's complete URL.  */
    public static final String URL = "url";
    /** {@value #IP} specifies the server's host IP.  */
    public static final String IP = "ip";
    /** {@value #ACTION} HTTP method parameter name for all actions.  */
    public static final String ACTION = "action";
    /** {@value #INSTANCE_NAME} HTTP method parameter name of a game instance name.  */
    public static final String INSTANCE_NAME = "name";
    /** {@value #USERNAME} HTTP method parameter name of a username.  */
    public static final String USERNAME = "username";
    /** {@value #PASSWORD} HTTP method parameter name of a user password.  */
    public static final String PASSWORD = "password";
    /** {@value #CREATE_INSTANCE} HTTP method parameter value for the action "create instance".  */
    public static final String CREATE_INSTANCE = "create";
    /** {@value #ACCOUNT} HTTP method parameter value for the action "create/validate account".  */
    public static final String ACCOUNT = "account";
    /** {@value #JOIN_INSTANCE} HTTP method parameter value for the action "join instance".  */
    public static final String JOIN_INSTANCE = "join";
    /** {@value #GET_INSTANCES} HTTP method parameter value for the action "get instance".  */
    public static final String GET_INSTANCES = "instances";
    /** {@value #GET_STATS} HTTP method parameter value for the action "get stats".  */
    public static final String GET_STATS = "stats";
    /** {@value #READY_MESSAGE} Server message to engage the "ready" state.  */
    public static final String READY_MESSAGE = "ready";
    /** {@value #WAIT_MESSAGE} Server message to engage or interpret the "wait" state.  */
    public static final String WAIT_MESSAGE = "wait";
    /** {@value #ROCK} Server message to represent the choice "rock".  */
    public static final String ROCK = "rock";
    /** {@value #PAPER} Server message to represent the choice "paper".  */
    public static final String PAPER = "paper";
    /** {@value #SCISSORS} Server message to represent the choice "scissors".  */
    public static final String SCISSORS = "scissors";
    /** {@value #CONNECTED_MESSAGE} Server message to interpret the "connected" state.  */
    public static final String CONNECTED_MESSAGE = "connected";
    /** {@value #PLAYING_MESSAGE} Server message to interpret the "playing" state.  */
    public static final String PLAYING_MESSAGE = "playing";
    /** {@value #HEARTBEAT} Represents a client connection heartbeat.  */
    public static final String HEARTBEAT = "doki";
    /** {@value #MAX_PLAYERS} The maximum number of players for any given game instance.  */
    public static final int MAX_PLAYERS = 4;
}
