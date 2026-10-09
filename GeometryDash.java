import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeometryDash extends JPanel implements ActionListener {
    static final int W = 800, H = 400, GROUND = 320, SIZE = 40, PX = 120;
    double py, vy, angle, speed, nextSpawn;
    boolean onGround, dead;
    int score, best;
    List<double[]> obs = new ArrayList<>();
    Random rnd = new Random();

    GeometryDash() {
        setPreferredSize(new Dimension(W, H));
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_UP) jump();
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { jump(); }
        });
        reset();
        new Timer(16, this).start();
    }

    void reset() {
        py = GROUND - SIZE; vy = 0; angle = 0; speed = 6; nextSpawn = 500;
        onGround = true; dead = false; score = 0;
        obs.clear();
    }

    void jump() {
        if (dead) reset();
        else if (onGround) { vy = -13; onGround = false; }
    }

    public void actionPerformed(ActionEvent e) {
        if (!dead) update();
        repaint();
    }

    void update() {
        score++;
        vy += 0.7;
        py += vy;
        onGround = false;
        if (py >= GROUND - SIZE) { py = GROUND - SIZE; vy = 0; onGround = true; }


        nextSpawn -= speed;
        if (nextSpawn <= 0) {
            int type = rnd.nextInt(3) == 0 ? 1 : 0;
            obs.add(new double[]{W, type});
            if (type == 0 && rnd.nextInt(3) == 0) obs.add(new double[]{W + SIZE, 0}); // double spike
            nextSpawn = 330 + rnd.nextInt(250);
        }


        Rectangle player = new Rectangle(PX + 4, (int) py + 4, SIZE - 8, SIZE - 8);
        for (double[] o : obs) {
            o[0] -= speed;
            int ox = (int) o[0];
            if (o[1] == 0) {
                if (player.intersects(new Rectangle(ox + 12, GROUND - 28, 16, 28))) dead = true;
            } else if (PX + SIZE > ox && PX < ox + SIZE && py + SIZE > GROUND - SIZE) {
                if (py + SIZE - vy <= GROUND - SIZE + 1) {
                    py = GROUND - 2 * SIZE; vy = 0; onGround = true;
                } else dead = true;
            }
        }
        obs.removeIf(o -> o[0] < -2 * SIZE);


        if (onGround) angle = Math.round(angle / 90) * 90;
        else angle += 2.5;

        if (dead) best = Math.max(best, score);
    }

    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // background + ground
        g.setColor(new Color(30, 40, 90));
        g.fillRect(0, 0, W, H);
        g.setColor(new Color(15, 15, 35));
        g.fillRect(0, GROUND, W, H - GROUND);
        g.setColor(Color.CYAN);
        g.drawLine(0, GROUND, W, GROUND);

        // obstacles
        for (double[] o : obs) {
            int ox = (int) o[0];
            if (o[1] == 0) {
                g.setColor(Color.WHITE);
                g.fillPolygon(new int[]{ox, ox + SIZE / 2, ox + SIZE},
                              new int[]{GROUND, GROUND - SIZE, GROUND}, 3);
            } else {
                g.setColor(Color.MAGENTA);
                g.fillRect(ox, GROUND - SIZE, SIZE, SIZE);
                g.setColor(Color.WHITE);
                g.drawRect(ox, GROUND - SIZE, SIZE, SIZE);
            }
        }

        // player (rotated cube with a face)
        double cx = PX + SIZE / 2.0, cy = py + SIZE / 2.0;
        g.rotate(Math.toRadians(angle), cx, cy);
        g.setColor(dead ? Color.RED : Color.YELLOW);
        g.fillRect(PX, (int) py, SIZE, SIZE);
        g.setColor(Color.BLACK);
        g.drawRect(PX, (int) py, SIZE, SIZE);
        g.fillRect(PX + 8, (int) py + 10, 8, 8);
        g.fillRect(PX + 24, (int) py + 10, 8, 8);
        g.fillRect(PX + 8, (int) py + 28, 24, 4);
        g.rotate(-Math.toRadians(angle), cx, cy);

        // HUD
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + score + "   Best: " + best, 15, 28);
        if (dead) {
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("GAME OVER - press SPACE", 190, 180);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Geometry Dash");
            f.add(new GeometryDash());
            f.pack();
            f.setResizable(false);
            f.setLocationRelativeTo(null);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);
        });
    }
}