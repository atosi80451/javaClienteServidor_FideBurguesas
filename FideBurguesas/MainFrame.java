import javax.swing.*;
import java.awt.*;

/** Ventana principal: las pestañas dependen del rol del usuario. */
public class MainFrame extends JFrame {
    private final CajaPanel caja;
    private final CocinaPanel cocina;

    public MainFrame(Usuario u) {
        super("FideBurguesas POS - " + u.getNombre() + " (" + u.getRol() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        caja = new CajaPanel(u);
        cocina = new CocinaPanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Caja", caja);
        tabs.add("Cocina", cocina);
        if (u instanceof Administrador) {
            ProductosPanel productos = new ProductosPanel();
            CombosPanel combos = new CombosPanel();
            // El selector "Producto / Combo" del formulario de combos lleva a la pestaña de productos
            combos.setAlElegirProducto(() -> {
                tabs.setSelectedComponent(productos);
                productos.nuevoProducto();
            });
            tabs.add("Cajeros", new CajerosPanel());
            tabs.add("Productos", productos);
            tabs.add("Combos", combos);
        }
        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c instanceof Refrescable r) r.refrescar();
        });

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        barra.add(new JLabel("Sesión: " + u.getNombre() + " · " + u.getRol()), BorderLayout.WEST);
        JButton volver = new JButton("← Volver al inicio de sesión");
        volver.addActionListener(e -> cerrarSesion());
        barra.add(volver, BorderLayout.EAST);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(barra, BorderLayout.NORTH);
        raiz.add(tabs);
        setContentPane(raiz);
        setSize(980, 640);
        setLocationRelativeTo(null);
    }

    private void cerrarSesion() {
        caja.detener();
        cocina.detener();
        dispose();
        new LoginFrame().setVisible(true);
    }
}