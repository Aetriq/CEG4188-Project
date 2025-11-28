package ceg4188.grp2.client;

// Missing import
import java.awt.event.ActionEvent;

//New imports
import java.awt.AlphaComposite;
import java.awt.GradientPaint;
import java.awt.event.ActionListener;
import javax.swing.Timer;


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

    // New variables
    private BufferedImage[] cookieFrames = new BufferedImage[5]; // For the countdown animation.
    private Map<Integer, ViewCookie> animatingCookies = new ConcurrentHashMap<>();

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
            
            // Add the cookie animation for the countdown of (5,4,3,2,1)
            for (int i = 0; i<5; i++ ){
                try (InputStream is = getClass().getResourceAsStream("/images/cookie_" + (5-i) + ".png")) 
                {
                    if (is != null) {
                        cookieFrames[i] = ImageIO.read(is);
                    }else{
                        System.err.println("Could not load cookie_" + (5-i) + ".png");
                          } 
                } 
                catch(Exception e){
                    System.err.println("Error loading cookie_" + (5-i) + ".png: " + e.getMessage());
                }
            } 
        }

        catch (Exception ignored) { System.err.println("Failed to load background"); }
        
        // These are still fine as BufferedImages
        try (InputStream is = getClass().getResourceAsStream("/images/cookie.png")) { if (is!=null) cookieImg = ImageIO.read(is); } catch (Exception ignored){}
        try (InputStream is = getClass().getResourceAsStream("/images/cookie_occupied.png")) { if (is!=null) cookieOcc = ImageIO.read(is); } catch (Exception ignored){}
    }

    // New method for countdown animation
    public void startCookieClickAnimation(int cookieId){
        ViewCookie v = cookies.get(cookieId);
        if (v != null){
            v.animating = true;
            v.animationFrame = 0;
            v.scale = 1.0f;
            animatingCookies.put(cookieId, v);

            // Create animation timer
            Timer animTimer = new Timer(200, new ActionListener() { // 200ms between frames
                @Override
                public void actionPerformed(ActionEvent e) {
                    ViewCookie cookie = animatingCookies.get(cookieId);
                    if (cookie != null) {
                        cookie.animationFrame++;
                        
                        // Bounce effect
                        cookie.scale = 1.0f + (float)Math.sin(cookie.animationFrame * 0.5f) * 0.2f;
                        
                        if (cookie.animationFrame >= 5) { // 5 frames for countdown
                            cookie.animating = false;
                            animatingCookies.remove(cookieId);
                            ((Timer)e.getSource()).stop();
                        }
                        repaint();
                    } else {
                        ((Timer)e.getSource()).stop();
                    }
                }
            });
            animTimer.start();
        }
    }

    // New Method for cookie score decrease
    public void updateCookieScore(int cookieId, int newScore){
        ViewCookie v = cookies.get(cookieId);
        if (v != null){
            v.score = newScore;
            // Use an animation when the score changes.
            startCookieClickAnimation(cookieId);
        }
    }

    // New mthod when the cookie is completely destroyed.
    public void startCookieDestructionAnimation(int cookieId){
        ViewCookie v = cookies.get(cookieId);
        if (v != null){
            v.animating = true;
            v.animationFrame = 0;
            v.scale = 1.0f;
            animatingCookies.put(cookieId, v);

            Timer destructionTimer = new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ViewCookie cookie = animatingCookies.get(cookieId);
                    if (cookie != null) {
                        cookie.animationFrame++;
                        // Shrink and fade out
                        cookie.scale = 1.0f - (cookie.animationFrame * 0.2f);
                        
                        if (cookie.animationFrame >= 5) {
                            cookie.animating = false;
                            animatingCookies.remove(cookieId);
                            cookies.remove(cookieId); // Remove from main cookie list
                            ((Timer)e.getSource()).stop();
                        }
                        repaint();
                    } else {
                        ((Timer)e.getSource()).stop();
                    }
                }
            });
            destructionTimer.start();
        }
    }

    /// NEw method for cookie drawing
    private void drawCookie(Graphics2D g2, ViewCookie v){
        int size = 56, half = size / 2;
        int cx = v.displayX, cy = v.displayY;

        // Apply animation transforms
        if (v.animating) {
            g2.translate(cx, cy);
            g2.scale(v.scale, v.scale);
            g2.translate(-cx, -cy);
        }
        BufferedImage imgToUse = null;

        // Chose the image depending on the score
        if (v.locked) {
        imgToUse = cookieOcc;
        // Add red tint overlay
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        } else{
            if(v.score >=1 && v.score <= 5 && cookieFrames[5 - v.score] != null){
                // This shows the image that matches the current score of the cookie
                imgToUse = cookieFrames[5 - v.score];
            }else{
                // Otherwise we just use the default cookie image.
                imgToUse = cookieImg;
            }
        }

        // Here we draw the cookie image or fallback
        if (imgToUse != null) {
        g2.drawImage(imgToUse, cx - half, cy - half, size, size, null);
        } else {
            // Enhanced fallback drawing
            Color cookieColor = v.animating ? new Color(255, 200, 100) : 
                            v.locked ? new Color(255, 150, 150) : 
                            new Color(255, 223, 186);
            g2.setColor(cookieColor);
            g2.fillOval(cx - half, cy - half, size, size);
            g2.setColor(Color.BLACK);
            g2.drawOval(cx - half, cy - half, size, size);
        }

        // We reset the composite
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        // Better Score Display
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        String scoreText = String.valueOf(v.score);
        int textWidth = g2.getFontMetrics().stringWidth(scoreText);
        
        // Add text background for better readability
        g2.setColor(new Color(0, 0, 0, 128));
        g2.fillRoundRect(cx - textWidth/2 - 3, cy - half - 20, textWidth + 6, 16, 8, 8);
        
        // Draw the score
        g2.setColor(Color.WHITE);
        g2.drawString(scoreText, cx - textWidth/2, cy - half - 6);

        // Enhanced the lock display
        if (v.locked) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            String lockText = "Locked: " + v.lockedBy;
            int lockWidth = g2.getFontMetrics().stringWidth(lockText);
            g2.drawString(lockText, cx - lockWidth/2, cy + half + 15);
        }

        // Reset transforms if we applied animation
        if (v.animating) {
            g2.translate(cx, cy);
            g2.scale(1.0f/v.scale, 1.0f/v.scale);
            g2.translate(-cx, -cy);
        }
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

    public void despawnCookie(int id){ 
        startCookieClickAnimation(id);
    }

    public void moveCookie(int id,int x,int y,long ts){
        ViewCookie v = cookies.get(id);
        if (v != null) {
            v.setTarget(x,y);
        }
    }

    // 
    public void setCookieState(int id,int x,int y,boolean locked,String lockedBy,int score){
        ViewCookie v = cookies.get(id);
        if (v==null) {
            v = new ViewCookie(id,x,y,score);
        }else{
            // We must check if the score has changed and use the animation.
            if (v.score != score){
                v.score = score;
                if (score > 0){
                    startCookieClickAnimation(id);
                }
            }
        }
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
            // Better gradiant background
            GradientPaint gradient = new GradientPaint(0, 0 , new Color(173, 216, 230), 
            getWidth(), getHeight(), new Color(135, 206, 250));

            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight()); 
        }

        double sx = getWidth() / (double) WORLD_W;
        double sy = getHeight() / (double) WORLD_H;
        g2.scale(sx, sy);

        for (ViewCookie v : cookies.values()) {
            drawCookie(g2, v);
        }

        for (ViewCookie v : animatingCookies.values()){
            drawCookie(g2, v);
        }

        g2.dispose();
    }

    private static class ViewCookie {
        int id, x,y, displayX, displayY, score;
        boolean locked=false; String lockedBy="-";
        //New fields
        boolean animating = false;
        int animationFrame = 0;
        float scale = 1.0f;

        ViewCookie(int id,int x,int y,int score){
            this.id=id; this.x=x; this.y=y; this.displayX=x; this.displayY=y; this.score=score;
        }
        void setTarget(int tx,int ty){ this.x=tx; this.y=ty; }
    }
}
