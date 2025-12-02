package ceg4188.grp2.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Thread-safe server-side game state (players, cookies, scores).
 */
public class GameState {
    private final Map<Integer, Cookie> cookies = new HashMap<>();
    private final Map<String, Integer> scores = new LinkedHashMap<>(); // preserve join order
    private final Random rnd = new Random();
    private int nextId = 1;
    private int totalScore = 0;

    private final GameSettings settings = new GameSettings();
    private boolean gameRunning = false;
    private long gameEndEpoch = 0L;

    // player ops
    public synchronized boolean addPlayer(String username, int maxPlayers) {
        if (scores.size() >= maxPlayers) return false;
        scores.putIfAbsent(username, 0);
        return true;
    }
    public synchronized void removePlayer(String username){ scores.remove(username); }

    public synchronized List<String> getPlayers(){ return new ArrayList<>(scores.keySet()); }
    public synchronized Map<String,Integer> getScoresSnapshot(){ return new LinkedHashMap<>(scores); }

    // cookie ops
    public synchronized Cookie spawnRandomCookie() {
        if (cookies.size() >= settings.maxCookiesAllowed) return null;
        int id = nextId++;
        int x = 50 + rnd.nextInt(900);
        int y = 50 + rnd.nextInt(400);
        int s = settings.minScore + rnd.nextInt(settings.maxScore - settings.minScore + 1);
        Cookie c = new Cookie(id,x,y,s);
        cookies.put(id, c);
        return c;
    }
    public synchronized void despawnCookie(int id){ cookies.remove(id); }
    public synchronized Collection<Cookie> getAllCookies(){ return new ArrayList<>(cookies.values()); }
    public synchronized Cookie getCookie(int id){ return cookies.get(id); }
    public synchronized void moveCookieRandomly(int id){
        Cookie c = cookies.get(id);
        if (c==null) return;
        int nx = Math.max(10, Math.min(990, c.getX() + rnd.nextInt(61)-30));
        int ny = Math.max(10, Math.min(490, c.getY() + rnd.nextInt(61)-30));
        c.setPos(nx, ny);
    }

    public synchronized boolean lockCookie(int id, String username){
        Cookie c = cookies.get(id);
        if (c==null) return false;
        return c.lock(username);
    }
    public synchronized int clickCookie(int id, String username){
        Cookie c = cookies.get(id);
        if (c==null) return -1;

        // Check if the user owns the lock
        if (!username.equals(c.getLockedBy())){
            return -1; // The user dosen't own the lock.
        }

        // Decrease the cookie score by 1.
        int currentScore = c.getScore();
        if (currentScore > 0){
            currentScore --; // Decrease the score by 1;
            c.setScore(currentScore);
        }
        // Give 1 point for each click
        int pointsEarned = 1; 
        totalScore += pointsEarned;
        scores.put(username, scores.getOrDefault(username, 0) + pointsEarned);
    
        // Only destroy the cookie once its score is 0
        if (currentScore <= 0){
            despawnCookie(id); // Remove the cookie when it reaches 0;
            c.unlock(); // Unlock the cookie only when destroyed.
            return pointsEarned; 
        } else{
            // The cookie still needs to be cliked.
            return pointsEarned; // Still give 1 point for the click.
        }
    }

    // settings / game control
    public synchronized GameSettings getSettings(){ return settings; }
    public synchronized void applySettings(GameSettings gs){
        settings.durationSeconds = gs.durationSeconds;
        settings.minCookies = gs.minCookies;
        settings.maxCookies = gs.maxCookies;
        settings.minScore = gs.minScore;
        settings.maxScore = gs.maxScore;
        settings.spawnMinMs = gs.spawnMinMs;
        settings.spawnMaxMs = gs.spawnMaxMs;
        settings.maxCookiesAllowed = gs.maxCookiesAllowed;
    }
    public synchronized void startGame(){
        if (gameRunning) return;
        gameRunning = true;
        gameEndEpoch = System.currentTimeMillis() + settings.durationSeconds * 1000L;
    }
    public synchronized void endGame(){ gameRunning=false; }
    public synchronized boolean isGameRunning(){ return gameRunning; }
    public synchronized long getEndEpoch(){ return gameEndEpoch; }
    public synchronized int getTotalScore(){ return totalScore; }
}
