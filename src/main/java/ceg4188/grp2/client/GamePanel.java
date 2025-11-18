package ceg4188.grp2.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import ceg4188.grp2.shared.Protocol;

/**
 * Draws cookies, handles clicks, and does simple linear interpolation.
 */
public class GamePanel extends JPanel {

    private final GameScreen parent;
    private final Map<Integer, ViewCookie> cookies = new ConcurrentHashMap<>();
    private ImageIcon bg; // <-- NEW
    private BufferedImage cookieImg, cookieOcc; // <-- Cookie images can stay as BufferedImage
    private PrintWriter out;
    private String username = "Player";

    private final int WORLD_W = 1000, WORLD_H = 500;

    public GamePanel(GameScreen parent) {
        this.parent = parent;
        setPreferredSize(new Dimension(WORLD_W, WORLD_H));
        setBackground(Color.WHITE);

        loadImages();

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { handleClick(e.getX(), e.getY()); }
        });

        Timer t = new Timer(16, ev -> { tick(); repaint(); });
        t.start();
    }

    public void setProtocolOut(PrintWriter out) { this.out = out; }
    public void setUsername(String u){ this.username=u; }

    private void loadImages(){
        try {
            // Use getResource() which returns a URL, perfect for ImageIcon
            bg = new ImageIcon(getClass().getResource("/images/background.png")); // <-- NEW
        } catch (Exception ignored) { System.err.println("Failed to load background"); }
        
        // These are still fine as BufferedImages
        try (InputStream is = getClass().getResourceAsStream("/images/cookie.png")) { if (is!=null) cookieImg = ImageIO.read(is); } catch (Exception ignored){}
        try (InputStream is = getClass().getResourceAsStream("/images/cookie_occupied.png")) { if (is!=null) cookieOcc = ImageIO.read(is); } catch (Exception ignored){}
    }

    private int toWorldX(int px) { return (int)(px * (WORLD_W / (double)getWidth())); }
    private int toWorldY(int py) { return (int)(py * (WORLD_H / (double)getHeight())); }

    private void handleClick(int px, int py) {
        int wx = toWorldX(px), wy = toWorldY(py);
        for (ViewCookie vc : cookies.values()) {
            int dx = wx - vc.x, dy = wy - vc.y;
            if (dx*dx + dy*dy <= 28*28) {
                if (out!=null) out.println(Protocol.LOCK_REQUEST + " " + vc.id);
                else parent.appendMessage("Not connected");
                return;
            }
        }
        parent.appendMessage("No cookie at that position.");
    }

    public void spawnCookie(int id,int x,int y,int score){
        cookies.put(id, new ViewCookie(id,x,y,score));
    }
    public void despawnCookie(int id){ cookies.remove(id); }
    public void moveCookie(int id,int x,int y,long ts){
        ViewCookie v = cookies.get(id);
        if (v != null) {
            v.setTarget(x,y);
        }
    }
    public void setCookieState(int id,int x,int y,boolean locked,String lockedBy,int score){
        ViewCookie v = cookies.get(id);
        if (v==null) v = new ViewCookie(id,x,y,score);
        v.x=x; v.y=y; v.locked=locked; v.lockedBy=lockedBy; v.score=score;
        cookies.put(id,v);
    }
    public void releaseCookieVisual(int id){ ViewCookie v = cookies.get(id); if (v!=null) { v.locked=false; v.lockedBy="-"; } }

    private void tick() {
        // simple interpolation: move displayX toward x by 20% of delta
        for (ViewCookie v : cookies.values()) {
            v.displayX += (v.x - v.displayX) * 0.2;
            v.displayY += (v.y - v.displayY) * 0.2;
        }
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // draw background
        if (bg!=null) {
            // Use .getImage() and pass 'this' as the ImageObserver
            g2.drawImage(bg.getImage(), 0,0,getWidth(),getHeight(), this); // <-- NEW
        } else { 
            g2.setColor(new Color(200,230,255)); g2.fillRect(0,0,getWidth(),getHeight()); 
        }

        double sx = getWidth() / (double) WORLD_W;
        double sy = getHeight() / (double) WORLD_H;
        g2.scale(sx, sy);

        for (ViewCookie v : cookies.values()) {
            int size = 56, half=size/2;
            int cx = v.displayX, cy = v.displayY;

            if (v.locked) {
                if (cookieOcc!=null) g2.drawImage(cookieOcc, cx-half, cy-half, size, size, null);
                else { g2.setColor(Color.PINK); g2.fillOval(cx-half,cy-half,size,size); }
            } else {
                if (cookieImg!=null) g2.drawImage(cookieImg, cx-half, cy-half, size, size, null);
                else { g2.setColor(new Color(255,240,200)); g2.fillOval(cx-half,cy-half,size,size); }
            }

            // score floating number
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            String s = String.valueOf(v.score);
            int w = g2.getFontMetrics().stringWidth(s);
            g2.drawString(s, cx - w/2, cy - half - 6);

            if (v.locked) {
                g2.setColor(Color.RED);
                g2.drawString("Locked: " + v.lockedBy, cx - half, cy + half + 12);
            }
        }
        g2.dispose();
    }

    private static class ViewCookie {
        int id, x,y, displayX, displayY, score;
        boolean locked=false; String lockedBy="-";
        ViewCookie(int id,int x,int y,int score){
            this.id=id; this.x=x; this.y=y; this.displayX=x; this.displayY=y; this.score=score;
        }
        void setTarget(int tx,int ty){ this.x=tx; this.y=ty; }
    }
}
