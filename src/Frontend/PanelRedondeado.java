package Frontend;

import javax.swing.*;
import java.awt.*;

import static javax.swing.text.StyleConstants.getBackground;

public class PanelRedondeado extends JPanel {
    private int radioCurvatura;

    public PanelRedondeado(int radioCurvatura) {
        this.radioCurvatura = radioCurvatura;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radioCurvatura, radioCurvatura);
        g2.dispose();
    }
}
