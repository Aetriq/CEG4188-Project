package ceg4188.grp2.server;

import java.util.List;
import java.util.Random;

import ceg4188.grp2.shared.Cookie;
import ceg4188.grp2.shared.GameState;
import ceg4188.grp2.shared.Protocol;

/**
 * Moves/spawns/despawns cookies while gameRunning.
 */
public class CookieManager implements Runnable {
    private final GameState state;
    private final List<ServerClientHandler> clients;
    private final Random rnd = new Random();

    public CookieManager(GameState state, List<ServerClientHandler> clients) {
        this.state = state; this.clients = clients;
    }

    @Override
    public void run() {
        while (true) {
            try { Thread.sleep(200); } catch (InterruptedException e) { break; }
            if (!state.isGameRunning()) continue;

            // spawn occasionally
            if (rnd.nextDouble() < 0.05) {
                Cookie c = state.spawnRandomCookie();
                if (c != null) broadcast(Protocol.COOKIE_SPAWN + " " + c.getId() + " " + c.getX() + " " + c.getY() + " " + c.getScore());
            }

            // move all cookies
            for (Cookie c : state.getAllCookies()) {
                state.moveCookieRandomly(c.getId());
                Cookie nc = state.getCookie(c.getId());
                if (nc != null) broadcast(Protocol.COOKIE_MOVE + " " + nc.getId() + " " + nc.getX() + " " + nc.getY() + " " + System.currentTimeMillis());
            }

            // check end
            if (state.isGameRunning() && System.currentTimeMillis() >= state.getEndEpoch()) {
                state.endGame();
                broadcast(Protocol.MSG + " Game finished!");
                // send leaderboard
                var map = state.getScoresSnapshot();
                String s = map.entrySet().stream().map(e->e.getKey()+":"+e.getValue()).reduce((a,b)->a+","+b).orElse("");
                broadcast(Protocol.LEADERBOARD + " " + s);
            }
        }
    }

    private void broadcast(String msg) {
        synchronized (clients) { for (ServerClientHandler c : clients) c.send(msg); }
    }
}
