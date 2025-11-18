package ceg4188.grp2.shared;

/**
 * Simple text protocol tokens (space separated).
 * Keep names stable and descriptive.
 */
public class Protocol {
    // Join/lobby
    public static final String JOIN = "JOIN";                  // JOIN username
    public static final String JOIN_OK = "JOIN_OK";            // JOIN_OK
    public static final String JOIN_FAIL = "JOIN_FAIL";        // JOIN_FAIL <reason>
    public static final String LOBBY_UPDATE = "LOBBY_UPDATE";  // LOBBY_UPDATE <comma-separated players>
    public static final String LOBBY_SETTINGS = "LOBBY_SETTINGS"; // LOBBY_SETTINGS <durationSec> <minC> <maxC> <minScore> <maxScore> <spawnMinMs> <spawnMaxMs> <maxCookies>
    public static final String START_GAME = "START_GAME";      // START_GAME <endEpochMillis>
    public static final String GAME_ALREADY_STARTED = "GAME_ALREADY_STARTED";

    // Cookie lifecycle
    public static final String COOKIE_SPAWN = "COOKIE_SPAWN";   // COOKIE_SPAWN id x y score
    public static final String COOKIE_DESPAWN = "COOKIE_DESPAWN"; // COOKIE_DESPAWN id
    public static final String COOKIE_MOVE = "COOKIE_MOVE";     // COOKIE_MOVE id x y ts
    public static final String COOKIE_STATE = "COOKIE_STATE";   // COOKIE_STATE id x y locked lockedBy score

    // Locking & clicking
    public static final String LOCK_REQUEST = "LOCK";           // LOCK id
    public static final String LOCK_GRANTED = "LOCK_GRANTED";   // LOCK_GRANTED id username
    public static final String LOCK_DENIED = "LOCK_DENIED";     // LOCK_DENIED id
    public static final String CLICK = "CLICK";                 // CLICK id
    public static final String CLICKED = "CLICKED";             // CLICKED id username score total

    // Game flow
    public static final String COOKIE_COUNT = "COOKIE_COUNT";   // COOKIE_COUNT totalScore
    public static final String LEADERBOARD = "LEADERBOARD";     // LEADERBOARD user:score,...
    public static final String MSG = "MSG";                     // MSG text
    public static final String ERROR = "ERROR";                 // ERROR text
}
