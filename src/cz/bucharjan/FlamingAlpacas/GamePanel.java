package cz.bucharjan.FlamingAlpacas;

import java.awt.*;
import java.awt.Color;
import java.awt.Dimension;

public class GamePanel extends javax.swing.JPanel {
    final int fieldSize = 20;
    private Image background;
    private int width;
    private int height;

    public GamePanel (int width, int height) {
        this.width = width;
        this.height = height;

        setPreferredSize(new Dimension(width * fieldSize, height * fieldSize));
    }

    private void repaintBackground () {
        background = createImage(width * fieldSize, height * fieldSize);

        if (background == null) {
            return;
        }

        Graphics g = background.getGraphics();

        g.setColor(Color.lightGray);
        g.fillRect(0, 0, width * fieldSize, height * fieldSize);

        g.setColor(Color.gray);

        for (int i = 0; i <= width; i++) {
            g.drawLine(i * fieldSize, 0, i * fieldSize, height * fieldSize);
        }

        for (int i = 0; i <= height; i++) {
            g.drawLine(0, i * fieldSize, width * fieldSize, i * fieldSize);
        }
    }

    @Override
    protected void paintComponent (Graphics g) {
        super.paintComponent(g);

        if (background == null) {
            repaintBackground();
        }

        g.drawImage(background, 0, 0, null);
    }
}