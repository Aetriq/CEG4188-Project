package ceg4188.grp2.shared;

/** Server-side cookie model */
public class Cookie {
    private final int id;
    private int x, y;
    private int score;
    private boolean locked = false;
    private String lockedBy = null;

    public Cookie(int id, int x, int y, int score) {
        this.id = id; this.x = x; this.y = y; this.score = score;
    }
    public int getId(){ return id; }
    public synchronized int getX(){ return x; }
    public synchronized int getY(){ return y; }
    public synchronized void setPos(int nx,int ny){ x=nx; y=ny; }
    public synchronized int getScore(){ return score; }
    public synchronized void setScore(int s){ score=s; }
    public synchronized boolean isLocked(){ return locked; }
    public synchronized String getLockedBy(){ return lockedBy; }
    public synchronized boolean lock(String user){
        if (!locked){ locked=true; lockedBy=user; return true; }
        return false;
    }
    public synchronized void unlock(){ locked=false; lockedBy=null; }
}
