import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {

    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    static final int DELAY = 75;
    final int x[] = new int[GAME_UNITS];
    final int y[] = new int[GAME_UNITS];
    int bodyParts = 6;
    int applesEaten;
    int appleX;
    int appleY;
    int highScore;
    char direction = 'R'; // U = up, D = down, L = left, R = right
    boolean running = false;
    boolean gameOver = false;
    Timer timer;
    Random random;

    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        loadHighScore();
        startGame();
    }

   public void startGame() {
    newApple();
    bodyParts = 6;
    applesEaten = 0;
    direction = 'R';
    running = true;
    gameOver = false;

    // Reset snake position
    for (int i = 0; i < x.length; i++) {
        x[i] = 0;
        y[i] = 0;
    }

    timer = new Timer(DELAY, this);
    timer.start();
}


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            // Optional grid
            for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
                g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
                g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
            }

            // Apple
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            // Snake
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.green);
                } else {
                    g.setColor(new Color(45, 180, 0));
                }
                g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
            }

            // Score
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Score: " + applesEaten, 10, 20);
            g.drawString("High Score: " + highScore, SCREEN_WIDTH - 180, 20);

        } else if (gameOver) {
            showGameOverScreen(g);
        }
    }

    public void newApple() {
        appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
        appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U': y[0] -= UNIT_SIZE; break;
            case 'D': y[0] += UNIT_SIZE; break;
            case 'L': x[0] -= UNIT_SIZE; break;
            case 'R': x[0] += UNIT_SIZE; break;
        }
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    public void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                running = false;
            }
        }

        if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
            gameOver = true;
            updateHighScore();
        }
    }

    public void showGameOverScreen(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 60));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Game Over", 
            (SCREEN_WIDTH - metrics.stringWidth("Game Over")) / 2, 
            SCREEN_HEIGHT / 2 - 50);

        g.setFont(new Font("Arial", Font.PLAIN, 30));
        g.drawString("Score: " + applesEaten, SCREEN_WIDTH / 2 - 70, SCREEN_HEIGHT / 2);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Press ENTER to Retry", SCREEN_WIDTH / 2 - 100, SCREEN_HEIGHT / 2 + 40);
    }

    public void updateHighScore() {
        if (applesEaten > highScore) {
            highScore = applesEaten;
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("highscore.txt"))) {
                writer.write(String.valueOf(highScore));
            } catch (IOException e) {
                System.out.println("Unable to save high score.");
            }
        }
    }

    public void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("highscore.txt"))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line);
            }
        } catch (IOException e) {
            highScore = 0; // No file yet
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

  public class MyKeyAdapter extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if (direction != 'R') direction = 'L';
                break;
            case KeyEvent.VK_RIGHT:
                if (direction != 'L') direction = 'R';
                break;
            case KeyEvent.VK_UP:
                if (direction != 'D') direction = 'U';
                break;
            case KeyEvent.VK_DOWN:
                if (direction != 'U') direction = 'D';
                break;
            case KeyEvent.VK_ENTER:
                if (gameOver) {
                    timer.stop(); // Make sure timer is cleared
                    startGame();  // Restart game
                    repaint();    // Refresh the screen
                }
                break;
        }
    }
  }
}

