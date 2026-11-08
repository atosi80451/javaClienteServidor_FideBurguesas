import java.awt.*;
import javax.swing.*;

public class CocinaPanel extends JPanel {
    private final DefaultListModel<Pedido> modelo = new DefaultListModel<>();
    private final JList<Pedido> lista = new JList<>(modelo);
    private final Thread hilo;

    public CocinaPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        lista.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        add(new JLabel("Pedidos pendientes de preparar"), BorderLayout.NORTH);
        add(new JScrollPane(lista));
        JButton prep = new JButton("En preparación"), listo = new JButton("Listo ✔");
        prep.addActionListener(e -> cambiar(EstadoPedido.EN_PREPARACION));
        listo.addActionListener(e -> cambiar(EstadoPedido.LISTO));
        JPanel b = new JPanel(); b.add(prep); b.add(listo);
        add(b, BorderLayout.SOUTH);

        Repositorio repo = Repositorio.get();
        repo.getCola().clear();
        for (Pedido p : repo.pedidos())
            if (p.getEstado() == EstadoPedido.PENDIENTE || p.getEstado() == EstadoPedido.EN_PREPARACION) modelo.addElement(p);

        hilo = new Thread(() -> {
            try {
                while (true) {
                    Pedido p = repo.getCola().take();
                    SwingUtilities.invokeLater(() -> modelo.addElement(p));
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "hilo-cocina");
        hilo.setDaemon(true);
        hilo.start();
    }

    public void detener() {
        if (hilo != null) hilo.interrupt(); 
    }

    private void cambiar(EstadoPedido nuevo) {
        Pedido p = lista.getSelectedValue();
        if (p == null) return;
        try {
            p.cambiarEstado(nuevo);
            Repositorio.get().guardar();
            if (nuevo == EstadoPedido.LISTO) modelo.removeElement(p); else lista.repaint();
        } catch (FideException e) { Ui.error(this, e); }
    }
}
