package rooster;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.*;

public class GamePanel extends JPanel implements KeyListener {
    private Image gallo1Img, gallo2Img, backgroundImg, gallo1AttackImg, gallo2AttackImg;
    private int x1 = 100, y1 = 200; // Posición gallo 1
    private int x2 = 500, y2 = 200; // Posición gallo 2
    private int health1 = 100, health2 = 100;
    private int width = 150, height = 150; // Tamaño de los gallos
    private boolean attacking1 = false, attacking2 = false;
    private Timer attackTimer;
    private Clip attackSound1, attackSound2;
    private Clip backgroundMusic;

    public GamePanel() {
        loadImages();
        loadSounds();
        setupTimer();
        playBackgroundMusic();
        this.setFocusable(true);
        this.addKeyListener(this);
    }
    private void playBackgroundMusic() {
        try {
            URL musicUrl = getClass().getResource("/sonidos/background_music.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(musicUrl);
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioIn);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);  // Repetir infinitamente
            backgroundMusic.start();
        } catch (Exception e) {
            System.out.println("Error al cargar música: " + e.getMessage());
        }
    }

    private void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
    }
    
    private void showGameOverDialog(String winner) {
        stopBackgroundMusic();  // Detener música al terminar
        
        Object[] options = {"Sí", "No"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "¡" + winner + " ganó!\n¿Quieres jugar de nuevo?",
            "Fin del juego",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

        if (choice == JOptionPane.YES_OPTION) {
            resetGame();
            playBackgroundMusic();  // Reanudar música
        } else {
            System.exit(0);  // Cerrar el juego
        }
    }
    
    private void loadImages() {
        try {
            gallo1Img = ImageIO.read(getClass().getResource("/imagenes/gallo1.png"));
            gallo2Img = ImageIO.read(getClass().getResource("/imagenes/gallo2.png"));
            gallo1AttackImg = ImageIO.read(getClass().getResource("/imagenes/gallo1a.png"));
            gallo2AttackImg = ImageIO.read(getClass().getResource("/imagenes/gallo2a.png"));
            backgroundImg = ImageIO.read(getClass().getResource("/imagenes/fondo.png"));
        } catch (IOException e) {
            showError("Error al cargar imágenes");
        }
    }

    private void loadSounds() {
        try {
            attackSound1 = AudioSystem.getClip();
            attackSound2 = AudioSystem.getClip();
            
            URL sound1 = getClass().getResource("/sonidos/ataque1.wav");
            URL sound2 = getClass().getResource("/sonidos/ataque2.wav");
            
            attackSound1.open(AudioSystem.getAudioInputStream(sound1));
            attackSound2.open(AudioSystem.getAudioInputStream(sound2));
        } catch (Exception e) {
            showError("Error al cargar sonidos");
        }
    }
    
    private void setupTimer(){
        attackTimer = new Timer(300, e-> {
            attacking1 = false;
            attacking2 = false;
            attackTimer.stop();
            repaint();
        });
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
    
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), null);

        Image currentGallo1 = attacking1 ? gallo1AttackImg : gallo1Img;
        Image currentGallo2 = attacking2 ? gallo2AttackImg : gallo2Img;
        g.drawImage(currentGallo1, x1, y1, width, height, null);
        g.drawImage(currentGallo2, x2, y2, width, height, null);

        drawHealthBar(g, x1, y1 - 20, health1, Color.RED);
        drawHealthBar(g, x2, y2 - 20, health2, Color.BLUE);
    }

    private void drawHealthBar(Graphics g, int x, int y, int health, Color color) {
        g.setColor(Color.BLACK);
        g.fillRect(x, y, 100, 10);
        g.setColor(color);
        g.fillRect(x, y, health, 10);
    }
    
    private void checkGameOver() {
        if (health1 <= 0) {
            showGameOverDialog("Jugador 2");
        } else if (health2 <= 0) {
            showGameOverDialog("Jugador 1");
        }
    }
    
   private void resetGame() {
        health1 = 100;
        health2 = 100;
        x1 = 100;
        y1 = 200;
        x2 = 600;
        y2 = 200;
        repaint();
    }
    
    private boolean collidesWithOther(int x, int y) {
        Rectangle thisRect = new Rectangle(x, y, width, height);
        Rectangle otherRect = new Rectangle(x2, y2, width, height);
        if (x == x1 && y == y1) otherRect = new Rectangle(x2, y2, width, height);
        else otherRect = new Rectangle(x1, y1, width, height);
        
        return thisRect.intersects(otherRect);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int prevX1 = x1, prevY1 = y1;
        int prevX2 = x2, prevY2 = y2;

        // Movimiento jugador 1
        if (e.getKeyCode() == KeyEvent.VK_A) x1 -= 10;
        if (e.getKeyCode() == KeyEvent.VK_D) x1 += 10;

        // Movimiento jugador 2
        if (e.getKeyCode() == KeyEvent.VK_LEFT) x2 -= 10;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) x2 += 10;

        // Verificar colisión entre gallos
        if (collidesWithOther(x1, y1)) {
            x1 = prevX1;
            y1 = prevY1;
        }
        if (collidesWithOther(x2, y2)) {
            x2 = prevX2;
            y2 = prevY2;
        }

        // Ataque jugador 1
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            attacking1 = true;
            playSound(attackSound1);
            if (Math.abs(x1 - x2) < width + 20 && Math.abs(y1 - y2) < height + 20) {
                health2 -= 5;
            }
            attackTimer.start();
        }

        // Ataque jugador 2
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            attacking2 = true;
            playSound(attackSound2);
            if (Math.abs(x1 - x2) < width + 20 && Math.abs(y1 - y2) < height + 20) {
                health1 -= 5;
            }
            attackTimer.start();
        }
        
        

        // Limitar bordes de pantalla
        x1 = Math.max(0, Math.min(x1, getWidth() - width));
        y1 = Math.max(0, Math.min(y1, getHeight() - height));
        x2 = Math.max(0, Math.min(x2, getWidth() - width));
        y2 = Math.max(0, Math.min(y2, getHeight() - height));

        checkGameOver();
        repaint();
    }
    
    private void playSound(Clip sound) {
        if (sound != null) {
            sound.setFramePosition(0);
            sound.start();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
}