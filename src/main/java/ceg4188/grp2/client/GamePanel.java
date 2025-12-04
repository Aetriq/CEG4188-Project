package ceg4188.grp2.client;

// Missing import
import java.awt.event.ActionEvent;

//New imports
import java.awt.AlphaComposite;
import java.awt.GradientPaint;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.text.View;

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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import ceg4188.grp2.shared.Protocol;

/**
 * Draws cookies, handles clicks, and does simple linear interpolation.
 */
public class GamePanel extends JPanel {

    // New variables
    private static final int COOKIE_RADIUS = 35;
    private static final int COOKIE_HITBOX_PADDING = 10;

    private BufferedImage[] cookieFrames = new BufferedImage[5]; // For the countdown animation.
    private Map<Integer, ViewCookie> animatingCookies = new ConcurrentHashMap<>();

    // Field to keep track of which cookies a user owns.
    private final Set<Integer> ownedCookies = ConcurrentHashMap.newKeySet();

    private int hoveredCookieId = -1; // This tracks which cookie is being hovered.
    private final Color HOVER_COLOR = new Color(0, 255, 0, 100); // Semi-transparent green color.

    private final GameScreen parent;
    private final Map<Integer, ViewCookie> cookies = new ConcurrentHashMap<>();
    private ImageIcon bg; // <-- NEW
    private BufferedImage cookieImg;
    private PrintWriter out;
    private String username = "Player";

    private final int WORLD_W = 1000, WORLD_H = 500;

    public GamePanel(GameScreen parent) {
        this.parent = parent;
        setPreferredSize(new Dimension(WORLD_W, WORLD_H));
        setBackground(Color.WHITE);

        loadImages();

        Timer t = new Timer(16, ev -> { tick(); repaint(); });
        t.start();
    }

    // New method for cookie Owned
    public void setCookieOwned(int cookieId, boolean owned) {
        if (owned) {
            ownedCookies.add(cookieId);
        } else {
            ownedCookies.remove(cookieId);
        }
    }

    public void setProtocolOut(PrintWriter out) { this.out = out; }
    public void setUsername(String u){ this.username=u; }

    private void loadImages(){
        try {
            // Use getResource() which returns a URL, perfect for ImageIcon
            bg = new ImageIcon(getClass().getResource("/images/background.png")); // <-- NEW
            parent.appendMessage("Background loaded " + (bg != null));
            
            // Add the cookie animation for the countdown of (5,4,3,2,1)
            for (int i = 0; i < 5; i++ ){
                String imageName = "/images/cookie_" + (i + 1) + ".png";
                
                try (InputStream is = getClass().getResourceAsStream(imageName)) {
                    if (is != null) {
                        cookieFrames[i] = ImageIO.read(is);
                        parent.appendMessage("Loaded " + imageName);
                    }else{
                        parent.appendMessage("Warning: Could not load " + imageName);
                        // Simple image as fallback
                        BufferedImage fallback = new BufferedImage(56, 56, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2 = fallback.createGraphics();
                        g2.setColor(new Color(255, 223, 186));
                        g2.fillOval(0, 0, 56, 56);
                        g2.setColor(Color.BLACK);
                        g2.drawOval(0, 0, 55, 55);
                        g2.dispose();
                        cookieFrames[i] = fallback;
                    } 
                } 
                catch(Exception e){
                    System.err.println("Error loading " + imageName);
                }
            } 
            if (cookieFrames[0] != null) {
            cookieImg = cookieFrames[0];
            }

        }
        catch (Exception e) { 
            parent.appendMessage("CRITICAL ERROR loading images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // New method to get the current cookie score.
    public int getCookieScore(int cookieId){
        ViewCookie v = cookies.get(cookieId);
        return (v != null) ? v.score : 0;
    }

    // New method to update the score visually
    public void updateCookieVisualScore(int cookieId){
        ViewCookie v = cookies.get(cookieId);
        if (v != null && v.score > 0){
            v.score--; // Deacreate the score by 1 on the client side.
        }
    }

    // New method for countdown animation.
    // Makes sure it syncs with the score.
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
        ViewCookie v = cookies.remove(cookieId); // Remove form the main map.
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
                        cookie.scale = 1.0f - (cookie.animationFrame * 0.25f);
                        
                        if (cookie.animationFrame >= 4) {
                            cookie.animating = false;
                            animatingCookies.remove(cookieId);
                            ((Timer)e.getSource()).stop();
                            parent.appendMessage("Cookie " + cookieId + " fully destroyed");
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

    // Better hitbox handling
    @Override
    public void addNotify() {
        super.addNotify();

        // Adapter that handles both click and movement.
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // MusePressed fires immediatly when the button is pressed.
                handleClick(e.getX(), e.getY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                updateHoveredCookie(e.getX(), e.getY());
            }
        };
        // Add both listeners to the same adapter
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    private void updateHoveredCookie(int px, int py) {
        int wx = toWorldX(px), wy = toWorldY(py);
        int previousHovered = hoveredCookieId;
        hoveredCookieId = -1;
        
        for (ViewCookie vc : cookies.values()) {
            int dx = wx - vc.x, dy = wy - vc.y;
            // Slightly larger hitbox for better usability (increased from 28 to 35)
            if (isPointInCookie(wx, wy, vc)) {
                hoveredCookieId = vc.id;
                break;
            }
        }
        
        // Only repaint if hover state changed
        if (hoveredCookieId != previousHovered) {
            repaint();
        }
    }

    // Method for improved hitbox detection
    private boolean isPointInCookie(int x, int y, ViewCookie cookie) { 
        int dx = x - cookie.x, dy = y - cookie.y;
        int hitboxRadius = COOKIE_RADIUS + COOKIE_HITBOX_PADDING;
        return dx * dx + dy * dy <= hitboxRadius * hitboxRadius;
    }

    /// NEw method for cookie drawing
    private void drawCookie(Graphics2D g2, ViewCookie v){
        // Don't draw te cookies that are being destroyed
        if (v.score <= 0 && v.animating && v.animationFrame >= 3){
            return; 
        }
        int size = 56, half = size / 2;
        int cx = v.displayX, cy = v.displayY;

        // Apply animation transforms
        if (v.animating) {
            g2.translate(cx, cy);
            g2.scale(v.scale, v.scale);
            g2.translate(-cx, -cy);
        }

        // New hover effect
        if(v.id == hoveredCookieId && !v.locked){
            g2.setColor(HOVER_COLOR);
            g2.fillOval(cx - half - 5, cy - half - 5, size + 10, size + 10);
        }

        BufferedImage imgToUse = null;

        if (v.score >=1 && v.score <= 5){
            int imageIndex = v.score -1; 
            if (cookieFrames != null && imageIndex >= 0 && imageIndex < cookieFrames.length){
                    imgToUse = cookieFrames[imageIndex];
            }
        }

         // Fallback to default image
        if (imgToUse == null && cookieImg != null) {
            imgToUse = cookieImg;
        }
        
        // Draw the cookie 
        if (imgToUse != null) {
            // Apply red tint for locked cookies
            if (v.locked) {
                // Draw the cookie image first
                g2.drawImage(imgToUse, cx - half, cy - half, size, size, null);
                // Then overlay a red tint
                g2.setColor(new Color(255, 0, 0, 100)); // Semi-transparent red
                g2.fillOval(cx - half, cy - half, size, size);
                // Draw a red border
                g2.setColor(Color.RED);
                g2.setStroke(new java.awt.BasicStroke(3));
                g2.drawOval(cx - half, cy - half, size, size);
            } else {
                // Draw normally if not locked
                g2.drawImage(imgToUse, cx - half, cy - half, size, size, null);
            }
        } else {
            // Fallback to drawng with colored circles.
             Color cookieColor = v.animating ? new Color(255, 200, 100) : 
                v.locked ? new Color(255, 150, 150) : 
                new Color(255, 223, 186);
            g2.setColor(cookieColor);
            g2.fillOval(cx - half, cy - half, size, size);
            g2.setColor(Color.BLACK);
            g2.drawOval(cx - half, cy - half, size, size);
            
            // Draw the score in the center for the fallback
            if (v.score > 0) {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Times New Roman", Font.BOLD, 14));
                String scoreText = String.valueOf(v.score);
                int textWidth = g2.getFontMetrics().stringWidth(scoreText);
                g2.drawString(scoreText, cx - textWidth/2, cy + 5);
            }
        }
        // Draw the score above the cookie only if its score is > 0.
        if (v.score > 0) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Times New Roman", Font.BOLD, 14));
            String scoreText = String.valueOf(v.score);
            int textWidth = g2.getFontMetrics().stringWidth(scoreText);
                
            // The background text for readability
            g2.setColor(new Color(0, 0, 0, 128));
            g2.fillRoundRect(cx - textWidth/2 - 3, cy - half - 20, textWidth + 6, 16, 8, 8);
                
            // Draw the score
            g2.setColor(Color.WHITE);
            g2.drawString(scoreText, cx - textWidth/2, cy - half - 6);
        }

        // Draw the hitbox outline.
        if (v.id == hoveredCookieId) {
            g2.setColor(Color.GREEN);
            g2.setStroke(new java.awt.BasicStroke(2));
            g2.drawOval(cx - (COOKIE_RADIUS + COOKIE_HITBOX_PADDING), 
                        cy - (COOKIE_RADIUS + COOKIE_HITBOX_PADDING), 
                        (COOKIE_RADIUS + COOKIE_HITBOX_PADDING) * 2, 
                        (COOKIE_RADIUS + COOKIE_HITBOX_PADDING) * 2);
        }

        // Reset the transforms if applied the animation
        if (v.animating) {
            g2.translate(cx, cy);
            g2.scale(1.0f/v.scale, 1.0f/v.scale);
            g2.translate(-cx, -cy);
        }
    }

    private int toWorldX(int px) { 
        // SInce the panel might be scaled, we need to map the pixel position to the world position.
        return (int)((px * WORLD_W) / (double)getWidth()); 
    }
    private int toWorldY(int py) { 
        // Convert the screen y cooridinates to world y coordinates.
        return (int)((py * WORLD_H) / (double)getHeight()); 
    }

    private void handleClick(int px, int py) {
        // Concert screen coordinates to world coordinates.
        int wx = toWorldX(px), wy = toWorldY(py);

        // Check if there are any cookies
         if (cookies.isEmpty()) {
            parent.appendMessage("ERROR: No cookies in the game!");
            return;
        }
        
        // Check the cookies from to to bottom.
        ViewCookie[] cookieArray = cookies.values().toArray(new ViewCookie[0]);
        
        for (int i = cookieArray.length - 1; i >= 0; i--) {
            ViewCookie vc = cookieArray[i];

            // Use the same hitbox as the hover hitbox.
            if (isPointInCookie(wx, wy, vc)) {

                if (vc.locked && username.equals(vc.lockedBy)){
                    
                    // User alredy own this cookie
                    parent.appendMessage("Clicking owned cookie " + vc.id);
                    if (out != null) {
                        out.println(Protocol.CLICK + " " + vc.id);
                    }
                } else if (!vc.locked){
                    if (out != null){
                        out.println(Protocol.LOCK_REQUEST + " " + vc.id);
                    }
                } else {
                    // Cookie is locked by someone else
                    parent.appendMessage("Cookie " + vc.id + " locked by " + vc.lockedBy);
                }
                return;
            }
        }
        parent.appendMessage("Missed - no cookies hit");
    }

    public void spawnCookie(int id,int x,int y,int score){
        cookies.put(id, new ViewCookie(id,x,y,score));
    }

    public void despawnCookie(int id){ 
        startCookieDestructionAnimation(id);
    }

    public void moveCookie(int id,int x,int y,long ts){
        ViewCookie v = cookies.get(id);
        if (v != null) {
            v.setTarget(x,y);
        }
    }

    // 
    public void setCookieState(int id, int x, int y, boolean locked, String lockedBy, int score){
        ViewCookie v = cookies.get(id);
        if (v==null) {
            v = new ViewCookie(id,x,y,score);
            cookies.put(id, v);
            return;
        }

        // Track if the user owns this cookie
        if (locked && username.equals(lockedBy)){
            ownedCookies.add(id);
        }else if (!locked) { 
            ownedCookies.remove(id);
        }

        // Always set the score fromt he server first to prevent any jumps.
        v.score = score;

        // Only change the animation if the score changed and it has not been destroyed
        if (v.score > 0){
            startCookieClickAnimation(id);
        }
        
        v.x=x; v.y=y; v.locked=locked; v.lockedBy=lockedBy;
        cookies.put(id,v);
        repaint(); // Ensure that the visuals are updated.
    }

    public void releaseCookieVisual(int id){ 
        ViewCookie v = cookies.get(id); 
        if (v!=null) { 
            v.locked=false; 
            v.lockedBy="-"; 
            ownedCookies.remove(id); // The user no longer owns it.
            repaint(); // Trigger the repaint to update the visuals.
        } 
    }

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
            if (!v.animating || !animatingCookies.containsKey(v.id)){
                drawCookie(g2, v);
            }
        }

        // Draw animating cookies.
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
