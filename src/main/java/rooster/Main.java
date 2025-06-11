
package rooster;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame ventana = new JFrame("Peleas de gallos");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(800, 400);
        ventana.setResizable(false);

        ventana.setLocationRelativeTo(null);
        
        GamePanel gamePanel = new GamePanel();
        ventana.add(gamePanel);

        ventana.setVisible(true);
    }
}