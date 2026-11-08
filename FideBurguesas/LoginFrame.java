import java.awt.*;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

public class LoginFrame extends JFrame {
    private static final Color OSCURO = new Color(0x2B2420), OSCURO2 = new Color(0x3A302A), BORDE_OSC = new Color(0x4C4038);
    private static final Color CREMA = new Color(0xF5F2ED), NARANJA = new Color(0xE8590C), ROJO = new Color(0xC8382B);
    private static final Color TEXTO = new Color(0x2B2420), GRIS = new Color(0x8A7F76), CLARO = new Color(0xE9DFD3);
    private static final String FUENTE = "Segoe UI";

    private final JTextField usuario = new JTextField();
    private final JPasswordField clave = new JPasswordField();
    private final Tarjeta aviso = new Tarjeta(new Color(0xFDECEA), new Color(0xF2B8B0), 8);
    private final JLabel textoAviso = new JLabel();

    public LoginFrame() {
        super("FideBurguesas POS - Inicio de sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel raiz = new JPanel(new GridLayout(1, 2));
        raiz.add(panelIzquierdo());
        raiz.add(panelDerecho());
        setContentPane(raiz);
        setSize(1000, 620);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);
    }

    private JPanel panelIzquierdo() {
        JPanel p = new JPanel();
        p.setBackground(OSCURO);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(32, 40, 28, 40));

        JPanel marca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        marca.setOpaque(false);
        Tarjeta icono = new Tarjeta(NARANJA, null, 10);
        icono.setLayout(new GridBagLayout());
        icono.setPreferredSize(new Dimension(40, 40));
        icono.add(texto("🍔", 20, Color.WHITE, false));
        JPanel nombres = new JPanel();
        nombres.setOpaque(false);
        nombres.setLayout(new BoxLayout(nombres, BoxLayout.Y_AXIS));
        nombres.add(izq(texto("FideBurguesas", 16, Color.WHITE, true)));
        nombres.add(izq(texto("BUENA COMIDA, BUEN SERVICIO", 9, GRIS, false)));
        marca.add(icono);
        marca.add(nombres);

        p.add(izq(marca));
        p.add(Box.createVerticalGlue());
        p.add(izq(texto("EL BUEN SERVICIO EMPIEZA AQUÍ", 10, NARANJA, true)));
        p.add(Box.createVerticalStrut(10));
        p.add(izq(texto("<html>Cada pedido,<br>un buen momento.</html>", 36, Color.WHITE, true)));
        p.add(Box.createVerticalStrut(12));
        p.add(izq(texto("<html><body style='width:300px'>Tu sucursal, tus pedidos y tu equipo. "
                + "Todo en una misma estación de trabajo.</body></html>", 13, new Color(0xB8ACA2), false)));
        p.add(Box.createVerticalStrut(24));
        p.add(izq(tarjetaTurno()));
        p.add(Box.createVerticalGlue());
        p.add(izq(texto("●  FideBurguesas · Punto de venta interno", 10, GRIS, false)));
        return p;
    }

    private Tarjeta tarjetaTurno() {
        Tarjeta t = new Tarjeta(OSCURO2, BORDE_OSC, 14);
        t.setLayout(new BorderLayout(0, 12));
        t.setBorder(BorderFactory.createEmptyBorder(14, 16, 16, 16));
        t.add(texto("PREPARADO PARA UN GRAN TURNO", 9, GRIS, true), BorderLayout.NORTH);

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        fila.setOpaque(false);
        Tarjeta circulo = new Tarjeta(new Color(0xEADBC3), null, 100);
        circulo.setPreferredSize(new Dimension(84, 84));
        circulo.setLayout(new GridBagLayout());
        circulo.add(texto("🍔", 38, TEXTO, false));

        Tarjeta mini = new Tarjeta(Color.WHITE, null, 12);
        mini.setLayout(new BoxLayout(mini, BoxLayout.Y_AXIS));
        mini.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        mini.add(izq(texto("FideBurguesas", 9, GRIS, true)));
        mini.add(izq(texto("Hecho con ganas.", 16, TEXTO, true)));
        mini.add(izq(texto("Un buen producto, un gran servicio.", 10, GRIS, false)));

        fila.add(circulo);
        fila.add(mini);
        t.add(fila);
        t.setMaximumSize(new Dimension(430, 170));
        return t;
    }

    private JPanel panelDerecho() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CREMA);

        Tarjeta c = new Tarjeta(Color.WHITE, CLARO, 14);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(BorderFactory.createEmptyBorder(28, 30, 24, 30));
        c.setPreferredSize(new Dimension(380, 470));

        c.add(izq(texto("BIENVENIDO A TU TURNO", 10, NARANJA, true)));
        c.add(Box.createVerticalStrut(6));
        c.add(izq(texto("Iniciar sesión", 26, TEXTO, true)));
        c.add(Box.createVerticalStrut(4));
        c.add(izq(texto("<html><body style='width:290px'>Ingrese su usuario y contraseña. "
                + "El menú y la sucursal dependen de su perfil.</body></html>", 11, GRIS, false)));
        c.add(Box.createVerticalStrut(14));

        aviso.setLayout(new BorderLayout());
        aviso.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        textoAviso.setFont(new Font(FUENTE, Font.PLAIN, 11));
        textoAviso.setForeground(ROJO);
        aviso.add(textoAviso);
        aviso.setVisible(false);
        aviso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        c.add(izq(aviso));
        c.add(Box.createVerticalStrut(12));

        c.add(izq(texto("Usuario", 11, TEXTO, true)));
        c.add(Box.createVerticalStrut(4));
        c.add(izq(campo(usuario, null)));
        c.add(Box.createVerticalStrut(12));

        c.add(izq(texto("Contraseña", 11, TEXTO, true)));
        c.add(Box.createVerticalStrut(4));
        clave.setEchoChar('•');
        JButton ojo = new JButton("👁");
        ojo.setBorderPainted(false);
        ojo.setContentAreaFilled(false);
        ojo.setFocusPainted(false);
        ojo.setMargin(new Insets(0, 6, 0, 6));
        ojo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ojo.addActionListener(e -> clave.setEchoChar(clave.getEchoChar() == 0 ? '•' : (char) 0));
        c.add(izq(campo(clave, ojo)));
        c.add(Box.createVerticalStrut(18));

        JButton entrar = boton("Entrar a FideBurguesas");
        entrar.addActionListener(e -> ingresar());
        getRootPane().setDefaultButton(entrar);
        c.add(izq(entrar));
        c.add(Box.createVerticalStrut(14));
        c.add(izq(texto("ⓘ  ¿Necesitas acceso? Habla con tu administrador", 10, GRIS, false)));

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        p.add(c, g);
        g.gridy = 1;
        g.insets = new Insets(14, 0, 0, 0);
        p.add(texto("Sesión local · Roles: administrador y cajero", 10, GRIS, false), g);
        return p;
    }

    private void ingresar() {
        aviso.setVisible(false);
        try {
            Usuario u = Repositorio.get().login(usuario.getText().trim(), new String(clave.getPassword()));
            new MainFrame(u).setVisible(true);
            dispose();
        } catch (FideException e) {
            textoAviso.setText("⚠  Credenciales inválidas. Revise su usuario y contraseña.");
            aviso.setVisible(true);
            clave.setText("");
            revalidate();
            repaint();
        }
    }

    private static JLabel texto(String t, int tam, Color color, boolean negrita) {
        JLabel l = new JLabel(t);
        l.setFont(new Font(FUENTE, negrita ? Font.BOLD : Font.PLAIN, tam));
        l.setForeground(color);
        return l;
    }

    private static <T extends JComponent> T izq(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private JPanel campo(JTextField f, JComponent extra) {
        JPanel c = new JPanel(new BorderLayout());
        c.setBackground(Color.WHITE);
        c.setBorder(new BordeRedondo(new Color(0xD9CFC4), 10));
        f.setBorder(null);
        f.setOpaque(false);
        f.setFont(new Font(FUENTE, Font.PLAIN, 13));
        f.setForeground(TEXTO);
        c.add(f);
        if (extra != null) c.add(extra, BorderLayout.EAST);
        c.setPreferredSize(new Dimension(300, 42));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return c;
    }

    private static JButton boton(String t) {
        JButton b = new JButton(t) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ROJO.darker() : ROJO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setFont(new Font(FUENTE, Font.BOLD, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(300, 44));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return b;
    }

    private static class Tarjeta extends JPanel {
        private final Color fondo, borde;
        private final int radio;
        Tarjeta(Color fondo, Color borde, int radio) {
            this.fondo = fondo; this.borde = borde; this.radio = radio;
            setOpaque(false);
        }
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

    private static class BordeRedondo extends AbstractBorder {
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
        @Override public Insets getBorderInsets(Component c) { return new Insets(8, 12, 8, 12); }
    }
}