import java.awt.*;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

abstract class Estilo extends JPanel {

    static final Color CREMA = new Color(0xF5F2ED), TEXTO = new Color(0x2B2420), GRIS = new Color(0x8A7F76),
            BORDE = new Color(0xE9DFD3), BORDE_CAMPO = new Color(0xD9CFC4), ROJO = new Color(0xB9442F),
            ROJO_SUAVE = new Color(0xF8E7E3), VERDE = new Color(0x2E7D4F), VERDE_SUAVE = new Color(0xE3F3E8),
            CAJA = new Color(0xFAF7F2), BEIGE = new Color(0xEADBC3);
    static final String FUENTE = "Segoe UI";

    static JLabel texto(String t, int tam, Color color, boolean negrita) {
        JLabel l = new JLabel(t);
        l.setFont(new Font(FUENTE, negrita ? Font.BOLD : Font.PLAIN, tam));
        l.setForeground(color);
        return l;
    }

    static <T extends JComponent> T izq(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    static <T extends JTextField> T entrada(T f) {
        f.setFont(new Font(FUENTE, Font.PLAIN, 13));
        f.setForeground(TEXTO);
        f.setBorder(BorderFactory.createCompoundBorder(new BordeRedondo(BORDE_CAMPO, 10),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        f.setPreferredSize(new Dimension(200, 42));
        return f;
    }

    static void estilizar(JComboBox<?> c) {
        c.setFont(new Font(FUENTE, Font.PLAIN, 13));
        c.setBackground(Color.WHITE);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        c.setPreferredSize(new Dimension(200, 42));
    }

    static class Tarjeta extends JPanel {
        private Color fondo;
        private final Color borde;
        private final int radio;
        Tarjeta(Color fondo, Color borde, int radio) {
            this.fondo = fondo; this.borde = borde; this.radio = radio;
            setOpaque(false);
        }
        void setFondo(Color c) { fondo = c; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fondo);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
            if (borde != null) {
                g2.setColor(borde);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class BordeRedondo extends AbstractBorder {
        private final Color color;
        private final int radio;
        BordeRedondo(Color color, int radio) { this.color = color; this.radio = radio; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w - 1, h - 1, radio, radio);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
    }

    static class Boton extends JButton {
        private final int tipo;
        Boton(String texto, int tipo) {
            super(texto);
            this.tipo = tipo;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font(FUENTE, Font.BOLD, 13));
            setForeground(tipo == 0 ? Color.WHITE : tipo == 2 ? ROJO : TEXTO);
            setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean activo = isEnabled();
            boolean sobre = activo && getModel().isRollover();
            Color fondo = tipo == 0 ? (!activo ? new Color(0xD9A79C) : sobre ? ROJO.darker() : ROJO)
                                    : (sobre ? CAJA : Color.WHITE);
            g2.setColor(fondo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            if (tipo != 0) {
                g2.setColor(tipo == 2 ? new Color(0xE3B5AB) : BORDE_CAMPO);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
            if (!activo) { // el texto deshabilitado se dibuja a mano para conservar el estilo
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                g2.setColor(tipo == 0 ? Color.WHITE : GRIS);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
                return;
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}